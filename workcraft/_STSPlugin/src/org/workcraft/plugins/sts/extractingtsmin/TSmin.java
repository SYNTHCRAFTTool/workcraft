package org.workcraft.plugins.sts.extractingtsmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.cobiningequivalentclasses.CobiningEquivalentClasses;
import org.workcraft.plugins.sts.utils.STSUtils;

public class TSmin {
    static Map<String, Integer> eventsWithLocalities = new HashMap<>();

    /**
     * Calculate min Steps q is the set of all minimal steps (w.r.t set inclusion)
     * belonging to all Steps q.
     **/
    public static Map<State, Set<Set<String>>> getMinStepsOfStates(STS sts,
            Map<State, Set<Set<String>>> allStepsOfStates, Set<State> states) {
        Map<State, Set<Set<String>>> minStepsq = new HashMap<>();
        for (State state : states) {
            Set<Set<String>> minStepsOfq = new HashSet<>();
            Set<Set<String>> allStepsq = allStepsOfStates.get(state);
            for (Set<String> step : allStepsq) {
                int count = 0;
                for (Set<String> s : allStepsq) {
                    if (!step.equals(s)) {
                        if (!step.containsAll(s)) {
                            count++;
                        } else {
                            break;
                        }
                    }
                }
                if (count == (allStepsq.size() - 1)) {
                    minStepsOfq.add(step);
                }
            }
            minStepsq.put(state, minStepsOfq);
        }
        return minStepsq;
    }

    // Calculate Eq is the union of all steps labelling arcs outgoing from q.
    public static Map<State, Set<String>> getEq(STS sts, Map<State, Set<Set<String>>> allStepsOfStates,
            Set<State> states) {
        Map<State, Set<String>> Eq = new HashMap<>();
        for (State state : states) {
            Set<String> Estate = new HashSet<>();
            for (Set<String> step : allStepsOfStates.get(state)) {
                Estate.addAll(step);
            }
            Eq.put(state, Estate);
        }
        return Eq;
    }

    public static Set<ArrayList<String>> cartesianProduct(Set<String> set) {
        Set<ArrayList<String>> result = new HashSet<>();
        for (String e : set) {
            for (String f : set) {
                ArrayList<String> pair = new ArrayList<>();
                pair.add(e);
                pair.add(f);
                result.add(pair);
            }
        }
        return result;
    }

    public static Set<ArrayList<String>> getTSq(Set<ArrayList<String>> allSteps, Set<Set<String>> minStepsOfq,
            Set<ArrayList<String>> EqEq) {
        Set<ArrayList<String>> result = new HashSet<>();
        Set<ArrayList<String>> minSteps = new HashSet<>();
        Set<ArrayList<String>> inEqEqOnly = new HashSet<>();
        for (Set<String> minSet : minStepsOfq) {
            minSteps.addAll(cartesianProduct(minSet));
        }
        result.addAll(minSteps);
        for (ArrayList<String> pair : EqEq) {
            if (!allSteps.contains(pair))
                inEqEqOnly.add(pair);
        }
        if (!inEqEqOnly.isEmpty())
            result.addAll(inEqEqOnly);
        return result;
    }

    public static boolean getTSmin(STS sts, int i) {
        boolean result = true;
        Set<ArrayList<String>> tsmin = new HashSet<>();
        Map<State, Set<ArrayList<String>>> tsqs = new HashMap<>();
        Set<State> allStates = new HashSet<>();
        Map<State, Set<ArrayList<String>>> qallSeps = new HashMap<>();
        Set<String> allEvents = sts.createEvents();
        Map<State, Set<ArrayList<String>>> allEqEq = new HashMap<>();
        allStates.addAll(sts.getStates());
        Map<State, Set<Set<String>>> allStepsStates = sts.getAllStepsOfStates(sts, allStates);
        Map<State, Set<Set<String>>> minStepsStates = getMinStepsOfStates(sts, allStepsStates, allStates);
        Map<State, Set<String>> EStates = getEq(sts, allStepsStates, allStates);
        for (State q : allStates) {
            Set<ArrayList<String>> EqEq = new HashSet<>();
            EqEq.addAll(cartesianProduct(EStates.get(q)));
            allEqEq.put(q, EqEq);
            Set<ArrayList<String>> allSteps = new HashSet<>();
            for (Set<String> allSet : allStepsStates.get(q)) {
                allSteps.addAll(cartesianProduct(allSet));
            }
            qallSeps.put(q, allSteps);
            Set<ArrayList<String>> tsq = getTSq(allSteps, minStepsStates.get(q), EqEq);
            tsqs.put(q, tsq);
            if (tsq != null)
                tsmin.addAll(tsq);
        }
        getReflexivity(allEvents, tsmin);
        Map<String, Set<String>> symmetry = getSymmetry(allEvents, tsmin);
        Map<String, Set<String>> transitive = getTransitive(symmetry, tsmin);
        if (checktsminToallq(tsmin, tsqs, allEqEq)) {
            Set<Set<String>> localities = new HashSet<>();
            Set<Set<String>> localities2 = new HashSet<>();
            for (Map.Entry<String, Set<String>> entry : transitive.entrySet()) {
                entry.getValue().add(entry.getKey());
                localities.add(entry.getValue());
            }
            if (i == 1) {
                localities2 = localities;
                System.out.print("The discovered co-location relation is " + "\u224F" + "^{ts}_{min} = {");
                int m = 0;
                for (ArrayList<String> pair : tsmin) {
                    System.out.print("(");
                    int t = 0;
                    m++;
                    for (String e : pair) {
                        t++;
                        System.out.print(e);
                        System.out.print(t < pair.size() ? "," : "");
                    }
                    System.out.print(")");
                    System.out.print(m < tsmin.size() ? "," : "");
                }
                System.out.println("}.");
                System.out.println();
                System.out.print("The set of equivalence classes of " + "\u224F" + "^{ts}_{min} is {");
                STSUtils.printSetOfStrings(localities);
                System.out.println("}. \n");
            } else if (i == 2) {
                Set<Set<Set<String>>> minColoNOPossi = new HashSet<>();
                System.out.println("Valid co-location relations computed from " + "\u224F"
                        + "^{ts}_{min} with the smallest number of equivalent classes (given as sets of equivalence classes unless it is "
                        + "\u224F" + "^{ts}_{min}) is ");
                minColoNOPossi = CobiningEquivalentClasses.computingMinColoRela2(sts, tsmin, qallSeps, allEqEq, EStates,
                        localities);
                for (Set<Set<String>> sets : minColoNOPossi) {
                    for (Set<String> set : sets) {
                        localities2.add(set);
                    }
                }
            } else if (i == 3) {
                Set<Set<Set<String>>> minBalancedColoNOPossi = new HashSet<>();
                minBalancedColoNOPossi = CobiningEquivalentClasses
                        .computingMinBalancedColoRela(CobiningEquivalentClasses.computingMinColoRela2(sts, tsmin,
                                qallSeps, allEqEq, EStates, localities), localities);
                for (Set<Set<String>> sets : minBalancedColoNOPossi) {
                    for (Set<String> set : sets) {
                        localities2.add(set);
                    }
                }
            }
            // Set localities for events using \u224F ^{ts}_{min}
            int loc = 0;
            for (Set<String> locality : localities2) {
                loc++;
                for (String event : locality)
                    eventsWithLocalities.put(event, loc);
            }
            STS.setEventsLocalities(eventsWithLocalities);
        } else {
            result = false;
        }
//        checkComputingProcedure(sts, tsmin, qallSeps, allEqEq);
        return result;
    }

