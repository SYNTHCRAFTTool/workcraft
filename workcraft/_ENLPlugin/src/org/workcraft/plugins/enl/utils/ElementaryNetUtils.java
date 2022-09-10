package org.workcraft.plugins.enl.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.ENLModel;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;

public class ElementaryNetUtils {
    static Map<String, Integer> eventsLocalities = new HashMap<>();

    // Return HashMap contains all conditions and their number of tokens.
    public static HashMap<Condition, Integer> getMarking(ENLModel net) {
        HashMap<Condition, Integer> marking = new HashMap<>();
        for (Condition condition : net.getConditions()) {
            marking.put(condition, condition.getTokens());
        }
        return marking;
    }

    public static void setMarking(ENLModel net, HashMap<Condition, Integer> marking) {
        for (Condition condition : net.getConditions()) {
            Integer count = marking.get(condition);
            if (count != null) {
                condition.setTokens(count);
            }
        }
    }

    public static boolean fireTrace(ENLModel elementaryNet, Trace trace) {
        for (String ref : trace) {
            Node node = elementaryNet.getNodeByReference(ref);
            if (node instanceof Transition) {
                Transition transition = (Transition) node;
                ENL net = (ENL) elementaryNet;
//                Set<Set<Transition>> controlEnabledSteps = ElementaryNetUtils.getControlEnabledSteps(net,
//                        ElementaryNetUtils.getResourceEnabledSteps(net, ElementaryNetUtils.getPotentailSteps(net)));
                Set<Set<Transition>> controlEnabledSteps = getControlEnabledSteps(net,
                        removeEmptySet(getControled(net, getEnabledTransitions(net), getConflict(net))));
                Set<Transition> step = new HashSet<>();
                /*
                 * for(Set<Transition> c: controlEnabledSteps) { for(Transition tt: c) {
                 * System.out.print(net.getName(tt)); } System.out.println(""); }
                 */
                if (!controlEnabledSteps.isEmpty()) {
                    for (Set<Transition> controlEnabledStep : controlEnabledSteps) {
                        if (controlEnabledStep.contains(transition)) {
                            step = controlEnabledStep;
                        }
                    }
                    for (Transition t : step) {
                        elementaryNet.fire(t);
                    }
                } else {
                    LogUtils.logError("Trace transition '" + ref + "' is not enabled.");
                    return false;
                }
            } else {
                LogUtils.logError("Trace transition '" + ref + "' cannot be found.");
                return false;
            }
        }
        return true;
    }

    // Get all enabled transitions that all their post-conditions have tokens and
    // all their pre-conditions do not have tokens.
    public static Set<Transition> getEnabledTransitions(ENL net) {
        Set<Transition> result = new HashSet<>();
        for (Transition transition : net.getTransitions()) {
            if (net.isEnabled(transition)) {
                result.add(transition);
//                System.out.print(net.getName(transition) + "   ");
            }
        }
//        System.out.println();
//        System.out.println(result.size());
        return result;
    }

