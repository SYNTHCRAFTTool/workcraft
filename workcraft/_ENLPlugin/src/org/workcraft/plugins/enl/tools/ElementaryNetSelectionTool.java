package org.workcraft.plugins.enl.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.enl.VisualCondition;

public class ElementaryNetSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getEditor().getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = (VisualNode) HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualCondition) {
                VisualCondition condition = (VisualCondition) node;
                if (condition.getReferencedCondition().getTokens() <= 1) {
                    e.getEditor().getWorkspaceEntry().saveMemento();
                    //  Add or Remover a token to a condition by clicking on it.
                    if (condition.getReferencedCondition().getTokens() == 1) {
                        condition.getReferencedCondition().setTokens(0);
                    } else {
                        condition.getReferencedCondition().setTokens(1);
                    }
                }
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

}
