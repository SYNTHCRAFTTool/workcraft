package org.workcraft.plugins.enl.reductionrules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.STSUtils;

public class ReductionRuleFour {
    // Check if we can delete r=(in, r, out) and its complement r'=(out, Q\r , in).
    public static Set<Region> reduceComplementRegionsRuleFour(Set<Region> compatibleRegions,
            Map<Region, Region> mapOfregsWithComplement, STS sts,Set<Region> compositionRegions) {
        Set<Region> nonTrivialRegions = compatibleRegions;
        Set<Region> temp = new HashSet<>();

        if (!compatibleRegions.isEmpty()) {
            // r=(in,r,out) and r'=(in',r',out')
            for (Entry<Region, Region> reg : mapOfregsWithComplement.entrySet()) {
//                if (compatibleRegions.contains(reg.getKey()) & compatibleRegions.contains(reg.getValue())) {
                    // in = {} and out' = {}
                    if (reg.getKey().getEventsEnteringR().isEmpty() & reg.getValue().getEventsLeavingR().isEmpty()) {
                        // |out| = 1 and |in'| = 1 
                        if (reg.getKey().getEventsLeavingR().size() == 1
                                & reg.getValue().getEventsEnteringR().size() == 1) {
                            String i = "";
                            String o = "";
                            for (String in : reg.getKey().getEventsLeavingR()) {
                                i = i + in;
                            }
                            for (String out : reg.getValue().getEventsEnteringR()) {
                                o = o + out;
                            }
                            // out=in'=e
                            if (i.equals(o)) {
//                                ExtractRts.printRegion(sts, reg.getKey());
//                                ExtractRts.printRegion(sts, reg.getValue());

                                Set<Region> ePreRegions = new HashSet<>();
                                Set<Region> ePostRegions = new HashSet<>();
                                STSUtils.getePrePostRegions(nonTrivialRegions, i, ePreRegions, ePostRegions);
                                boolean deletingr = false;     
                                boolean deletingreg = false;

                                // |0e| > 1 and |e0| > 1
                                if (ePreRegions.size() > 1) { 
                                    if (!compositionRegions.contains(reg.getKey())) { 
                                        nonTrivialRegions.remove(reg.getKey());
                                        deletingr = true;
//                                        System.out.println("deletingr= " + deletingr);

                                    }
                                }
                                    if (ePostRegions.size() > 1) { 
                                    if (!compositionRegions.contains(reg.getValue())) {
                                        nonTrivialRegions.remove(reg.getValue());
                                        deletingreg = true;
//                                        System.out.println("deletingreg= " + deletingreg);

                                    }
                                    }
                                     if(deletingr || deletingreg) {
                                    if (!STSUtils.isENLSystem(sts, nonTrivialRegions)) {
                                        if (deletingr) 
                                        nonTrivialRegions.add(reg.getKey());
                                        
                                        if (deletingreg) 
                                        nonTrivialRegions.add(reg.getValue());
                                        
                                    }else {
                                        if (deletingr)
                                        temp.add(reg.getKey());
                                        if (deletingreg) 
                                            temp.add(reg.getValue());
                                    }
                                    }
//                                     System.out.println("deletingr= " + deletingr + "  deletingreg= " + deletingreg);
                            }
                        }
                    }

                    if (reg.getValue().getEventsEnteringR().isEmpty() & reg.getKey().getEventsLeavingR().isEmpty()) {
                        if (reg.getValue().getEventsLeavingR().size() == 1
                                & reg.getKey().getEventsEnteringR().size() == 1) {
                            String i = "";
                            String o = "";
                            for (String in : reg.getValue().getEventsLeavingR()) {
                                i = i + in;
                            }
                            for (String out : reg.getKey().getEventsEnteringR()) {
                                o = o + out;
                            }
                            if (i.equals(o)) {
                                Set<Region> ePreRegions = new HashSet<>();
                                Set<Region> ePostRegions = new HashSet<>();
                                STSUtils.getePrePostRegions(nonTrivialRegions, i, ePreRegions, ePostRegions);
                                boolean deletingr = false;                                    
                                boolean deletingreg = false;
                                // |0e| > 1 and |e0| > 1
                                if (ePostRegions.size() > 1) { 
                                 
                                    if (!compositionRegions.contains(reg.getKey())) { 
                                        nonTrivialRegions.remove(reg.getKey());
                                        deletingr = true;
                                    }
                                }
                                if (ePreRegions.size() > 1 ) { 

                                    if (!compositionRegions.contains(reg.getValue())) {
                                        nonTrivialRegions.remove(reg.getValue());
                                        deletingreg = true;
                                    }
                                }
                                     if(deletingr || deletingreg) {
                                    if (!STSUtils.isENLSystem(sts, nonTrivialRegions)) {
                                        if (deletingr) 
                                        nonTrivialRegions.add(reg.getValue());
                                        
                                        if (deletingreg) 
                                        nonTrivialRegions.add(reg.getKey());
                                        
                                    }else {
                                        if (deletingr)
                                        temp.add(reg.getValue());
                                        if (deletingreg) 
                                            temp.add(reg.getKey());
                                    }
                                    }
                                     System.out.println("deletinggr= " + deletingr + "  deletinggreg= " + deletingreg);
                            }
                        }
                    }
//                }
            }
        }

        if (!temp.isEmpty()) {
            System.out.println("Regions will be deleted according to rule 4: ");
            ExtractRts.printRegions(sts, temp);
        }
        return nonTrivialRegions;
    }

}