    // Return set of all sets of localities. May cause lots of garbage.
    public static Set<Set<Integer>> powerSetOfLocalities(Set<Integer> originalSet) {
        Set<Set<Integer>> sets = new HashSet<Set<Integer>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        List<Integer> list = new ArrayList<Integer>(originalSet);
        Integer head = list.get(0);
        Set<Integer> rest = new HashSet<Integer>(list.subList(1, list.size()));
        for (Set<Integer> set : powerSetOfLocalities(rest)) {
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    // Return set of all non-empty sets of non-conflicting events. May cause lots of
    // garbage.
    public static Set<Set<Transition>> getControled(ENL net, Set<Transition> set2,
            Map<Transition, Set<Transition>> eWithEconfli) {
        Set<Set<Transition>> sets = new HashSet<Set<Transition>>();
        if (set2.isEmpty()) {
            sets.add(new HashSet<Transition>());
            return sets;
        }
        List<Transition> list = new ArrayList<Transition>(set2);
        Transition head = list.get(0);
        Set<Transition> rest = new HashSet<Transition>(list.subList(1, list.size()));
        for (Set<Transition> set : getControled(net, rest, eWithEconfli)) {
            Set<Transition> newSet = new HashSet<Transition>();
            newSet.add(head);
            newSet.addAll(set);
            if (areEventsConflicted(net, head, newSet, eWithEconfli)) {
                sets.add(set);
            } else {
                sets.add(newSet);
                sets.add(set);
            }
        }
//        System.out.println(sets.size());

        return sets;
    }

    // Check if the newSet has events their q' is the same of head's q, or verse.
    public static boolean areEventsConflicted(ENL net, Transition head, Set<Transition> newSet,
            Map<Transition, Set<Transition>> eWithEconfli) {
        boolean result = false;
        Set<Transition> events = eWithEconfli.get(head);
        Set<Transition> headInString = new HashSet<>();
        headInString.add(head);
        if (!headInString.equals(newSet)) {
            if (!events.isEmpty()) {
                for (Transition step : events) {
                    // Check if newSet has any event that cannot be with e in the in or out of reg.
                    if (newSet.contains(step)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    // This method used to build a map to link each event with its conflicting
    // events
    public static Map<Transition, Set<Transition>> getConflict(ENL net) {
        Map<Transition, Set<Transition>> eWithEconfli = new HashMap<>();
        Collection<Transition> transitions = net.getTransitions();
//        Collection<Transition> transitions = getEnabledTransitions(net);
        for (Transition t : transitions) {
            Set<Transition> events = new HashSet<>();
            events = getConflictingEvents(net, t, transitions, eWithEconfli);
            eWithEconfli.put(t, events);
        }

//        for (Entry<Transition, Set<Transition>> entry : eWithEconfli.entrySet()) {
//            System.out.print(net.getName(entry.getKey()) + "   ");
//            for (Transition e: entry.getValue()) {
//                System.out.print(net.getName(e) + " ");
//            }
//            System.out.println();
//        }
        return eWithEconfli;
    }

    public static Set<Transition> getConflictingEvents(ENL net, Transition t, Collection<Transition> transitions,
            Map<Transition, Set<Transition>> eWithEconfli) {
        Set<Transition> events = new HashSet<>();
        for (Transition t1 : transitions) {
            // it can not be in conflict with itself.
            if (t != t1) {
                if (eWithEconfli.containsKey(t1)) {
                    if (eWithEconfli.get(t1).contains(t)) {
                        // So, they already checked and there is conflict between them.
                        events.add(t1);
                        continue;
                    } else {
                        // So, they already checked and there is no conflict between them.
                        continue;
                    }
                } else {
                    boolean check = false;
                    for (Node n1 : net.getPreset(t)) {
                        Condition c1 = (Condition) n1;
                        for (Node n2 : net.getPreset(t1)) {
                            Condition c2 = (Condition) n2;
                            if (c1.equals(c2)) {
                                check = true;
                            }
                        }
                    }
                    for (Node n3 : net.getPostset(t)) {
                        Condition c3 = (Condition) n3;
                        for (Node n4 : net.getPostset(t1)) {
                            Condition c4 = (Condition) n4;
                            if (c3.equals(c4)) {
                                check = true;
                            }
                        }
                    }
                    for (Node n5 : net.getPreset(t)) {
                        Condition c5 = (Condition) n5;
                        for (Node n6 : net.getPostset(t1)) {
                            Condition c6 = (Condition) n6;
                            if (c5.equals(c6)) {
                                check = true;
                            }
                        }
                    }
                    for (Node n7 : net.getPostset(t)) {
                        Condition c7 = (Condition) n7;
                        for (Node n8 : net.getPreset(t1)) {
                            Condition c8 = (Condition) n8;
                            if (c7.equals(c8)) {
                                check = true;
                            }
                        }
                    }
                    if (check) {
                        events.add(t1);
                    }
                }
            }
        }
        return events;
    }

    /*
     * Get control enabled steps at a case c which is a resource enabled step at c
     * that cannot be enlarged by any new event, which sharing existing events of
     * this step.
     */
    public static Set<Set<Transition>> getControlEnabledSteps(ENL net, Set<Set<Transition>> allResourceEnabledSteps) {
        Set<Set<Transition>> resourceEnabledSteps = allResourceEnabledSteps;
        Set<Set<Transition>> controlEnabledSteps = new HashSet<Set<Transition>>();
        Set<Integer> localities = getAllLocalities(resourceEnabledSteps);
        if (localities.size() == 1) {
            controlEnabledSteps = getAllControlEnabledSteps(resourceEnabledSteps);

        } else if (localities.size() > 1) {

            Set<Set<Integer>> setsOfLocalities = powerSetOfLocalities(localities);

            for (Set<Integer> setOfLocalities : setsOfLocalities) {
                Set<Set<Transition>> resourceEnabledStepsWithLocality = getResourceEnabledStepsWithLocality(
                        setOfLocalities, resourceEnabledSteps);
                if (!resourceEnabledStepsWithLocality.isEmpty() || !resourceEnabledStepsWithLocality.equals(null)) {
                    controlEnabledSteps.addAll(getAllControlEnabledSteps(resourceEnabledStepsWithLocality));
                }
            }
        }
        /*
         * if (!controlEnabledSteps.isEmpty()) {
         * 
         * System.out.println("Control enabled steps: " + controlEnabledSteps.size() +
         * " sets"); int k = 0; System.out.print("{"); for (Set<Transition> t :
         * controlEnabledSteps) { k = ++k; System.out.print("{"); for (Transition tt :
         * t) { System.out.print(tt.getName(net)); } System.out.print("}");
         * System.out.print((k < controlEnabledSteps.size()) ? "," : ""); }
         * System.out.println("}"); }
         */
        return controlEnabledSteps;
    }

    // Return set of all enlarged sets.
    public static Set<Set<Transition>> getAllControlEnabledSteps(Set<Set<Transition>> resourceEnabledSteps) {
        boolean check = true;
        Set<Set<Transition>> controlEnabledSteps = new HashSet<Set<Transition>>();
        for (Set<Transition> setOfEnabledEvents : resourceEnabledSteps) {
            Set<Transition> t1 = setOfEnabledEvents;
            Set<Transition> enlargedSet = new HashSet<>();
            for (Set<Transition> setOfEvents : resourceEnabledSteps) {
                if ((!t1.equals(setOfEvents)) & (t1.size() < setOfEvents.size())) {
                    if (setOfEvents.containsAll(t1)) {
                        check = false;
                        if (setOfEvents.size() > enlargedSet.size()) {
                            enlargedSet = setOfEvents;
                        }
                    }
                }
            }
            if (check) {
                controlEnabledSteps.add(t1);
            }
            if (!enlargedSet.isEmpty()) {
                controlEnabledSteps.add(enlargedSet);
            }
            check = true;
        }
        return controlEnabledSteps;
    }

    // Return set of all localities.
    public static Set<Integer> getAllLocalities(Set<Set<Transition>> allResourceEnabledSteps) {
        Set<Integer> localities = new HashSet<>();
        for (Set<Transition> resourceEnabledSteps : allResourceEnabledSteps) {
            for (Transition t : resourceEnabledSteps) {
                localities.add(t.getLocality());
            }
        }
        return localities;
    }

    // Return set of localities of one set of events.
    public static Set<Integer> getLocalitiesOfSetOfEvents(Set<Transition> resourceEnabledStep) {
        Set<Integer> localities = new HashSet<>();
        for (Transition event : resourceEnabledStep) {
            localities.add(event.getLocality());
        }
        return localities;
    }

    /*
     * Return all resource enabled steps with specific set of localities (It may be
     * 1 or more).
     */
    public static Set<Set<Transition>> getResourceEnabledStepsWithLocality(Set<Integer> l,
            Set<Set<Transition>> allResourceEnabledSteps) {
        Set<Set<Transition>> resourceEnabledEventsWithLocality = new HashSet<>();
        for (Set<Transition> setOfEvents : allResourceEnabledSteps) {
            if (l.equals(getLocalitiesOfSetOfEvents(setOfEvents))) {
                resourceEnabledEventsWithLocality.add(setOfEvents);
            }
        }
        return resourceEnabledEventsWithLocality;
    }

    public static Set<Set<Transition>> removeEmptySet(Set<Set<Transition>> originalSet) {
//        Set<Set<Transition>> setOfSets = originalSet;
//        if (originalSet.stream().anyMatch(o -> o.isEmpty() || o.equals(null))) {
//            originalSet.remove(o);
//        }
        for (Iterator<Set<Transition>> allSets = originalSet.iterator(); allSets.hasNext();) {
            Set<Transition> setOfEvents = allSets.next();
            // Remove the Ø set. We need 2^n -1.
            if ((setOfEvents.isEmpty()) || ((setOfEvents.equals(null))))
                allSets.remove();
        }
        return originalSet;

    }

    public static Set<Condition> getConditionsWithToken(ENL net) {
        Set<Condition> setOfStates = new HashSet<>();
        for (Condition c : net.getConditions()) {
            if (c.getTokens() == 1) {
                setOfStates.add(c);
            }
        }
        return setOfStates;
    }

    public static void getSetOfSTS(ENL net, ArrayList<Set<Condition>> conditions,
            ArrayList<DataOfStepTransitionSystem> dataOfSTS, Map<Transition, Set<Transition>> conflictedEvents) {
        boolean check = false;
//        if (dataOfSTS.isEmpty()) {
//            dataOfSTS.add(new DataOfStepTransitionSystem(new Pair<>(null, null), null));
//            return;
//        }
//        Set<Set<Transition>> controlEnabledSteps = ElementaryNetUtils.getControlEnabledSteps(net,
//                ElementaryNetUtils.getResourceEnabledSteps(net, ElementaryNetUtils.getPotentailSteps(net)));
        Set<Set<Transition>> controlEnabledSteps = getControlEnabledSteps(net,
                removeEmptySet(getControled(net, getEnabledTransitions(net), conflictedEvents)));
        HashMap<Condition, Integer> initialMark = getMarking(net);

        // object to link q---->q' by event
        DataOfStepTransitionSystem transitionFromSToS;
        Set<Condition> state = new HashSet<>();
        state = getConditionsWithToken(net);
        // Collecting a set of all states to generate the STS (reachability graph).
        if (!state.isEmpty())
            conditions.add(state);
        if ((!controlEnabledSteps.isEmpty()) || (!(controlEnabledSteps.equals(null)))) {
            for (Set<Transition> c : controlEnabledSteps) {
                for (Transition t1 : c) {
                    net.fire(t1);
                }
                Set<Condition> setOfState = getConditionsWithToken(net);
                if (!state.isEmpty() & !setOfState.isEmpty()) {
                    transitionFromSToS = new DataOfStepTransitionSystem(new Pair<>(state, setOfState), c);
                    dataOfSTS.add(transitionFromSToS);
                }
                if (conditions.contains(setOfState))
                    check = true;
                if (!check) {
                    getSetOfSTS(net, conditions, dataOfSTS, conflictedEvents);
                }
                if (controlEnabledSteps.size() > 1) {
                    for (Transition t2 : c) {
                        net.unFire(t2);
                    }
                }
                check = false;
            }
        }
        setMarking(net, initialMark);
    }

    public static Set<Condition> initialConditions(ENL net) {
        return getConditionsWithToken(net);
    }

    // Return all conditions and transitions going out of them.
    public static HashMap<Condition, HashSet<Transition>> calcConditionOutgoingEventsMap(final ENL enl) {
        HashMap<Condition, HashSet<Transition>> conditionOutgoingEvents = new HashMap<>();
        // collect all conditions in the net.
        for (Condition condition : enl.getConditions()) {
            HashSet<Transition> events = new HashSet<>();
            conditionOutgoingEvents.put(condition, events);
        }
        // collect transition and find its condition to add it to it.
        for (Transition transition : enl.getTransitions()) {
            for (MathConnection c : enl.getConnections(transition)) {
                if (transition == c.getSecond()) {
                    Condition fromCondition = (Condition) c.getFirst();
                    if (fromCondition != null) {
                        HashSet<Transition> events = conditionOutgoingEvents.get(fromCondition);
                        events.add(transition);
                    }
                }
            }
        }
        return conditionOutgoingEvents;
    }

    public static HashMap<Condition, HashSet<Transition>> calcConditionIncommingEventsMap(final ENL enl) {
        HashMap<Condition, HashSet<Transition>> stateIncommingEvents = new HashMap<>();
        for (Condition condition : enl.getConditions()) {
            HashSet<Transition> events = new HashSet<>();
            stateIncommingEvents.put(condition, events);
        }
        for (Transition transition : enl.getTransitions()) {
            // Condition toState = (Condition) transition.getSecond();
            for (MathConnection c : enl.getConnections(transition)) {
                if (transition == c.getFirst()) {
                    Condition toCondition = (Condition) c.getSecond();
                    if (toCondition != null) {
                        HashSet<Transition> events = stateIncommingEvents.get(toCondition);
                        events.add(transition);
                    }
                }
            }
        }
        return stateIncommingEvents;
    }

    public static boolean checkSoundness(ENLModel enl, boolean ask) {
        String msg = "";
        System.out.print("");
        Set<String> hangingTransitions = new HashSet<>();
        Set<String> transitionsNoPre = new HashSet<>();
        Set<String> transitionsNoPost = new HashSet<>();
        for (Transition transition : enl.getTransitions()) {
            if (enl.getPreset(transition).isEmpty() & enl.getPostset(transition).isEmpty()) {
                String ref = enl.getNodeReference(transition);
                hangingTransitions.add(ref);
            }
            if (enl.getPreset(transition).isEmpty() & !enl.getPostset(transition).isEmpty()) {
                String ref = enl.getNodeReference(transition);
                transitionsNoPre.add(ref);
            }
            if (!enl.getPreset(transition).isEmpty() & enl.getPostset(transition).isEmpty()) {
                String ref = enl.getNodeReference(transition);
                transitionsNoPost.add(ref);
            }
        }
        if (!hangingTransitions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected event", hangingTransitions);
        }
        if (!transitionsNoPre.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* events without pre-conditions", transitionsNoPre);
        }
        if (!transitionsNoPost.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* events without post-conditions", transitionsNoPost);
        }

        Set<String> hangingconditiones = new HashSet<>();
        Set<String> deadConditions = new HashSet<>();
        for (Condition condition : enl.getConditions()) {
            if (enl.getPreset(condition).isEmpty()) {
                String ref = enl.getNodeReference(condition);
                if (enl.getPostset(condition).isEmpty()) {
                    hangingconditiones.add(ref);
                } // else if (condition.getTokens() == 0) {
                  // deadConditions.add(ref);
                  // }
            }
        }
        if (!hangingconditiones.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected condition", hangingconditiones);
        }
        if (!deadConditions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Dead condition", deadConditions);
        }
        if (!msg.isEmpty()) {
            msg = "The enl-system has the following issues:" + msg;
            if (ask) {
                msg += "\n\n Proceed anyway?";
                return DialogUtils.showConfirmWarning(msg, "Model validation", false);
            } else {
                DialogUtils.showWarning(msg);
            }
        }
        if (msg.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static Set<Integer> getLocalities(ENL enl) {
        Collection<Transition> event = enl.getTransitions();
        Set<Integer> localities = new HashSet<>();
        for (Transition e : event) {
            localities.add(e.getLocality());
            eventsLocalities.put(e.getName(enl), e.getLocality());
        }
        return localities;
    }

    public static Map<Integer, Set<String>> getEventsLocalities(ENL enl) {
        Map<Integer, Set<String>> allEventsLocalities = new HashMap<>();
        for (Integer loc : getLocalities(enl)) {
            Set<String> eventsWithOneLoc = new HashSet<>();
            for (Map.Entry<String, Integer> eWithLoc : eventsLocalities.entrySet()) {
                if (eWithLoc.getValue() == loc) {
                    eventsWithOneLoc.add(eWithLoc.getKey());
                }
            }
            allEventsLocalities.put(loc, eventsWithOneLoc);
        }
        return allEventsLocalities;
    }

    public static Set<Set<String>> getColocationRelations(ENL enl) {
        Set<Set<String>> eventsOneLocalities = new HashSet<>();
        for (Entry<Integer, Set<String>> e : getEventsLocalities(enl).entrySet()) {
            eventsOneLocalities.add(e.getValue());
        }
        return eventsOneLocalities;
    }

    public static String getCoRe(ENL enl) {
        String msg = " = ";
        int k = 0;
        Set<Set<String>> allColoRelation = getColocationRelations(enl);
        for (Set<String> equClass : allColoRelation) {
            k++;
            String oneLoc = "{";
            int i = 0;
            for (String e : equClass) {
                i++;
                oneLoc = oneLoc + e + (i < equClass.size() ? "," : "");
            }
            oneLoc = oneLoc + "}";
            oneLoc = oneLoc + " \u00d7 " + oneLoc;
            if (oneLoc.length() > 20)
                oneLoc = oneLoc + "\n";
            msg = msg + oneLoc + (k < allColoRelation.size() ? " \u222A " : "");
        }
        return msg;
    }
}
