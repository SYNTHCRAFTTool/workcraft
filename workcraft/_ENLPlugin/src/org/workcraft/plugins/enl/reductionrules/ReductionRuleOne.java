package org.workcraft.plugins.enl.reductionrules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.STSUtils;

public class ReductionRuleOne {

    // Check if we can delete r=(in, r, out) or its complement r'=(out, Q\r , in).
    public static Set<Region> reduceComplementRegions(Set<Region> compatibleRegions,
            Map<Region, Region> mapOfregsWithComplement, STS sts) {
        Set<Region> temp = new HashSet<>();
        Set<Region> nonTrivialRegions = compatibleRegions;
//        int i = 0;
        if (!compatibleRegions.isEmpty()) {
            for (Entry<Region, Region> reg : mapOfregsWithComplement.entrySet()) {
                if (compatibleRegions.contains(reg.getKey()) & compatibleRegions.contains(reg.getValue())) {
                    nonTrivialRegions.remove(reg.getValue());
                    temp.add(reg.getValue());
                    if (!STSUtils.isENLSystemForRuleOne(sts, nonTrivialRegions)) {
                        nonTrivialRegions.add(reg.getValue());
                        temp.remove(reg.getValue());                        
                        nonTrivialRegions.remove(reg.getKey());
                        temp.add(reg.getKey());
                        if (!STSUtils.isENLSystemForRuleOne(sts, nonTrivialRegions)) {
                            nonTrivialRegions.add(reg.getKey());
                            temp.remove(reg.getKey());
                        }
                    }
                    }
                }
            }
//        }
//        System.out.println("i= " + i);
//        if (!temp.isEmpty()) {
//            System.out.println("Regions will be deleted according to rule 1: ");
//            ExtractRts.printRegions(sts, temp);
//        }
        return nonTrivialRegions;
    }

    // Check if we can delete r=(in, r, out) or its complement r'=(out, Q\r , in).
    public static Set<Region> reduceComplementRegions(Set<Region> compatibleRegions,
            Map<Region, Region> mapOfregsWithComplement, STS sts, Map<String, Set<Region>> preRegions,
            Map<String, Set<Region>> postRegions) {
        Set<Region> nonTrivialRegions = compatibleRegions;
        if (!compatibleRegions.isEmpty()) {
            for (Entry<Region, Region> reg : mapOfregsWithComplement.entrySet()) {
                if (compatibleRegions.contains(reg.getKey()) & compatibleRegions.contains(reg.getValue())) {
                    nonTrivialRegions.remove(reg.getValue());
                    if (!STSUtils.isENLSystemForRuleOne(sts, nonTrivialRegions)) {
                        nonTrivialRegions.add(reg.getValue());
                        nonTrivialRegions.remove(reg.getKey());
                        if (!STSUtils.isENLSystemForRuleOne(sts, nonTrivialRegions)) {
                            nonTrivialRegions.add(reg.getKey());
                        }
                    }
                }
            }
        }
        return nonTrivialRegions;
    }
}
