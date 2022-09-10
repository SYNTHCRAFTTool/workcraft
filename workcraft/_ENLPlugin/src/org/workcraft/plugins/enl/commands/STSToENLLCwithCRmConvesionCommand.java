package org.workcraft.plugins.enl.commands;

import java.time.Duration;
import java.time.Instant;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.enl.ENLDescriptor;
import org.workcraft.plugins.enl.tools.STSToENLWayTowConverter;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.plugins.sts.extractingtsmin.TSmin;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class STSToENLLCwithCRmConvesionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis-[Check if ts can be synthesised to an ENL/LC-system and if so construct a solution satisfying criteria 1]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, STS.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        if (Hierarchy.isHierarchical(me)) {
            DialogUtils.showError("STS cannot be derived from a hierarchical ENL.");
            return null;
        }
        Instant start = Instant.now();
        final VisualSTS vsts = me.getAs(VisualSTS.class);
        final STS sts = vsts.getStepTransitionSystemModel();
        if (TSmin.getTSmin(sts, 2)) {
            final STSToENLWayTowConverter converter = new STSToENLWayTowConverter(vsts, false);
            ModelEntry m = new ModelEntry(new ENLDescriptor(), converter.getENL());
            if (converter.getHasLocalities()) {
                if (STSToENLWayTowConverter.isIsomorphic()) {
                    Instant end = Instant.now();
                    long time = Duration.between(start, end).toMillis();
//            System.out.println("Execution time is " + time + " milliseconds");
                    STS.recordTime(time);
                    DialogUtils.showInfo("The given ts can be synthesised w.r.t " + "\u224F" + "^{ts}_{min} "
                            + STS.getCoRe() + " to an ENL/LC-system.", "Result");
                    STS.setClearLocalities();
                    return m;
                } else {
                    Instant end = Instant.now();
                    long time = Duration.between(start, end).toMillis();
//            System.out.println(time);
                    STS.recordTime(time);
                    DialogUtils.showInfo("The given ts is not synthesisable within the class of ENL-systems w.r.t "
                            + "\u224F" + "^{ts}_{min} " + STS.getCoRe() + ", because it is not isomorphic to ts_{enl^{"
                            + "\u224F" + "}_{ts}}.", "Result");
                    STS.setClearLocalities();
                    return null;
                }
            } else {
                return null;
            }
        } else {
            DialogUtils.showWarning("The given ts is not synthesisable within the class of ENL/LC-systems.");
            Instant end = Instant.now();
            long time = Duration.between(start, end).toMillis();
            System.out.println("Execution time is " + time + " milliseconds");
            STS.recordTime(time);
            return null;
        }
    }
}
