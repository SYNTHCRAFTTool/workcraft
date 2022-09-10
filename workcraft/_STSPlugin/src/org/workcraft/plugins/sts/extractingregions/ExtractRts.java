package org.workcraft.plugins.sts.extractingregions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.utils.DataOfSTS;
import org.workcraft.plugins.sts.utils.STSUtils;
import org.workcraft.utils.DialogUtils;

public class ExtractRts {

    static Map<Region, Region> rwithcomb = new HashMap<>();
    static ArrayList<DataOfSTS> stsData = new ArrayList<>();

    // Return set of all sets of events. May cause lots of garbage.
    public static Set<Set<String>> powerSetOfEventsForThin(STS sts, Set<String> originalSet,
            Map<String, Set<Set<String>>> connectedEvent) {
        Set<Set<String>> sets = new HashSet<Set<String>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<String>());
            return sets;
        }
        List<String> list = new ArrayList<String>(originalSet);
        String head = list.get(0);
        Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
        for (Set<String> set : powerSetOfEventsForThin(sts, rest, connectedEvent)) {
            Set<String> newSet = new HashSet<String>();
            newSet.add(head);
            newSet.addAll(set);
            if (areEventsConnected(sts, head, newSet, connectedEvent)) {
                sets.add(set);
            } else {
                sets.add(newSet);
                sets.add(set);
            }
        }
        return sets;
    }

    // Check if the newSet has events that their q' is the same of head's q, or
    // verse.
    public static boolean areEventsConnected(STS sts, String head, Set<String> newSet,
            Map<String, Set<Set<String>>> connectedEvent) {
        boolean result = false;
        Set<Set<String>> events = connectedEvent.get(head);
        Set<String> headInString = new HashSet<>();
        headInString.add(head);
        if (!headInString.equals(newSet)) {
            if (!events.isEmpty()) {
                for (Set<String> step : events) {
                    // Check if newSet has any event that cannot be with e in the in or out of reg.
                    if (newSet.containsAll(step)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    // Building a map to connect each e with any other event sharing q or q' states.
    public static Map<String, Set<Set<String>>> connectedEvents(STS sts) {
        Map<String, Set<Set<String>>> allConnectedEvents = new HashMap<>();
        ArrayList<DataOfSTS> allqToq = sts.getStepsWithqToq(sts);
        for (String event : sts.createEvents()) {
            Set<Set<String>> eventsConnected = new HashSet<>();
            allConnectedEvents.put(event, eventsConnected);
            Set<State> fristStatesOfEvent = new HashSet<>();
            Set<State> secondStatesOfEvent = new HashSet<>();
            // Collect all q of e in set and all its q' in another set.
            for (DataOfSTS qToq1 : allqToq) {
                if (qToq1.getStep().contains(event)) {
                    fristStatesOfEvent.add(qToq1.getPairOfState().getFirst());
                    secondStatesOfEvent.add(qToq1.getPairOfState().getSecond());
                }
            }
            /* Collect all events are connected to e, so they can not be together in same
               Potential in or out of region.*/
            for (DataOfSTS qToq2 : allqToq) {
                if (!qToq2.getStep().contains(event)) {
                    if ((fristStatesOfEvent.contains(qToq2.getPairOfState().getSecond()))
                            || (secondStatesOfEvent.contains(qToq2.getPairOfState().getFirst()))) {
                        for (String s : qToq2.getStep()) {
                            if (!s.equals(event)) {
                                Set<String> newString = new HashSet<>();
                                newString.add(event);
                                newString.add(s);
                                eventsConnected.add(newString);
                            }
                        }
                    }
                } else if (qToq2.getStep().size() > 1) {
                    // collects events with e in same step like {e,f}
                    for (String s : qToq2.getStep()) {
                        if (!s.equals(event)) {
                            Set<String> newString = new HashSet<>();
                            newString.add(event);
                            newString.add(s);
                            eventsConnected.add(newString);
                        }
                    }
                }
            }
        }
//        for (Entry<String, Set<Set<String>>> e : allConnectedEvents.entrySet()) {
//            System.out.println(e.getKey() + "---> " + e.getValue());
//        }
        return allConnectedEvents;
    }

    // Generate all potential in and out of (in, r ,out) for thin only.
    public static Set<Set<String>> generateAllinAndoutForThin(STS sts, Map<String, Set<Set<String>>> connectedEvent) {
        connectedEvent = connectedEvents(sts);
        Set<Set<String>> allinAndout = powerSetOfEventsForThin(sts, sts.createEvents(), connectedEvent);
        return allinAndout;
    }

    // Return set of all potential sets of states. May cause lots of garbage.
    public static Set<Set<State>> powerSetOfStatesForThin(STS sts, Collection<? extends Set<State>> originalSet) {
        Set<Set<State>> sets = new HashSet<Set<State>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<State>());
            return sets;
        }
        List<Set<State>> list = new ArrayList<Set<State>>(originalSet);
        Set<State> head = list.get(0);
        Set<Set<State>> rest = new HashSet<Set<State>>(list.subList(1, list.size()));
        for (Set<State> set : powerSetOfStatesForThin(sts, rest)) {
            Set<State> newSet = new HashSet<State>();
            newSet.addAll(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static boolean hasInOut(Set<Set<Set<String>>> toCheckInOut, Set<Set<String>> putInOut) {
        boolean result = false;
        for (Set<Set<String>> set : toCheckInOut) {
            if (set.equals(putInOut)) {
                result = true;
            }
        }
        return result;
    }

    public static Set<State> generateSandTSets(Set<String> inSet, Set<String> outSet, ArrayList<DataOfSTS> stsData,
            STS sts) {
        Set<State> SourceTargetSets = new HashSet<>();
        for (DataOfSTS arc : stsData) {
            boolean inResult = false;
            boolean outResult = false;
            for (String event : arc.getStep()) {
                if (inSet.contains(event)) {
                    inResult = true;
                }
                if (outSet.contains(event)) {
                    outResult = true;
                }
            }
            if (inResult) {
                SourceTargetSets.add(arc.getPairOfState().getSecond());
            }
            if (outResult) {
                SourceTargetSets.add(arc.getPairOfState().getFirst());
            }
        }
        return SourceTargetSets;
    }

    public static Set<State> generateFillerSet(Set<String> inAndOut, Set<State> f, Set<State> r,
            ArrayList<DataOfSTS> stsData, STS sts) {
        Set<State> fillerSet = new HashSet<>();
        for (DataOfSTS arc : stsData) {
            List<String> notInOrOut = arc.getStep().stream().filter(inAndOut::contains).collect(Collectors.toList());
            if (notInOrOut.isEmpty()) {
                if ((f.contains(arc.getPairOfState().getFirst())) & (!f.contains(arc.getPairOfState().getSecond()))) {
                    if (!r.contains(arc.getPairOfState().getSecond())) {
                        fillerSet.add(arc.getPairOfState().getSecond());
                        r.add(arc.getPairOfState().getSecond());
                    }
                }
                if ((f.contains(arc.getPairOfState().getSecond())) & (!f.contains(arc.getPairOfState().getFirst()))) {
                    if (!r.contains(arc.getPairOfState().getFirst())) {
                        fillerSet.add(arc.getPairOfState().getFirst());
                        r.add(arc.getPairOfState().getFirst());
                    }
                }
            }
        }
        return fillerSet;
    }

    public static Set<State> generater(Set<String> inSet, Set<String> outSet, ArrayList<DataOfSTS> stsData, STS sts) {
        Set<State> r = new HashSet<>();
        Set<State> SandTsets = new HashSet<>();
        Set<State> fillerSet = new HashSet<>();
        SandTsets = generateSandTSets(inSet, outSet, stsData, sts);
        r.addAll(SandTsets);
        Set<String> inAndOut = new HashSet<>();
        inAndOut.addAll(inSet);
        inAndOut.addAll(outSet);
        int counter = SandTsets.size();
        fillerSet = generateFillerSet(inAndOut, SandTsets, r, stsData, sts);
        while (r.size() > counter) {
            counter++;
            fillerSet = generateFillerSet(inAndOut, fillerSet, r, stsData, sts);
        }
        return r;
    }

    public static Map<String, Set<String>> eventsInSameSteps(STS sts, ArrayList<DataOfSTS> stsData) {
        Map<String, Set<String>> connectedEvents = new HashMap<>();
        for (String event : sts.createEvents()) {
            Set<String> events = new HashSet<>();
            for (DataOfSTS qToq2 : stsData) {
                if (qToq2.getStep().contains(event)) {
                    for (String s : qToq2.getStep()) {
                        if (!s.equals(event)) {
                            events.add(s);
                        }
                    }
                }
            }
            if (!events.isEmpty())
                connectedEvents.put(event, events);
        }
        return connectedEvents;
    }

    public static boolean validPair(Set<String> inSet, Set<String> outSet, Map<String, Set<String>> connectedEvent) {
        boolean result = true;
        if (!inSet.isEmpty()) {
            for (String event : inSet) {
                if (connectedEvent.containsKey(event)) {
                    for (String e : connectedEvent.get(event)) {
                        if (!outSet.isEmpty()) {
                            if (outSet.contains(e)) {
                                result = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static Set<Region> extractingNonTrivialRegions(STS sts) {
        Set<Region> regions = new HashSet<Region>();
        Collection<State> Q = sts.getStates();
        ArrayList<DataOfSTS> stsData1 = sts.getStepsWithqToq(sts);
        
        Set<String> events = sts.createEvents();
//        STS.setLocalities(events);
        
        if (!STS.getArcsWithIgnoringThichArcs(sts, stsData1,events)) {
            stsData = sts.getStepsWithqToqForThin(sts);
//            System.out.println("Algo 1* " + stsData.size());
        } else {
            stsData = NotThinSystem.removeNotAllowedThickArcs(stsData1, sts);
//            System.out.println( "Algo1 1 " + stsData1.size());
//            System.out.println( "Algo 1 " + stsData.size());
        }

        Map<String, Set<Set<String>>> connectedEvent = new HashMap<>();
        Map<String, Set<String>> connectedEvents = new HashMap<>();

        Set<Set<String>> potentialInOutSets = generateAllinAndoutForThin(sts, connectedEvent);
        Set<Set<Set<String>>> toCheckInOut = new HashSet<>();
        Set<Set<String>> inSets = potentialInOutSets;
        Set<Set<String>> outSets = potentialInOutSets;
        connectedEvents = eventsInSameSteps(sts, stsData1);
        for (Set<String> inSet : inSets) {
            for (Set<String> outSet : outSets) {
                if (!inSet.isEmpty() || !outSet.isEmpty()) {
                    List<String> inout = inSet.stream().filter(outSet::contains).collect(Collectors.toList());
                    if (inout.isEmpty()) {
                        if (validPair(inSet, outSet, connectedEvents)) {
                            Set<State> r = new HashSet<>();
                            Set<Set<String>> putInOut = new HashSet<>();
                            putInOut.add(inSet);
                            putInOut.add(outSet);
                            if (toCheckInOut.isEmpty()) {
                                toCheckInOut.add(putInOut);
                                r.addAll(generater(inSet, outSet, stsData, sts));
                            } else {
                                if (!hasInOut(toCheckInOut, putInOut)) {
                                    toCheckInOut.add(putInOut);
                                    r.addAll(generater(inSet, outSet, stsData, sts));
                                }
                            }
                            if (!r.isEmpty()) {
                                Region reg = new Region(inSet, r, outSet);
                                if (STSUtils.checkFiveConditionsR(stsData, reg, sts)) {
                                    regions.add(reg);
                                    Region rCom = getRegComplement(reg, sts, Q);
                                    regions.add(rCom);
                                    rwithcomb.put(reg,rCom);
                                }
                            }
                        }
                    }
                }
            }
        }
//       System.out.println("The all non-trivial regions are: " );
//        printRegions(sts, regions);
//        System.out.println("-------------------------------");
        return regions;
    }

    // Generate complement r'=(out,Q\r,in) of r=(in,r,out).
    public static Region getRegComplement(Region reg, STS sts) {
        Set<State> rOfComplementReg = new HashSet<>();
        // to get Q\r (states not in reg r).
        for (State s : sts.getStates()) {
            if (!reg.getRinRegion().contains(s)) {
                rOfComplementReg.add(s);
            }
        }
        Region r = new Region(reg.getEventsLeavingR(), rOfComplementReg, reg.getEventsEnteringR());
        return r;
    }
    
    // Generate complement r'=(out,Q\r,in) of r=(in,r,out).
    public static Region getRegComplement(Region reg, STS sts, Collection<State> Q) {
        Set<State> rOfComplementReg = new HashSet<>();
        // to get Q\r (states not in reg r).
        for (State s : Q) {
            if (!reg.getRinRegion().contains(s)) {
                rOfComplementReg.add(s);
            }
        }
        Region r = new Region(reg.getEventsLeavingR(), rOfComplementReg, reg.getEventsEnteringR());
        return r;
    }

    // Return map of each region with its complement.
    public static Map<Region, Region> getallRegsWithCom(){
        return rwithcomb;
    }

    // Print all non-trivial regions Ri = (in, r, out).
    public static void printRegions(STS sts, Set<Region> regions) {
        int k = 0;
        if (!regions.isEmpty()) {
//            System.out.println("The non-trivial regions are: ");
            for (Region reg : regions) {
                int i = 0;
                int j = 0;
                int l = 0;
                System.out.print("r" + k++ + " = ({");
                for (String inE : reg.getEventsEnteringR()) {
                    i++;
                    if (!inE.isEmpty()) {
                        System.out.print(inE);
                        System.out.print(i < reg.getEventsEnteringR().size() ? "," : "");
                    }
                }
                System.out.print("},{");
                for (State s : reg.getRinRegion()) {
                    j++;
                    System.out.print(sts.getName(s));
                    System.out.print(j < reg.getRinRegion().size() ? "," : "");
                }
                System.out.print("},{");
                for (String outE : reg.getEventsLeavingR()) {
                    l++;
                    if (!outE.isEmpty()) {
                        System.out.print(outE);
                        System.out.print(l < reg.getEventsLeavingR().size() ? "," : "");
                    }
                }
                System.out.println("})");
            }
        } else {
            DialogUtils.showWarning("There are no non trivial regions of this STS.");

        }
    }
    
    public static ArrayList<DataOfSTS> getStsData() {
        return stsData;
    }

    // Print all non-trivial regions Ri = (in, r, out).
    public static void printRegion(STS sts, Region reg) {
                int i = 0;
                int j = 0;
                int l = 0;
                System.out.print(" = ({");
                for (String inE : reg.getEventsEnteringR()) {
                    i++;
                    if (!inE.isEmpty()) {
                        System.out.print(inE);
                        System.out.print(i < reg.getEventsEnteringR().size() ? "," : "");
                    }
                }
                System.out.print("},{");
                for (State s : reg.getRinRegion()) {
                    j++;
                    System.out.print(sts.getName(s));
                    System.out.print(j < reg.getRinRegion().size() ? "," : "");
                }
                System.out.print("},{");
                for (String outE : reg.getEventsLeavingR()) {
                    l++;
                    if (!outE.isEmpty()) {
                        System.out.print(outE);
                        System.out.print(l < reg.getEventsLeavingR().size() ? "," : "");
                    }
                }
                System.out.print("})");            
        } 
}
