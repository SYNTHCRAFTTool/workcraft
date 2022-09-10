package org.workcraft.plugins.sts.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.plugins.sts.STSModel;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.VisualState;
import org.workcraft.plugins.sts.VisualTransitionArc;

public class STSSimulationTool extends SimulationTool {

	

    public STSSimulationTool() {
        this(false);
    }

    public STSSimulationTool(boolean enableTraceGraph) {
        super(enableTraceGraph);
    }
    
    public STSModel getUnderlyingStepTransitionSystem() {
        return (STSModel) getUnderlyingModel().getMathModel();
    }
    
    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
    }

    @Override
    public boolean isConnectionExcited(VisualConnection connection) {
        VisualNode first = connection.getFirst();
        State state = null;
        if (first instanceof VisualState) {
        	state = ((VisualState) first).getReferencedState();
        }
        return (state != null);
    }

	@Override
	public HashMap<? extends Node, Integer> readModelState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeModelState(Map<? extends Node, Integer> state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applySavedState(GraphEditor editor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<? extends Node> getEnabledNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabledNode(Node node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean fire(String ref) {
		// TODO Auto-generated method stub
		
		VisualTransitionArc transitionArc = null;
        STSModel sts = getUnderlyingStepTransitionSystem();
        if (ref != null) {
            final Node node = sts.getNodeByReference(ref);

            if (node instanceof VisualTransitionArc) {
                transitionArc = (VisualTransitionArc) node;
        		System.out.println("true");

            }
        }

        sts.fire();
		return true;
	}

	@Override
	public boolean unfire(String ref) {
		// TODO Auto-generated method stub
		return false;
	}
}
