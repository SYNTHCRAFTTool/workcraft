package org.workcraft.plugins.enl.tools;

import org.workcraft.dom.Node;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.plugins.enl.VisualCondition;
import org.workcraft.plugins.enl.VisualTransition;

public class ElementaryNetConnectionTool extends ConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualCondition) || (node instanceof VisualTransition);
    }
}
