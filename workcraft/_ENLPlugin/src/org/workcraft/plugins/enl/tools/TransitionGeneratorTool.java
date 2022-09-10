package org.workcraft.plugins.enl.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.VisualTransition;
import org.workcraft.utils.DesktopApi;
import org.workcraft.workspace.WorkspaceEntry;

public class TransitionGeneratorTool extends NodeGeneratorTool {

    public TransitionGeneratorTool() {
        super(new DefaultNodeGenerator(Transition.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent event) {
        WorkspaceEntry we = event.getEditor().getWorkspaceEntry();
        VisualNode node = we.getTemplateNode();
        if (node instanceof VisualTransition) {
            VisualTransition transition = (VisualTransition) node;
            transition.getReferencedTransition().setLocality(event.isMenuKeyDown() ? 1 : 0);
        }
        super.mousePressed(event);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        // message appears when we put a muse on an event button generator.
        return "Click to create an event (hold " +
                DesktopApi.getMenuKeyName() +
                " to mark it with a locality).";
    }
}

