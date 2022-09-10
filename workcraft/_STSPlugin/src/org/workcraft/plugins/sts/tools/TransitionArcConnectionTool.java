package org.workcraft.plugins.sts.tools;

import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.plugins.sts.VisualState;
import org.workcraft.plugins.sts.VisualTransitionArc;
import org.workcraft.utils.GuiUtils;

public class TransitionArcConnectionTool extends ConnectionTool {
	
	public TransitionArcConnectionTool(){
        super(true, true, true);
	}
	
	 @Override
	    public boolean isConnectable(Node node) {
	        return (node instanceof VisualState);
	    }
	 
	 @Override
	    public VisualConnection createTemplateNode() {
	        return new VisualTransitionArc();
	    }
	 
	 @Override
	    public Icon getIcon() {
	        return GuiUtils.createIconFromSVG("images/tool-connection.svg");
	    }
	 
	 @Override
	    public String getLabel() {
	        return "Transition-arc";
	    }

	    @Override
	    public int getHotKeyCode() {
	        return KeyEvent.VK_T;
	    }
}
