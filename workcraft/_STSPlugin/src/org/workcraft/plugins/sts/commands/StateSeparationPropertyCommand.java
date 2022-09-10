package org.workcraft.plugins.sts.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.plugins.sts.utils.STSUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StateSeparationPropertyCommand extends AbstractVerificationCommand {
    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Check state separation property [axiom-A3]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, STS.class);
    }

    @Override
    public final Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final STS sts = WorkspaceUtils.getAs(we, STS.class);
        Set<Set<State>> StateSeparationProperty = checkStateSeparationProperty(sts);
        HashSet<State> states = new HashSet<>();
        String msg = "";
        if (StateSeparationProperty.isEmpty()) {
            DialogUtils.showInfo(
                    "The ts satisfies axiom A3. There are no states violating the State Separation Property.", TITLE);
        } else {
            for (Set<State> qToq : StateSeparationProperty) {
                String refStr = ReferenceHelper.getNodesAsString(sts, qToq, SizeHelper.getWrapLength());
                msg = msg
                        + "The ts does not satisfy axiom A3. There are states violating the State Separation Property:\n"
                        + refStr + ".\n\n";
                states.addAll(qToq);
            }
            msg = msg + "\n Select states violating the State Separation Property?\n";
            if (DialogUtils.showConfirmWarning(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualSTS visualSts = WorkspaceUtils.getAs(we, VisualSTS.class);
                SelectionHelper.selectByReferencedComponents(visualSts, states);
            }
        }
        return StateSeparationProperty.isEmpty();
    }

    // check state separation property for axiom 3.
    public static Set<Set<State>> checkStateSeparationProperty(final STS sts) {
        Set<Region> nonTrivialRegions = STS.getNonTrvialRegions(sts);
        Map<Region, String> ri = new HashMap<>();
        int i = 0;
        for (Region reg : nonTrivialRegions) {
            ri.put(reg, "r" + i++);
        }
        HashMap<State, HashSet<Region>> allRq = STSUtils.calcRqForEachState(nonTrivialRegions, sts);
        Set<Set<State>> result = new HashSet<>();
        for (Entry<State, HashSet<Region>> R : allRq.entrySet()) {
            Set<State> fToS = new HashSet<>();
            for (Entry<State, HashSet<Region>> toR : allRq.entrySet()) {
                if (!R.equals(toR)) {
                    if (R.getValue().equals(toR.getValue())) {
                        fToS.add(R.getKey());
                        fToS.add(toR.getKey());
                        result.add(fToS);
                    }
                }
            }
        }
        if (!result.isEmpty()) {
            for (Set<State> state : result) {
                for (State s : state) {
                    int h = 0;
                    System.out.print("R" + sts.getName(s) + " = {");
                    for (Region reg : allRq.get(s)) {
                        System.out.print(ri.get(reg));
                        System.out.print(allRq.get(s).size() > ++h ? "," : "");
                    }
                    System.out.println("}");
                }
            }
        }
        return result;
    }

    // check state separation property for axiom A3.
    public static ArrayList<Set<State>> checkStateSeparationPropertyWayTwo(Set<Region> nonTrivialRegions, final STS sts,
            HashMap<State, HashSet<Region>> allRq) {
        ArrayList<Set<State>> result = new ArrayList<>();
        for (Entry<State, HashSet<Region>> R : allRq.entrySet()) {
            for (Entry<State, HashSet<Region>> toR : allRq.entrySet()) {
                if (!R.equals(toR)) {
                    if (R.getValue().equals(toR.getValue())) {
                        Set<State> fToS = new HashSet<>();
                        fToS.add(R.getKey());
                        fToS.add(toR.getKey());
                        result.add(fToS);
                    }
                }
            }
        }
        return result;
    }

    public static boolean checkAxiomThree(Set<Region> nonTrivialRegions, final STS sts,
            HashMap<State, HashSet<Region>> allRq) {
        return checkStateSeparationPropertyWayTwo(nonTrivialRegions, sts, allRq).isEmpty();
    }
}
