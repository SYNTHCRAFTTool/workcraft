package org.workcraft.plugins.enl.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SimulationTool;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.ENLModel;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.VisualCondition;
import org.workcraft.plugins.enl.utils.ElementaryNetUtils;
import org.workcraft.utils.LogUtils;

public class ElementaryNetSimulationTool extends SimulationTool {

    public ElementaryNetSimulationTool() {
        this(false);
    }

    public ElementaryNetSimulationTool(boolean enableTraceGraph) {
        super(enableTraceGraph);
    }

    public ENLModel getUnderlyingElementaryNet() {
        return (ENLModel) getUnderlyingModel().getMathModel();
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
    }

    @Override
    public boolean isConnectionExcited(VisualConnection connection) {
        VisualNode first = connection.getFirst();
        Condition condition = null;
        if (first instanceof VisualCondition) {
            condition = ((VisualCondition) first).getReferencedCondition();
        }
        return (condition != null) && (condition.getTokens() > 0);
    }

    @Override
    public HashMap<? extends Node, Integer> readModelState() {
        // Return HashMap contains all conditions and their number of tokens.
        return ElementaryNetUtils.getMarking(getUnderlyingElementaryNet());
    }

    @Override
    public void writeModelState(Map<? extends Node, Integer> state) {
        HashSet<Condition> conditions = new HashSet<>(getUnderlyingElementaryNet().getConditions());
        for (Node node : state.keySet()) {
            if (node instanceof Condition) {
                Condition condition = (Condition) node;
                if (conditions.contains(condition)) {
                    condition.setTokens(state.get(condition));
                } else {
                    ExceptionDialog
                            .show(new RuntimeException("Condition " + condition.toString() + " is not in the model"));
                }
            }
        }
    }

    @Override
    public void applySavedState(final GraphEditor editor) {
        if ((savedState == null) || savedState.isEmpty()) {
            return;
        }
        MathModel model = editor.getModel().getMathModel();
        if (model instanceof ENLModel) {
            ENLModel elementaryNet = (ENLModel) model;
            editor.getWorkspaceEntry().saveMemento();
            for (Condition condition : elementaryNet.getConditions()) {
                String ref = elementaryNet.getNodeReference(condition);
                Node underlyingNode = getUnderlyingElementaryNet().getNodeByReference(ref);
                if ((underlyingNode instanceof Condition) && savedState.containsKey(underlyingNode)) {
                    Integer tokens = savedState.get(underlyingNode);
                    condition.setTokens(tokens);
                }
            }
        }
    }

    @Override
    public boolean isEnabledNode(Node node) {
        boolean result = false;
        ENLModel elementaryNet = getUnderlyingElementaryNet();
        if ((elementaryNet != null) && (node instanceof Transition)) {
            Transition transition = (Transition) node;
            result = elementaryNet.isEnabled(transition);
        }
        return result;
    }

    @Override
    public ArrayList<Node> getEnabledNodes() {
        ArrayList<Node> result = new ArrayList<>();
        for (Transition transition : getUnderlyingElementaryNet().getTransitions()) {
            if (isEnabledNode(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    @Override
    public boolean fire(String ref) {
        boolean result = false;
        Transition transition = null;
        String traceRef = "";
        ENLModel elementaryNet = getUnderlyingElementaryNet();
        ENL net = (ENL) elementaryNet;
        ElementaryNetUtils.checkSoundness(elementaryNet, false);
        Set<Set<Transition>> e = ElementaryNetUtils.getControled(net, ElementaryNetUtils.getEnabledTransitions(net),
                ElementaryNetUtils.getConflict(net));
        Set<Set<Transition>> controlEnabledSteps = ElementaryNetUtils
                .removeEmptySet(ElementaryNetUtils.getControlEnabledSteps(net, e));
        Set<Transition> step = new HashSet<>();
        if (ref != null) {
            final Node node = elementaryNet.getNodeByReference(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (!controlEnabledSteps.isEmpty()) {
            if (controlEnabledSteps.size() <= 1) {
                LogUtils.logInfo("There is an one cotrol enabled step as follows:");
            } else {
                LogUtils.logInfo("There are " + controlEnabledSteps.size() + " cotrol enabled steps: ");
            }        
            printSetOfevents(controlEnabledSteps, net);            
            for (Set<Transition> controlEnabledStep : controlEnabledSteps) {
//                if (controlEnabledStep.contains(transition) & controlEnabledStep.size() == 1) {
//                    step = controlEnabledStep;
//                    result = true;
//                    break;
//                } else 
                if (controlEnabledStep.contains(transition)) {
                    step = controlEnabledStep;
                    result = true;
                }
            }
            int l = 0;
            String s = "The executed step is ";
            for (Transition t : step) {
                elementaryNet.fire(t);
                l++;
                traceRef = traceRef + net.getName(t);
                if (l < step.size()) {
                    traceRef = traceRef + ",";
                }
            }
                traceRef = "{" + traceRef + "}";
            System.out.println( s + traceRef);
            super.mainTrace.add(traceRef);
        }
        return result;
    }

    public void printSetOfevents(Set<Set<Transition>> steps, ENL net) {
        System.out.print("{");
        int r = 0;
        for (Set<Transition> tt : steps) {
            r++;
            int k = 0;
            System.out.print("{");
            for (Transition t : tt) {
                k++;
                System.out.print(t.getName(net));
                System.out.print((k < tt.size()) ? "," : "");
            }
            System.out.print("}");
            System.out.print((r < steps.size()) ? "," : "");
        }
        System.out.println("}");
    }
    @Override
    public boolean unfire(String ref) {
        boolean result = false;
        Transition transition = null;
        if (ref != null) {
            final Node node = getUnderlyingElementaryNet().getNodeByReference(ref);
            if (node instanceof Transition) {
                transition = (Transition) node;
            }
        }
        if (transition != null) {
            if (getUnderlyingElementaryNet().isUnfireEnabled(transition)) {
                getUnderlyingElementaryNet().unFire(transition);
                result = true;
            }
        }
        return result;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a highlighted event to fire it.";
    }
}