    public static boolean checktsminToallq(Set<ArrayList<String>> tsmin, Map<State, Set<ArrayList<String>>> tsqs,
            Map<State, Set<ArrayList<String>>> EqEq) {
        boolean result = true;
        for (Map.Entry<State, Set<ArrayList<String>>> entry : tsqs.entrySet()) {
            Set<ArrayList<String>> tsminEqEq = tsmin.stream().filter(EqEq.get(entry.getKey())::contains)
                    .collect(Collectors.toSet());
            if (entry.getValue().equals(tsminEqEq)) {
                continue;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    // To check reflexive events.
    public static Map<String, Integer> getReflexivity(Set<String> allEvents, Set<ArrayList<String>> tsmin) {
        Map<String, Integer> relexiveEvents = new HashMap<>();
        for (String e : allEvents) {
            ArrayList<String> checkSet = new ArrayList<>();
            checkSet.add(e);
            checkSet.add(e);
            if (tsmin.contains(checkSet)) {
                relexiveEvents.put(e, 1);
            } else {
                relexiveEvents.put(e, 0);
            }
        }
        return relexiveEvents;
    }

    // To check reflexive events.
    public static Map<String, Set<String>> getSymmetry(Set<String> allEvents, Set<ArrayList<String>> tsmin) {
        Map<String, Set<String>> symmetriveEvents = new HashMap<>();
        for (String e : allEvents) {
            Set<String> saymmE = new HashSet<>();
            for (String f : allEvents) {
                if (!e.equals(f)) {
                    ArrayList<String> a = new ArrayList<>();
                    a.add(e);
                    a.add(f);
                    if (tsmin.contains(a)) {
                        ArrayList<String> b = new ArrayList<>();
                        b.add(f);
                        b.add(e);
                        if (tsmin.contains(b)) {
                            saymmE.add(f);
                        }
                    }
                }
            }
            symmetriveEvents.put(e, saymmE);
        }
        return symmetriveEvents;
    }

    public static Map<String, Set<String>> getTransitive(Map<String, Set<String>> symmetry,
            Set<ArrayList<String>> tsmin) {
        Map<String, Set<String>> transitiveEvents = new HashMap<>();
        Set<ArrayList<String>> missingPairs = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : symmetry.entrySet()) {
            Set<String> missingEvents = new HashSet<>();
            for (String event : entry.getValue()) {
                if (symmetry.containsKey(event)) {
                    if (symmetry.get(event).contains(entry.getKey())) {
                        for (String events : symmetry.get(event)) {
                            if (!events.equals(entry.getKey())) {
                                ArrayList<String> a = new ArrayList<>();
                                a.add(entry.getKey());
                                a.add(events);
                                if (!tsmin.contains(a)) {
                                    missingPairs.add(a);
                                    missingEvents.add(events);
                                }
                            }
                        }
                    }
                }
            }
            missingEvents.addAll(entry.getValue());
            transitiveEvents.put(entry.getKey(), missingEvents);
        }
        if (missingPairs != null) {
            tsmin.addAll(missingPairs);
        }
        return transitiveEvents;
    }

    public static Map<String, Integer> getLocalities() {
        return eventsWithLocalities;
    }
}
