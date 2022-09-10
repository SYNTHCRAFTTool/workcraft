package org.workcraft.plugins.sts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.sts.observers.FirstStateSupervisor;
import org.workcraft.plugins.sts.tools.STSSelectionTool;
import org.workcraft.plugins.sts.tools.SetLocalitiesToEventsTool;
import org.workcraft.plugins.sts.tools.StateGeneratorTool;
import org.workcraft.utils.Hierarchy;

@DisplayName("Step Transition System")

public class VisualSTS extends AbstractVisualModel {

	public VisualSTS(STS model) {
		this(model, null);
	}

	public VisualSTS(STS model, VisualGroup root) {
		super(model, root);
		setGraphEditorTools();
        new FirstStateSupervisor().attach(getRoot());
	}

	private void setGraphEditorTools() {
		List<GraphEditorTool> tools = new ArrayList<>();
		tools.add(new STSSelectionTool());
		tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
		tools.add(new StateGeneratorTool());
        tools.add(new SetLocalitiesToEventsTool());
		setGraphEditorTools(tools);
	}

	public STS getStepTransitionSystemModel() {
		return (STS) getMathModel();
	}

	public VisualState createState(String mathName, Container container) {
		if (container == null) {
			container = getRoot();
		}
		Container mathContainer = NamespaceHelper.getMathContainer(this, container);
		State state = getStepTransitionSystemModel().createState(mathName, mathContainer);
		VisualState visualCase = new VisualState(state);
		container.add(visualCase);
		return visualCase;
	}

	@Override
	public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {		
	    if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
		if (!(first instanceof VisualComponent) || !(second instanceof VisualComponent)) {
			throw new InvalidConnectionException("Invalid connection.");
		}
		getMathModel().validateConnection(((VisualComponent) first).getReferencedComponent(),
				((VisualComponent) second).getReferencedComponent());
	}

	@Override
	public VisualConnection connect(VisualNode first, VisualNode second) throws InvalidConnectionException {
		VisualConnection connection = null;
		if ((first != null) && (second != null)) {
			connection = connect(first, second, null);
		}
		return connection;
	}

	//@Override
	public VisualTransitionArc connect(VisualNode fromState, VisualNode toState, TransitionArc mConnection)
			throws InvalidConnectionException {
		STS sts = (STS) getMathModel();
		VisualTransitionArc vConnection = null;
		validateConnection(fromState, toState);
		if (mConnection == null) {
			State fState = null;
			if (fromState instanceof VisualState) {
				fState = ((VisualState) fromState).getReferencedState();
			}
			State tState = null;
			if (toState instanceof VisualState) {
				tState = ((VisualState) toState).getReferencedState();
			}
			if ((fState != null) && (tState != null)) {
				mConnection = sts.connect(fState, tState);
			}
			vConnection = new VisualTransitionArc(fromState, toState, mConnection);
			Container container = Hierarchy.getNearestContainer(fromState, toState);
			container.add(vConnection);
		}
		return vConnection;
	}

	public Collection<VisualState> getVisualStates() {                                         
		return Hierarchy.getDescendantsOfType(getRoot(), VisualState.class);
	}

	public Collection<TransitionArc> getTransitionArcs() {
		return Hierarchy.getChildrenOfType(getRoot(), TransitionArc.class);
	}
}
