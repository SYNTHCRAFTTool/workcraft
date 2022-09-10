package org.workcraft.plugins.sts.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.types.Pair;

public class STSUtils {
    // Check if STS is thin or not.
    public static boolean isThin(STS sts) {
        boolean result = false;
        int count = 0;
        ArrayList<DataOfSTS> stsData = sts.getStepsWithqToq(sts);
        Set<Set<String>> allSteps = new HashSet<>();
        allSteps = getAllSteps(stsData, sts);
        Set<String> events = sts.createEvents();
        for (String event : events) {
            Set<String> s = new HashSet<>();
            s.add(event);
            if (allSteps.contains(s)) {
                count++;
            }
        }
        if (count == events.size()) {
            result = true;
        }
        return result;
    }

    // Collect all steps of the sts.
    public static Set<Set<String>> getAllSteps(ArrayList<DataOfSTS> stsData, STS sts) {
        Set<Set<String>> allSteps = new HashSet<>();
        for (DataOfSTS step : stsData) {
            if (!step.getStep().isEmpty()) {
                allSteps.add(step.getStep());
            }
        }
        return allSteps;
    }

    // Check if r=(in,r,out) has complement r'=(out,Q\r,in). If not return false.
    public static boolean hasComplement(Region reg, Set<Region> nonTrivialRegions,
            Map<Region, Region> mapOfregsWithComplement, STS sts) {
        boolean hasComp = false;
        Set<State> rOfComplementReg = new HashSet<>();
        // to get Q\r (states not in reg r).
        for (State s : sts.getStates()) {
            if (!reg.getRinRegion().contains(s)) {
                rOfComplementReg.add(s);
            }
        }
        // I should find a way to not check complement region again !!!!!!!!
        for (Region r : nonTrivialRegions) {
            if (!reg.equals(r)) {
                // to check if r = (in, r, out) has complement r' = (out, Q\r , in).
                if (reg.getEventsEnteringR().equals(r.getEventsLeavingR()) & r.getRinRegion().equals(rOfComplementReg)
                        & reg.getEventsLeavingR().equals(r.getEventsEnteringR())) {
                    hasComp = true;
                    mapOfregsWithComplement.put(reg, r);
                    break;
                }
            }
        }
        return hasComp;
    }

    // Check if r=(in,r,out) has complement r'=(out,Q\r,in). If not return false.
    public static boolean checkComplement(Region reg, Set<Region> nonTrivialRegions, ArrayList<DataOfSTS> qUq,
            Map<Region, Region> mapOfregsWithComplement, STS sts) {
        boolean hasComp = false;
        boolean alreadyExict = false;

        Set<State> rOfComplementReg = new HashSet<>();
        // to get Q\r (states not in reg r).
        for (State s : sts.getStates()) {
            if (!reg.getRinRegion().contains(s)) {
                rOfComplementReg.add(s);
            }
        }
        Region r = new Region(reg.getEventsLeavingR(), rOfComplementReg, reg.getEventsEnteringR());
        // to check if r = (in, r, out) has complement r' = (out, Q\r , in).
        if (checkFiveConditionsR(qUq, r, sts)) {
            mapOfregsWithComplement.put(reg, r);
            hasComp = true;
            // to check if nonTrivialRegions is already have this region.
            for (Region compReg : nonTrivialRegions) {
                if (r.getEventsEnteringR().equals(compReg.getEventsEnteringR())
                        & r.getRinRegion().equals(compReg.getRinRegion())
                        & r.getEventsLeavingR().equals(compReg.getEventsLeavingR())) {
                    alreadyExict = true;
                    break;
                }
            }
            if (!alreadyExict) {
                nonTrivialRegions.add(r);
            }
        }
        return hasComp;
    }

    // Check if r=(in,r,out) has complement r'=(out,Q\r,in). If not return false.
    public static boolean checkComplement2(Region reg, Set<Region> nonTrivialRegions, ArrayList<DataOfSTS> qUq,
            STS sts) {
        boolean hasComp = false;
        Set<State> rOfComplementReg = new HashSet<>();
        // to get Q\r (states not in reg r).
        for (State s : sts.getStates()) {
            if (!reg.getRinRegion().contains(s)) {
                rOfComplementReg.add(s);
            }
        }
        Region r = new Region(reg.getEventsLeavingR(), rOfComplementReg, reg.getEventsEnteringR());
        // to check if r = (in, r, out) has complement r' = (out, Q\r , in).
        if (checkFiveConditionsR(qUq, r, sts)) {
            hasComp = true;
            nonTrivialRegions.add(r);
        }
        return hasComp;
    }

