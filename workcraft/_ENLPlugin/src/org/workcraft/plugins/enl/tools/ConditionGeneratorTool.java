package org.workcraft.plugins.enl.tools;

import java.awt.Cursor;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.VisualCondition;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ConditionGeneratorTool extends NodeGeneratorTool {

    public ConditionGeneratorTool() {
        super(new DefaultNodeGenerator(Condition.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent event) {
        WorkspaceEntry we = event.getEditor().getWorkspaceEntry();
        VisualNode node = we.getTemplateNode();
        if (node instanceof VisualCondition) {
            VisualCondition condition = (VisualCondition) node;
            condition.getReferencedCondition().setTokens(event.isMenuKeyDown() ? 1 : 0);
        }
        super.mousePressed(event);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        // message appears when we put a muse on a condition button generator.
        return "Click to create an empty condition (hold " + DesktopApi.getMenuKeyName() + " to mark it with a token).";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (menuKeyDown) {
            return GuiUtils.createCursorFromSVG("images/elementarynet-node-condition-marked.svg");
        } else {
            return GuiUtils.createCursorFromSVG("images/elementarynet-node-condition-empty.svg");
        }
    }
}
