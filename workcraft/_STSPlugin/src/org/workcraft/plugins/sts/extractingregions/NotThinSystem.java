package org.workcraft.plugins.sts.extractingregions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.utils.DataOfSTS;

public class NotThinSystem {

    // Return set of all sets of events Of Not Thin way one. May cause lots of
    // garbage.
    public static Set<Set<String>> powerSetOfEventsOfNotThin1(Set<String> originalSet) {
        Set<Set<String>> sets = new HashSet<Set<String>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<String>());
            return sets;
        }
        List<String> list = new ArrayList<String>(originalSet);
        String head = list.get(0);
        Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
        for (Set<String> set : powerSetOfEventsOfNotThin1(rest)) {
            Set<String> newSet = new HashSet<String>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    // Return set of all sets of states. May cause lots of garbage.
    public static Set<Set<State>> powerSetOfStatesOfNotThin(Collection<State> originalSet) {
        Set<Set<State>> sets = new HashSet<Set<State>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<State>());
            return sets;
        }
        List<State> list = new ArrayList<State>(originalSet);
        State head = list.get(0);
        Set<State> rest = new HashSet<State>(list.subList(1, list.size()));
        for (Set<State> set : powerSetOfStatesOfNotThin(rest)) {
            Set<State> newSet = new HashSet<State>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public static <E> List<List<Set<String>>> generatePerm(List<Set<String>> original) {
        if (original.isEmpty()) {
            List<List<Set<String>>> result = new ArrayList<>();
            result.add(new ArrayList<>());
            return result;
        }
        Set<String> firstElement = original.remove(0);
        List<List<Set<String>>> returnValue = new ArrayList<>();
        List<List<Set<String>>> permutations = generatePerm(original);
        for (List<Set<String>> smallerPermutated : permutations) {
            for (int index = 0; index <= smallerPermutated.size(); index++) {
                
                List<Set<String>> temp = new ArrayList<>(smallerPermutated);
                temp.add(index, firstElement);
                returnValue.add(temp);
                if (permutations.size() == 2)
                    break;  
            }            
        }
        return returnValue;
    }

    public static ArrayList<Set<String>> splitIntoSubsteps(DataOfSTS thickArc, Set<Integer> localities, STS sts) {
        ArrayList<Set<String>> splitSubstep = new ArrayList<>();
        for (Integer locality : localities) {
            Set<String> oneLocality = new HashSet<>();
            for (String event : thickArc.getStep()) {
                if (STS.getELocs().get(event) == locality) {
                    oneLocality.add(event);
                }
            }
            if (!oneLocality.isEmpty())
                splitSubstep.add(oneLocality);
        }
        return splitSubstep;
    }

    // Check if it is an allowed thick arc or not.
    public static boolean isAllowedArc(DataOfSTS thickArc, List<List<Set<String>>> potentailSteps,
            ArrayList<DataOfSTS> stsData, STS sts) {
        boolean isAllowed = true;
        int countIn = 0;
        int countOut = 0;
        // potentailSteps like {{{c2c3}{p1}},{{p1}{c2c3}}}
        for (int i = 0; i < potentailSteps.size(); i++) {
            // substeps like {{{c2c3}{p1}}}}
            List<Set<String>> substeps = potentailSteps.get(i);
            // Check if the begin of this sequence is q of thick arc.
            if (arcsWithSameqSOfThickArc(substeps.get(0), thickArc, stsData, sts)) {
                // Check if the end of this sequence is q' of thick arc.
                if (arcsWithSameqTOfThickArc(substeps.get(substeps.size() - 1), thickArc, stsData, sts)) {
                    ArrayList<State> q = new ArrayList<>();
                    q.add(thickArc.getPairOfState().getFirst());
                    // Check 0.second with 1.first and 1.second with 2.first and so on.
                    for (int j = 0; j < (substeps.size() - 1); j++) {
                        Set<String> stepOfqTarget = substeps.get(j);
                        Set<String> stepOfqSource = substeps.get(j + 1);
                        if (checkStepsSequence(stepOfqTarget, stepOfqSource, q, stsData, sts)) {
                            countIn = countIn + 1;
                            continue;
                        } else {
                            break;
                        }
                    }
                    // {{c2c3}{p1}}} for set 1 to set 2 is 1. So, 2-1.
                    if (countIn == (substeps.size() - 1)) {
                        countOut++;
                    }
                }
            }
            countIn = 0;
        }
        if (countOut == potentailSteps.size()) {
            isAllowed = false;
        }
        return isAllowed;
    }

    public static boolean checkStepsSequence(Set<String> stepOfqTarget, Set<String> stepOfqSource, ArrayList<State> q,
            ArrayList<DataOfSTS> stsData, STS sts) {
        boolean result = false;
        DataOfSTS qTargetStep = null;
        for (DataOfSTS step : stsData) {
            if (step.getStep().equals(stepOfqTarget) & step.getPairOfState().getFirst().equals(q.get(0))) {
                qTargetStep = step;
                break;
            }
        }

        for (DataOfSTS step : stsData) {
            if (step.getPairOfState().getFirst().equals(qTargetStep.getPairOfState().getSecond())
                    & step.getStep().equals(stepOfqSource)) {
                result = true;
                q.clear();
                q.add(step.getPairOfState().getFirst());
            }
        }
        return result;
    }

    // Check if the begin of this sequence is the same q of thick arc.
    public static boolean arcsWithSameqSOfThickArc(Set<String> substep, DataOfSTS thickArc,
            ArrayList<DataOfSTS> stsData, STS sts) {
        boolean result = false;
        if (stsData.stream().anyMatch(o -> o.getPairOfState().getFirst().equals(thickArc.getPairOfState().getFirst())
                & o.getStep().equals(substep))) {
            result = true;
        }
        return result;
    }

    // Check if the end of this sequence is the same q' of thick arc.
    public static boolean arcsWithSameqTOfThickArc(Set<String> substep, DataOfSTS thickArc,
            ArrayList<DataOfSTS> stsData, STS sts) {
        boolean result = false;
        if (stsData.stream().anyMatch(o -> o.getPairOfState().getSecond().equals(thickArc.getPairOfState().getSecond())
                & o.getStep().equals(substep))) {
            result = true;
        }
        return result;
    }

    public static ArrayList<DataOfSTS> removeNotAllowedThickArcs(ArrayList<DataOfSTS> stsData1, STS sts) {
        ArrayList<DataOfSTS> notAllowedThickArcs = new ArrayList<>();
        ArrayList<DataOfSTS> stsData = new ArrayList<>();
        stsData.addAll(stsData1);
        Set<Integer> localities = STS.getLocalities();   
        for (DataOfSTS thickArc : stsData) {
            if (thickArc.getStep().size() > 1) {
                ArrayList<Set<String>> splitSubsteps = splitIntoSubsteps(thickArc, localities, sts);
                // check if thick arc is like {c2,c3}, so we cannot remove it.
                if (splitSubsteps.size() > 1) {
                    List<List<Set<String>>> potentailSteps = generatePerm(splitSubsteps);
                    if (!isAllowedArc(thickArc, potentailSteps, stsData, sts)) {
                        notAllowedThickArcs.add(thickArc);
                    }
                }
            }
        }
        if (!notAllowedThickArcs.isEmpty()) {
            stsData.removeAll(notAllowedThickArcs);
            if (notAllowedThickArcs.size() > 1) {
//                System.out.println(
//                        "There are " + notAllowedThickArcs.size() + " thick arcs removed from the sts, which are: ");
            } else {
//                System.out.println("This thick arc is removed from the sts: ");
            }
        } else {
//            System.out.println("There is no thick arc to be removed from the sts. ");
        }

        // Print Not Allowed Thick Arcs q-->q' U.
//      for (DataOfSTS step : notAllowedThickArcs) {
//          int i =0;
//          System.out.print("" + sts.getNodeReference(step.getPairOfState().getFirst()));
//          System.out.print("--->" + sts.getNodeReference(step.getPairOfState().getSecond()));
//          System.out.print(" U={");
//          for (String s1 : step.getStep()) {
//              i++;
//              if (!s1.isEmpty())
//                  System.out.print(s1);
//                  System.out.print(i < s1.length() ? "," : "");
//          }
//          System.out.println("} ");
//      }
        return stsData;
    }
}