    // This method is check R1, R2, R3, R4 and R5 for one possible region.
    public static boolean checkFiveConditionsR(ArrayList<DataOfSTS> qUq, Region r, STS sts) {
        boolean result = false;
        boolean R1 = false, R2 = false, R3 = false, R4 = false, R5 = false;
        boolean p = false, q = false;
        Region reg = r;
        int check = 0;
        for (DataOfSTS step : qUq) {
            /** Check R1 **/
            if (reg.getRinRegion().contains(step.getPairOfState().getFirst())
                    & !reg.getRinRegion().contains(step.getPairOfState().getSecond())) {
                p = true;
            }
            List<String> stepIn = step.getStep().stream().filter(reg.getEventsEnteringR()::contains)
                    .collect(Collectors.toList());
            List<String> stepOut = step.getStep().stream().filter(reg.getEventsLeavingR()::contains)
                    .collect(Collectors.toList());
            if (stepOut.size() == 1) {
                q = true;
            }
            if ((p & q) || (!p & !q) || (!p & q)) {
                R1 = true;
            } else {
                break;
            }
            p = false;
            q = false;
            /** Check R2 **/
            if (!reg.getRinRegion().contains(step.getPairOfState().getFirst())
                    & reg.getRinRegion().contains(step.getPairOfState().getSecond())) {
                p = true;
            }
            if (stepIn.size() == 1) {
                q = true;
            }
            if ((p & q) || (!p & !q) || (!p & q)) {
                R2 = true;
            } else {
                break;
            }
            p = false;
            q = false;
            /** Check R3 **/
            if ((!stepOut.isEmpty()) & (!stepOut.equals(null))) {
                p = true;
            }
            if (reg.getRinRegion().contains(step.getPairOfState().getFirst())
                    & !reg.getRinRegion().contains(step.getPairOfState().getSecond())) {
                q = true;
            }
            if ((p & q) || (!p & !q) || (!p & q)) {
                R3 = true;
            } else {
                break;
            }
            p = false;
            q = false;
            /** Check R4 **/
            if ((!stepIn.isEmpty()) & (!stepIn.equals(null))) {
                p = true;
            }
            if (!reg.getRinRegion().contains(step.getPairOfState().getFirst())
                    & reg.getRinRegion().contains(step.getPairOfState().getSecond())) {
                q = true;
            }
            if ((p & q) || (!p & !q) || (!p & q)) {
                R4 = true;
            } else {
                break;
            }
            p = false;
            q = false;
            /** Check R5 **/
            if (reg.getEventsEnteringR().isEmpty() & reg.getEventsLeavingR().isEmpty()) {
                p = true;
            }
            if ((reg.getRinRegion().size() == (sts.getStates().size())) || reg.getRinRegion().isEmpty()) {
                q = true;
            }
            if ((p & q) || (!p & !q) || (!p & q)) {
                R5 = true;
            } else {
                break;
            }
            if (R1 & R2 & R3 & R4 & R5) {
                check++;
            }
            R1 = false;
            R2 = false;
            R3 = false;
            R4 = false;
            R5 = false;
            p = false;
            q = false;
        }
        if (check == qUq.size()) {
            result = true;
        }
        return result;
    }

    // Collect all states of each step in one set. Ex: q0-{e}->q1, q9-{e}->q3.
    public static Set<DataOfSTS> buildDataOfSTS(Set<Set<String>> allSteps, ArrayList<DataOfSTS> stsData, STS sts) {
        Set<DataOfSTS> qUq = new HashSet<>();
        boolean isStepExist = false;
        for (Set<String> U : allSteps) {
            Set<State> fromq = new HashSet<>();
            Set<State> toq = new HashSet<>();
            for (DataOfSTS steps : qUq) {
                if (steps.getStep().equals(U)) {
                    isStepExist = true;
                }
            }
            if (!isStepExist) {
                for (DataOfSTS qToq : stsData) {
                    if (U.equals(qToq.getStep())) {
                        fromq.add(qToq.getPairOfState().getFirst());
                        toq.add(qToq.getPairOfState().getSecond());
                    }
                }
                qUq.add(new DataOfSTS((new Pair<>(fromq, toq)), U));
            }
            isStepExist = false;
        }
        return qUq;
    }

    /**
     * Calculate Rq for every state q. Rq is the set of all non-trivial regions
     * containing q (axiom A3).
     **/
    public static HashMap<State, HashSet<Region>> calcRqForEachState(Set<Region> nonTrivialRegions, STS sts) {
        HashMap<State, HashSet<Region>> Rq = new HashMap<State, HashSet<Region>>();
        for (State q : sts.getStates()) {
            HashSet<Region> reg = new HashSet<>();
            Rq.put(q, reg);
            for (Region r : nonTrivialRegions) {
                if (r.getRinRegion().contains(q)) {
                    reg.add(r);
                }
            }
        }
        return Rq;
    }

