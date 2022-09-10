package org.workcraft.plugins.enl.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.plugins.enl.utils.ElementaryNetUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Unreachable condition";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, ENL.class);
    }

    @Override
    public final Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final ENL enl = WorkspaceUtils.getAs(we, ENL.class);
        HashSet<Condition> unreachableConditions = checkReachability(enl);
        if (unreachableConditions.isEmpty()) {
            DialogUtils.showInfo("There is no an unreachable condition in the model.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(enl, unreachableConditions, SizeHelper.getWrapLength());
            String msg = "The model has unreachable condition:\n" + refStr + "\n\nSelect unreachable conditions?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualENL visualEnl = WorkspaceUtils.getAs(we, VisualENL.class);
                SelectionHelper.selectByReferencedComponents(visualEnl, unreachableConditions);
            }
        }
        return unreachableConditions.isEmpty();
    }

    private HashSet<Condition> checkReachability(final ENL enl) {
        HashMap<Condition, HashSet<Transition>> conditionTransitions = ElementaryNetUtils
                .calcConditionOutgoingEventsMap(enl);

        HashSet<Condition> visited = new HashSet<>();
        Queue<Condition> queue = new LinkedList<>();

        Set<Condition> initialConditions = ElementaryNetUtils.initialConditions(enl);
        for (Condition initialCondition : initialConditions) {
            if (initialCondition != null) {
                queue.add(initialCondition);
            }
        }
        while (!queue.isEmpty()) {
            Condition curCondition = queue.remove();
            if (visited.contains(curCondition))
                continue;
            visited.add(curCondition);
            for (Transition curTransition : conditionTransitions.get(curCondition)) {
                for (MathConnection c : enl.getConnections(curTransition)) {
                    if (curTransition == c.getFirst()) {
                        Condition nextCondition = (Condition) c.getSecond();
                        if (nextCondition != null) {
                            queue.add(nextCondition);
                        }
                    }
                }
            }
        }
        HashSet<Condition> unreachableConditions = new HashSet<>(enl.getConditions());
        unreachableConditions.removeAll(visited);
        return unreachableConditions;
    }
}
