package org.workcraft.plugins.enl.reductionrules;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;

public class ReductionRuleThree {

// Reduction rule three (companion regions).
    public static Set<Set<Region>> companionRegions(Set<Region> nonTrivialRegions, STS sts) {
        Set<Set<Region>> companionRegions = new HashSet<>();
        for (Region reg : nonTrivialRegions) {
            boolean check = false;
            Set<Region> companionOfSameRegion = new HashSet<>();
            if (!companionRegions.isEmpty()) {
                for (Set<Region> regs : companionRegions) {
                    for (Region region : regs) {
                        if (reg.getRinRegion().equals(region.getRinRegion())) {
                            check = true;
                            break;
                        }
                    }
                }
            }
            if (!check) {
                for (Region r : nonTrivialRegions) {
                    if (!reg.equals(r)) {
                        if (reg.getRinRegion().equals(r.getRinRegion())) {
                            companionOfSameRegion.add(r);
                        }
                    }
                }
                if (!companionOfSameRegion.isEmpty()) {
                    companionOfSameRegion.add(reg);
                    companionRegions.add(companionOfSameRegion);
                }
            }
        }
//    for (Set<Region> regs : companionRegions) {
//        System.out.println("a set of companion regions: " + "{");
//        ExtractRts.printRegions(sts, regs);
//        System.out.println("}");
//        System.out.println("****************************");
//    }
        return companionRegions;
    }

    public static Set<Region> redundantCompanionRegions(Set<Region> nonTrivialRegions, STS sts) {

        Set<Set<Region>> companionRegions = companionRegions(nonTrivialRegions, sts);
        Set<Region> temp = new HashSet<>();
        for (Set<Region> comRegions : companionRegions) {
            if (comRegions.size() >= 2) {
                int i = 0;
                Set<Region> notRedRegs = new HashSet<>();
                Set<Region> notRedReg = new HashSet<>();
                Set<Region> mayRedRegs = new HashSet<>();
                for (Region reg : comRegions) {
                    i++;
                    if (i == 1)
                        notRedRegs.add(reg);
                    if (i >= 2) {
                        for (Region r : notRedRegs) {
                            List<String> in = r.getEventsEnteringR().stream().filter(reg.getEventsEnteringR()::contains)
                                    .collect(Collectors.toList());
                            List<String> out = r.getEventsLeavingR().stream().filter(reg.getEventsLeavingR()::contains)
                                    .collect(Collectors.toList());
                            if (in.isEmpty() & out.isEmpty()) {
                                notRedReg.add(reg);
                            } else {
                                mayRedRegs.add(reg);
                            }
                        }
                    }
                }
                if (!notRedReg.isEmpty()) {
                    notRedRegs.addAll(notRedReg);
                }
                for (Region redReg : mayRedRegs) {
                    Set<String> inSets = new HashSet<>();
                    Set<String> outSets = new HashSet<>();
                    for (Region reg : comRegions) {
                        if (!redReg.equals(reg)) {
                            inSets.addAll(reg.getEventsEnteringR());
                            outSets.addAll(reg.getEventsLeavingR());
                        }
                    }
                    if (inSets.containsAll(redReg.getEventsEnteringR())
                            & outSets.containsAll(redReg.getEventsLeavingR())) {
                        temp.add(redReg);
                        nonTrivialRegions.remove(redReg);
                    } else {
                        notRedRegs.add(redReg);
                    }
                }
            }
        }
//    if (!temp.isEmpty()) {
//        System.out.println("Regions will be deleted according to rule 3: ");
//        ExtractRts.printRegions(sts, temp);
//    }
//  System.out.println("*********************************");
//    ExtractRts.printRegions(sts, nonTrivialRegions);
        return nonTrivialRegions;
    }
}
