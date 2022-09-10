package org.workcraft.plugins.enl.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.plugins.enl.utils.DataOfStepTransitionSystem;
import org.workcraft.plugins.enl.utils.ElementaryNetUtils;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.TransitionArc;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.plugins.sts.VisualState;
import org.workcraft.plugins.sts.VisualTransitionArc;
import org.workcraft.plugins.sts.utils.DataOfSTS;
import org.workcraft.types.Pair;
import org.workcraft.utils.LogUtils;

public class ENLToSTSConverter {
    private VisualENL enl;
    private VisualSTS vsts;
    private ArrayList<Set<Condition>> coditions = new ArrayList<>();
    private ArrayList<DataOfStepTransitionSystem> enlDataOfSTS = new ArrayList<>();
    private ArrayList<DataOfStepTransitionSystem> newDataOfSTS = new ArrayList<>();
    private DataOfStepTransitionSystem stepFromSToS;
    VisualTransitionArc fromStoSconnection;
    private Map<Set<Condition>, VisualState> statesMap;
    ArrayList<DataOfSTS> fristStepSecond = new ArrayList<>();
    ArrayList<DataOfSTS> fristStepSecondForThin = new ArrayList<>();

    public ENLToSTSConverter(VisualENL enl) {
        this.enl = enl;
        ENL net = enl.getElementaryNetModel();
        Map<Transition, Set<Transition>> conflictedEvents = ElementaryNetUtils.getConflict(net);
        ElementaryNetUtils.getSetOfSTS(net, coditions, enlDataOfSTS, conflictedEvents);
        this.vsts = new VisualSTS(new STS());
        statesMap = convertConditions();
        try {
            connectCases();
        } catch (InvalidConnectionException e) {
            // throw new RuntimeException(e);
        }
    }

    private Map<Set<Condition>, VisualState> convertConditions() {
        Map<Set<Condition>, VisualState> result = new HashMap<>();
        Set<Condition> intial = ElementaryNetUtils.initialConditions(enl.getElementaryNetModel());
        int n = 0;
        if (!coditions.isEmpty()) {
            for (Set<Condition> c0 : coditions) {
                if (!c0.equals(null)) {
                    VisualState newCase = vsts.createState(null, null);
                    if (c0.equals(intial)) {
                        newCase.getReferencedState().setInitial(true);
                    }
                    // newCase.copyPosition(place);
                    // newCase.copyStyle(newCase);
                    newCase.setX(n);
                    newCase.setY(n);
                    n++;
                    result.put(c0, newCase);
                }
            }
//		for (Entry<Set<Condition>, VisualState> entry : result.entrySet()) {
//			System.out.print("{");
//			for (Condition c : entry.getKey()) {
//				System.out.print(enl.getMathModel().getName(c));
//			}
//			System.out.print("}-{");
//			System.out.println(vsts.getMathModel().getNodeReference(entry.getValue().getReferencedState()) + "}");
//
//		}
        } else {
            LogUtils.logWarning("The enl-system does not have a step transition system");
        }
        return result;
    }

    private void connectCases() throws InvalidConnectionException {
        boolean hasState1 = false;
        boolean hasState2 = false;
        if (!enlDataOfSTS.isEmpty() & !enlDataOfSTS.equals(null)) {
            for (DataOfStepTransitionSystem step : enlDataOfSTS) {
                Set<Condition> state1 = step.getPair().getFirst();
                Set<Condition> state2 = step.getPair().getSecond();
                Set<Transition> events = step.getEvent();
                VisualState fromState = new VisualState(null);
                VisualState toState = new VisualState(null);
                for (Entry<Set<Condition>, VisualState> entry : statesMap.entrySet()) {
                    if (!entry.getKey().isEmpty() & !entry.getKey().equals(null)) {
                        if (state1.equals(entry.getKey())) {
                            fromState = entry.getValue();
                            hasState1 = true;
                        } else if (state2.equals(entry.getKey())) {
                            toState = entry.getValue();
                            hasState2 = true;
                        }
                        if (hasState1 & hasState2)
                            break;
                    }
                }
                hasState1 = false;
                hasState2 = false;
                if ((!fromState.equals(null)) && (!toState.equals(null))) {

                    State q0 = null;
                    if (fromState instanceof VisualState) {
                        q0 = ((VisualState) fromState).getReferencedState();
                    }

                    State q1 = null;
                    if (toState instanceof VisualState) {
                        q1 = ((VisualState) toState).getReferencedState();
                    }
                    stepFromSToS = new DataOfStepTransitionSystem(events, new Pair<>(fromState, toState));
                    newDataOfSTS.add(stepFromSToS);
                    VisualConnection fromSToSConnection = vsts.connect(fromState, toState);
                    VisualTransitionArc fromqToqConnection = (VisualTransitionArc) fromSToSConnection;
                    TransitionArc fromStateToStateConnection = ((VisualTransitionArc) fromqToqConnection)
                            .getReferencedTransitionArc();
                    int n = 0;
                    // if a step is a singleton step like: e
                    if (events.size() == 1) {
                        Set<String> U = new HashSet<>();
                        String name = "";
                        for (Transition event : events) {
                            name = enl.getMathModel().getName(event);
                            fromStateToStateConnection.setName(name);
                        }
                        U.add(name);
                        DataOfSTS qToq = new DataOfSTS(U, new Pair<>(q0, q1));
                        fristStepSecondForThin.add(qToq);
                        fristStepSecond.add(qToq);
                    } else if (events.size() > 1) { // if a step is like: {e,f}
                        String newStep = "{";
                        for (Transition event : events) {
                            n++;
                            newStep = newStep + enl.getMathModel().getName(event);
                            if (events.size() > n) {
                                newStep = newStep + ",";
                            } else {
                                newStep = newStep + "}";
                            }
                        }
                        Set<String> U = new HashSet<>();
                        U.add(newStep);
                        fristStepSecond.add(new DataOfSTS(U, new Pair<>(q0, q1)));
                        fromStateToStateConnection.setName(newStep);
                    }
                }
            }
        }
    }

    public VisualENL getENL() {
        return enl;
    }

    public VisualSTS getSTS() {
        return vsts;
    }

    public ArrayList<DataOfSTS> qToqForThin() {
        return fristStepSecondForThin;
    }

    public ArrayList<DataOfSTS> qToqForNotThin() {
        return fristStepSecond;

    }
}
