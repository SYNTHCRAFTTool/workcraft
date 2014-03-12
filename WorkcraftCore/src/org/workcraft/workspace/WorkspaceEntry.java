/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.workspace;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.workspace.Path;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

public class WorkspaceEntry implements ObservableState {
	private ModelEntry modelEntry = null;
	private boolean changed = true;
	private boolean temporary = true;
	private final Framework framework;
	private final Workspace workspace;
	private final MementoManager history = new MementoManager();
	private boolean canModify = true;
	private Memento capturedMemento = null;
	private Memento savedMemento = null;

	public WorkspaceEntry(Workspace workspace) {
		this.workspace = workspace;
		if (workspace != null) {
			this.framework = workspace.getFramework();
		} else {
			this.framework = null;
		}
	}

	public void setChanged(boolean changed) {
		if(this.changed != changed) {
			this.changed = changed;
			if (changed == false) {
				savedMemento = null;
			}
			workspace.fireEntryChanged(this);
		}
	}

	public boolean isChanged() {
		return changed;
	}

	public ModelEntry getModelEntry() {
		return modelEntry;
	}

	private StateObserver modelObserver = new StateObserver(){
		@Override
		public void notify(StateEvent e) {
			if (e instanceof ModelModifiedEvent) {
				setChanged(true);
			}
			observableState.sendNotification(e);
		}
	};

	public void setModelEntry(ModelEntry modelEntry)
	{
		if(this.modelEntry != null) {
			if (this.modelEntry.isVisual()) {
				this.modelEntry.getVisualModel().removeObserver(modelObserver);
			}
		}
		this.modelEntry = modelEntry;

		observableState.sendNotification(new StateEvent() {
			@Override
			public Object getSender() {
				return this;
			}
		});

		if (this.modelEntry.isVisual()) {
			this.modelEntry.getVisualModel().addObserver(modelObserver);
		}
	}

	public boolean isWork() {
		return (modelEntry != null) || (getWorkspacePath().getNode().endsWith(".work"));
	}

	public String getTitle() {
		String res;
		String name = getWorkspacePath().getNode();
		if (isWork()) {
			int dot = name.lastIndexOf('.');
			if (dot == -1) {
				res = name;
			} else {
				res = name.substring(0,dot);
			}
		} else {
			res = name;
		}
		return res;
	}

	@Override
	public String toString() {
		String res = getTitle();
		if (modelEntry != null && modelEntry.isVisual()) {
			res = res + " [V]";
		}
		if (changed) {
			res = "* " + res;
		}
		if (temporary) {
			res = res + " (not in workspace)";
		}
		return res;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	public Path<String> getWorkspacePath() {
		return workspace.getPath(this);
	}

	public File getFile() {
		return workspace.getFile(this);
	}

	ObservableStateImpl observableState = new ObservableStateImpl();

	@Override
	public void addObserver(StateObserver obs) {
		observableState.addObserver(obs);
	}

	@Override
	public void removeObserver(StateObserver obs) {
		observableState.removeObserver(obs);
	}

	public void updateActionState() {
		MainWindowActions.EDIT_UNDO_ACTION.setEnabled(canModify && history.canUndo());
		MainWindowActions.EDIT_REDO_ACTION.setEnabled(canModify && history.canRedo());
		MainWindowActions.EDIT_CUT_ACTION.setEnabled(canModify);
		MainWindowActions.EDIT_COPY_ACTION.setEnabled(canModify);
		MainWindowActions.EDIT_PASTE_ACTION.setEnabled(canModify);
		MainWindowActions.EDIT_DELETE_ACTION.setEnabled(canModify);
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
		updateActionState();
	}

	public boolean getCanModify() {
		return canModify;
	}

	public void captureMemento() {
		capturedMemento = framework.save(modelEntry);
		if (changed == false) {
			savedMemento = capturedMemento;
		}
	}

	public void cancelMemento() {
		setModelEntry(framework.load(capturedMemento));
		setChanged(savedMemento != capturedMemento);
		capturedMemento = null;
	}

	public void saveMemento() {
		Memento currentMemento = capturedMemento;
		capturedMemento = null;
		if (currentMemento == null) {
			currentMemento = framework.save(modelEntry);
		}
		if (changed == false) {
			savedMemento = currentMemento;
		}
		history.pushUndo(currentMemento);
		history.clearRedo();
		updateActionState();
	}

	public void undo() {
		if (history.canUndo()) {
			Memento undoMemento = history.pullUndo();
			if (undoMemento != null) {
				Memento currentMemento = framework.save(modelEntry);
				if (changed == false) {
					savedMemento = currentMemento;
				}
				history.pushRedo(currentMemento);
				setModelEntry(framework.load(undoMemento));
				setChanged(undoMemento != savedMemento);
			}
		}
		updateActionState();
	}

	public void redo() {
		if (history.canRedo()) {
			Memento redoMemento = history.pullRedo();
			if (redoMemento != null) {
				Memento currentMemento = framework.save(modelEntry);
				if (changed == false) {
					savedMemento = currentMemento;
				}
				history.pushUndo(currentMemento);
				setModelEntry(framework.load(redoMemento));
				setChanged(redoMemento != savedMemento);
			}
		}
		updateActionState();
	}

	public void insert(ModelEntry me) {
		try {
			Memento currentMemento = framework.save(modelEntry);
			Memento insertMemento = framework.save(me);
			ModelEntry result = framework.load(currentMemento.getStream(), insertMemento.getStream());
			saveMemento();
			setModelEntry(result);
			setChanged(true);
		} catch (DeserialisationException e) {
			e.printStackTrace();
		}
	}

	public void copy() {
		VisualModel model = modelEntry.getVisualModel();
		if (model.getSelection().size() > 0) {
			captureMemento();

			try {

				// copy selected nodes inside a group as if it was the root
				while (model.getCurrentLevel() != model.getRoot()) {
					Collection<Node> nodes = new HashSet<Node>(model.getSelection());
					Container level = model.getCurrentLevel();
					Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
					if (parent != null) {
						model.setCurrentLevel(parent);
						model.addToSelection(level);
					}
					model.ungroupSelection();
					model.select(nodes);
				}
				model.selectInverse();
				model.deleteSelection();
				framework.clipboard = framework.save(modelEntry);

				if (CommonEditorSettings.getDebugClipboard()) {
					String smodel = framework.saveToString(modelEntry);

					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(new StringSelection(smodel), null);
				}

			} finally {

				cancelMemento();
			}

		}
	}

	public void cut() {
		copy();
		delete();
	}

	public void paste() {
		if (framework.clipboard != null) {
			try {
				Memento memento = framework.save(modelEntry);
				ModelEntry result = framework.load(memento.getStream(), framework.clipboard.getStream());
				saveMemento();
				setModelEntry(result);
				setChanged(true);
				VisualModelTransformer.translateSelection(result.getVisualModel(), 1.0, 1.0);
			} catch (DeserialisationException e) {
				e.printStackTrace();
			}
		}
	}


	public void delete() {
		VisualModel model = modelEntry.getVisualModel();
		if (model.getSelection().size() > 0) {
			saveMemento();
			model.deleteSelection();
		}
	}

}