    public static void printSetOfStrings(Set<Set<String>> set) {
        int k = 0;
        for (Set<String> s : set) {
            System.out.print("{");
            int i = 0;
            k++;
            for (String e : s) {
                i++;
                System.out.print(e);
                System.out.print(i < s.size() ? "," : "");
            }
            System.out.print("}");
            System.out.print(k < set.size() ? "," : "");
        }
    }

    public static void printSetOfString(Set<String> set) {
        System.out.print("{");
        int i = 0;
        for (String e : set) {
            i++;
            System.out.print(e);
            System.out.print(i < set.size() ? "," : "");
        }
        System.out.print("}");
    }

    // Building a map to connect each e with any other event in conflicting with it.
    public static void getPerPosRegsOfEvents(STS sts, Set<Region> nonTrivialRegions,
            Map<String, Set<Region>> PreRegions, Map<String, Set<Region>> PostRegions) {
        for (String e : sts.createEvents()) {

            Set<Region> ePreRegions = new HashSet<>();
            PreRegions.put(e, ePreRegions);
            Set<Region> ePostRegions = new HashSet<>();
            PostRegions.put(e, ePostRegions);

            for (Region r : nonTrivialRegions) {
                // Collect 0f
                if (r.getEventsLeavingR().contains(e)) {
                    ePreRegions.add(r);
                }
                // Collect f0
                if (r.getEventsEnteringR().contains(e)) {
                    ePostRegions.add(r);
                }
            }
        }
    }

    // Get 0e and e0.
    public static void getePrePostRegions(Set<Region> nonTrivialRegions, String e, Set<Region> ePreRegions,
            Set<Region> ePostRegions) {

        for (Region r : nonTrivialRegions) {
            // Collect 0e
            if (r.getEventsLeavingR().contains(e)) {
                ePreRegions.add(r);
            }
            // Collect e0
            if (r.getEventsEnteringR().contains(e)) {
                ePostRegions.add(r);
            }
        }
    }

    // Building a map to connect each e with any other event in conflicting with it.
    public static boolean isENLSystem(STS sts, Set<Region> nonTrivialRegions, Map<String, Set<Region>> PreRegions,
            Map<String, Set<Region>> PostRegions) {
        boolean result = true;
        for (String e : sts.createEvents()) {
            Set<Region> ePreRegions = new HashSet<>();
            PreRegions.put(e, ePreRegions);
            Set<Region> ePostRegions = new HashSet<>();
            PostRegions.put(e, ePostRegions);
            for (Region r : nonTrivialRegions) {
                // Collect 0f
                if (r.getEventsLeavingR().contains(e)) {
                    ePreRegions.add(r);
                }
                // Collect f0
                if (r.getEventsEnteringR().contains(e)) {
                    ePostRegions.add(r);
                }
            }
            if (ePreRegions.isEmpty()) {
                result = false;
                break;
            }
            if (ePostRegions.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }

    // Building a map to connect each e with any other event in conflicting with it.
    public static boolean isENLSystemForRuleOne(STS sts, Set<Region> nonTrivialRegions) {
        boolean result = true;
        for (String e : sts.getEvents()) {
            Set<Region> ePreRegions = new HashSet<>();
            Set<Region> ePostRegions = new HashSet<>();
            for (Region r : nonTrivialRegions) {
                // Collect 0e
                if (r.getEventsLeavingR().contains(e)) {
                    ePreRegions.add(r);
                }
                // Collect e0
                if (r.getEventsEnteringR().contains(e)) {
                    ePostRegions.add(r);
                }
            }
            if ((ePreRegions.isEmpty()) || (ePostRegions.isEmpty())) {
                result = false;
                break;
            }
        }
        return result;
    }

    // Building a map to connect each e with any other event in conflicting with it.
    public static boolean isENLSystemForMethodII(STS sts, Set<Region> nonTrivialRegions) {
        boolean result = true;
        System.out.println("events: " + sts.getEvents().size());

        for (String e : sts.getEvents()) {
            Set<Region> ePreRegions = new HashSet<>();
            Set<Region> ePostRegions = new HashSet<>();
            for (Region r : nonTrivialRegions) {
                // Collect 0e
                if (r.getEventsLeavingR().contains(e)) {
                    ePreRegions.add(r);
                }
                // Collect e0
                if (r.getEventsEnteringR().contains(e)) {
                    ePostRegions.add(r);
                }
            }
            List<Region> ePreePost = ePreRegions.stream().filter(ePostRegions::contains).collect(Collectors.toList());
            if ((ePreRegions.isEmpty()) || (ePostRegions.isEmpty()) || !ePreePost.isEmpty()) {
                result = false;
                break;
            }
        }
        return result;
    }
}
