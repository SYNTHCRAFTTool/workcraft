package org.workcraft.plugins.enl.commands;

import java.time.Duration;
import java.time.Instant;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.enl.ENLDescriptor;
import org.workcraft.plugins.enl.tools.STSToENLConverter;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class STSToENLredRulesConvesionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis-[Method I: by checking axioms A1-A4 for an ST-system, and applying reduction rules]";
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
        final VisualSTS sts = me.getAs(VisualSTS.class);
        final STSToENLConverter converter = new STSToENLConverter(sts, true);
        ModelEntry m = new ModelEntry(new ENLDescriptor(), converter.getENL());
        // To stop the system if there are no localities associated with each event.
        if (converter.getHasLocalities()) {
            if (converter.getResult()) {
                Instant end = Instant.now();
                long time = Duration.between(start, end).toMillis();
//                System.out.println("Execution time way 1 is " + time + " MilliSeconds");
                STS.recordTime(time);
                DialogUtils.showInfo("The given ts satisfies axioms A1-A4 w.r.t " + "\u224F" + STS.getCoRe()
                        + "So, it can be synthesised to an ENL_{" + "\u224F"
                        + "}-system with applying reduction rules.", "Result");
                converter.setHasLocalities();
                converter.setResult();
                return m;
            } else {
                Instant end = Instant.now();
                long time = Duration.between(start, end).toMillis();
//                System.out.println(time);
                STS.recordTime(time);
                DialogUtils.showWarning(
                        "The given ts is not synthesisable within the class of ENL-systems, because it does not satisfy axioms"
                                + converter.getMessage() + ".");
                return null;
            }

        } else {
            return null;
        }
    }
}