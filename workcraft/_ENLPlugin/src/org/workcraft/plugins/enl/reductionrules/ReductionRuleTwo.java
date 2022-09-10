package org.workcraft.plugins.enl.reductionrules;

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
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.DataOfSTS;

public class ReductionRuleTwo {
    static Map<Region, Region> StrocompRegions = new HashMap<>();

    // Reduction rule two (composition regions of compatible regions).
    public static Set<Region> compatibleRegions(Set<Region> nonTrivialRegions, ArrayList<DataOfSTS> stsData, STS sts) {
        Set<Region> tempnonTrivialRegions = new HashSet<>();
        tempnonTrivialRegions = nonTrivialRegions;
        Set<Region> compositionRegions = new HashSet<>();
        boolean second = false;
        boolean third = false;
        boolean fourth = false;
        boolean fifth = false;  
//        System.out.println("Below is a list of regions where regions r and r'  are strongly compatible: ");
        for (Region reg : tempnonTrivialRegions) {
            for (Region r : tempnonTrivialRegions) {
                if (!reg.equals(r)) {
                   // r /\ r' = Ø.
                    List<State> frisCond = reg.getRinRegion().stream().filter(r.getRinRegion()::contains)
                            .collect(Collectors.toList());
                    if (frisCond.size() == 0) {
                        Set<String> eItsEndInreg = new HashSet<>();
                        Set<String> eItsEndInr = new HashSet<>();
                        Set<String> eItsBeginningInreg = new HashSet<>();
                        Set<String> eItsBeginningInr = new HashSet<>();
                        Set<String> eItsNotEndInreg = new HashSet<>();
                        Set<String> eItsNotEndInr = new HashSet<>();
                        Set<String> eItsNotBeginningInreg = new HashSet<>();
                        Set<String> eItsNotBeginningInr = new HashSet<>();

                        Set<String> eInr = new HashSet<>();
                        Set<String> eOutr = new HashSet<>();
                        Set<String> eInreg = new HashSet<>();
                        Set<String> eOutreg = new HashSet<>();

                        for (String event : sts.createEvents()) {
                            int noOfStepsOfEvent = 0;
                            int stepsEndInreg = 0;
                            int stepsNotEndInreg = 0;
                            int stepsEndInr = 0;
                            int stepsNotEndInr = 0;
                            int stepsBeginningInreg = 0;
                            int stepsNotBeginningInreg = 0;
                            int stepsBeginningInr = 0;
                            int stepsNotBeginningInr = 0;

                            int stepseIn = 0;
                            int stepseOut = 0;
                            int stepseInreg = 0;
                            int stepseOutreg = 0;
                            for (DataOfSTS step : stsData) {
                                if (step.getStep().contains(event)) {
                                    noOfStepsOfEvent++;
                                    // ^r
                                    if (reg.getRinRegion().contains(step.getPairOfState().getSecond())) {
                                        stepsEndInreg++;
                                    } else {
                                        stepsNotEndInreg++;
                                    }
                                    // ^r'
                                    if (r.getRinRegion().contains(step.getPairOfState().getSecond())) {
                                        stepsEndInr++;
                                    } else {
                                        stepsNotEndInr++;
                                    }
                                    // r^
                                    if (reg.getRinRegion().contains(step.getPairOfState().getFirst())) {
                                        stepsBeginningInreg++;
                                    } else {
                                        stepsNotBeginningInreg++;
                                    }
                                    // r'^
                                    if (r.getRinRegion().contains(step.getPairOfState().getFirst())) {
                                        stepsBeginningInr++;
                                    } else {
                                        stepsNotBeginningInr++;
                                    }

                                    // e in in'
                                    if (r.getEventsEnteringR().contains(event)) {
                                        stepseIn++;
                                    }
                                    // e in out'
                                    if (r.getEventsLeavingR().contains(event)) {
                                        stepseOut++;
                                    }
                                    // e in in
                                    if (reg.getEventsEnteringR().contains(event)) {
                                        stepseInreg++;
                                    }
                                    // e in out
                                    if (reg.getEventsLeavingR().contains(event)) {
                                        stepseOutreg++;
                                    }
                                }
                            }
                            if (noOfStepsOfEvent == stepsEndInreg) {
                                eItsEndInreg.add(event);
                            } else if (noOfStepsOfEvent == stepsNotEndInreg) {
                                eItsNotEndInreg.add(event);
                            }
                            if (noOfStepsOfEvent == stepsEndInr) {
                                eItsEndInr.add(event);
                            } else if (noOfStepsOfEvent == stepsNotEndInr) {
                                eItsNotEndInr.add(event);
                            }

                            if (noOfStepsOfEvent == stepsBeginningInreg) {
                                eItsBeginningInreg.add(event);
                            } else if (noOfStepsOfEvent == stepsNotBeginningInreg) {
                                eItsNotBeginningInreg.add(event);
                            }
                            if (noOfStepsOfEvent == stepsBeginningInr) {
                                eItsBeginningInr.add(event);
                            } else if (noOfStepsOfEvent == stepsNotBeginningInr) {
                                eItsNotBeginningInr.add(event);
                            }

                            if (noOfStepsOfEvent == stepseIn) {
                                eInr.add(event);
                            }

                            if (noOfStepsOfEvent == stepseOut) {
                                eOutr.add(event);
                            }
                            
                            if (noOfStepsOfEvent == stepseInreg) {
                                eInreg.add(event);
                            }

                            if (noOfStepsOfEvent == stepseOutreg) {
                                eOutreg.add(event);
                            }
                        }
                        Set<String> chechSecond = new HashSet<>();
                        if (!eInr.isEmpty())
                            chechSecond.addAll(eInr);
                        if (!eItsNotEndInr.isEmpty())
                            chechSecond.addAll(eItsNotEndInr);
                        Set<String> chechThird = new HashSet<>();
                        if (!eOutr.isEmpty())
                            chechThird.addAll(eOutr);
                        if (!eItsNotBeginningInr.isEmpty())
                            chechThird.addAll(eItsNotBeginningInr);
                        Set<String> chechFourth = new HashSet<>();
                        if (!eInreg.isEmpty())
                            chechFourth.addAll(eInreg);
                        if (!eItsNotEndInreg.isEmpty())
                            chechFourth.addAll(eItsNotEndInreg);
                        Set<String> chechFifth = new HashSet<>();
                        if (!eOutreg.isEmpty())
                            chechFifth.addAll(eOutreg);
                        if (!eItsNotBeginningInreg.isEmpty())
                            chechFifth.addAll(eItsNotBeginningInreg);

                        // Check 2 & 3 to see if r is compatible with r'.
                        if (chechSecond.containsAll(reg.getEventsLeavingR()))
                            second = true;
                        if (chechThird.containsAll(reg.getEventsEnteringR())) 
                            third = true;
                        // Check 4 & 5 to see if r' is compatible with r.
                        if (chechFourth.containsAll(r.getEventsLeavingR())) 
                            fourth = true;
                        if (chechFifth.containsAll(r.getEventsEnteringR())) 
                            fifth = true;                       
                        // check if the 5 conditions of reduction rule 2 are true.
                        if (second & third & fourth & fifth) {
                            /** calculate composition region = (in V in'\H, r V r' , out V out'\H) **/
                            // collect r V r'.
                            Set<State> compositionReg = new HashSet<>();
                            compositionReg.addAll(reg.getRinRegion());
                            compositionReg.addAll(r.getRinRegion());
                            /** calculate H in (in V in'\H, r V r' , out V out'\H) **/
                            Set<String> H = new HashSet<>();
                            // Get (^r /\ r'^).
                            List<String> fPartOfH = eItsEndInreg.stream().filter(eItsBeginningInr::contains)
                                    .collect(Collectors.toList());
                            // Get (^r' /\ r^).
                            List<String> sPartOfH = eItsEndInr.stream().filter(eItsBeginningInreg::contains)
                                    .collect(Collectors.toList());
                            // check in /\ in' != 0.
                            // to generate H = (in /\ in') V (out /\ out').
                            if (!fPartOfH.isEmpty() & !fPartOfH.equals(null))
                                H.addAll(fPartOfH);
                            // check out /\ out' != 0.
                            if (!sPartOfH.isEmpty())
                                H.addAll(sPartOfH);
//                        System.out.println("H= " + H);
                            /** get (in V in'\H) **/
                            Set<String> fPartIn = new HashSet<>();
                            if (!reg.getEventsEnteringR().isEmpty())
                                fPartIn.addAll(reg.getEventsEnteringR());

                            if (!r.getEventsEnteringR().isEmpty() & !r.getEventsEnteringR().equals(null))
                                fPartIn.addAll(r.getEventsEnteringR());
                            Set<String> inOfCompositionRegion = new HashSet<>();
                            if (!fPartIn.isEmpty())
                                inOfCompositionRegion.addAll(fPartIn);
                            inOfCompositionRegion.removeAll(H);

                            /** get (out V out'\H) **/
                            Set<String> sPartOut = new HashSet<>();
                            if (!reg.getEventsLeavingR().isEmpty())
                                sPartOut.addAll(reg.getEventsLeavingR());
                            if (!r.getEventsLeavingR().isEmpty())
                                sPartOut.addAll(r.getEventsLeavingR());
                            Set<String> outOfCompositionRegion = new HashSet<>();
                            if (!sPartOut.isEmpty())
                                outOfCompositionRegion.addAll(sPartOut);
                            outOfCompositionRegion.removeAll(H);

                            /** Great composition region = (in V in'\H, r V r' , out V out'\H) **/
                            boolean check = false;
                            if (compositionRegions.stream()
                                    .anyMatch(o -> o.getEventsEnteringR().equals(inOfCompositionRegion)
                                            & o.getRinRegion().equals(compositionReg)
                                            & o.getEventsLeavingR().equals(outOfCompositionRegion))) {
                                check = true;
                            }
                            if (!check) {
                                Region compositionRegion = new Region(inOfCompositionRegion, compositionReg,
                                        outOfCompositionRegion);
                                compositionRegions.add(compositionRegion);
                            }
                            check = false;
                        }
                        second = false;
                        third = false;
                        fourth = false;
                        fifth = false;
                    }
                }
            }
        }
        Set<Region> temp = new HashSet<>();
        if (!compositionRegions.isEmpty()) {
            for (Region reg : nonTrivialRegions) {
                if (compositionRegions.stream()
                        .anyMatch(o -> o.getEventsEnteringR().equals(reg.getEventsEnteringR())
                                & o.getRinRegion().equals(reg.getRinRegion())
                                & o.getEventsLeavingR().equals(reg.getEventsLeavingR()))) {
                    temp.add(reg);
                }
            }
//            if (!temp.isEmpty()) {
//                System.out.println("Regions will be deleted according to rule 2: ");
//                ExtractRts.printRegions(sts, temp);
//            }
            nonTrivialRegions.removeAll(temp);
        }
        return nonTrivialRegions;
    }

    public static Map<Region, Region> getStrocompRegions() {
        return StrocompRegions;
    }

}

