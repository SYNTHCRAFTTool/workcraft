package org.workcraft.plugins.sts.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.TransitionArc;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachabilityVerificationCommand extends AbstractVerificationCommand {
    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Check state reachability [axiom-A1]";
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
        HashSet<State> unreachableStates = checkReachability(sts);
        if (unreachableStates.isEmpty()) {
            DialogUtils.showInfo("The ts satisfies axiom A1. All states are reachable from the initial state.", TITLE);
        } else {
            String refStr = ReferenceHelper.getNodesAsString(sts, unreachableStates, SizeHelper.getWrapLength());
            String msg = "The ts does not satisfy axiom A1. There are states that are unreachable from the initial state:\n"
                    + refStr + ".\n\nSelect unreachable states?\n";
            if (DialogUtils.showConfirmWarning(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualSTS visualSts = WorkspaceUtils.getAs(we, VisualSTS.class);
                SelectionHelper.selectByReferencedComponents(visualSts, unreachableStates);
            }
        }
        return unreachableStates.isEmpty();
    }

    private static HashSet<State> checkReachability(final STS sts) {
        HashMap<State, HashSet<TransitionArc>> stateEvents = calcStateOutgoingEventsMap(sts);
        HashSet<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        State initialState = sts.getInitialState();
        if (initialState != null) {
            queue.add(initialState);
        }
        while (!queue.isEmpty()) {
            State curState = queue.remove();
            if (visited.contains(curState))
                continue;
            visited.add(curState);
            if (stateEvents.get(curState).isEmpty())
                continue;
            for (TransitionArc curEvent : stateEvents.get(curState)) {
                State nextState = (State) curEvent.getSecond();
                if (nextState != null) {
                    queue.add(nextState);
                }
            }
        }
        HashSet<State> unreachableStates = new HashSet<>(sts.getStates());
        unreachableStates.removeAll(visited);
        return unreachableStates;
    }

    // Calculate each state with its out going events (steps leaving state). Use for
    // axiom A1.
    public static HashMap<State, HashSet<TransitionArc>> calcStateOutgoingEventsMap(STS sts) {
        HashMap<State, HashSet<TransitionArc>> stateOutgoingEvents = new HashMap<>();
        for (State state : sts.getStates()) {
            HashSet<TransitionArc> events = new HashSet<>();
            stateOutgoingEvents.put(state, events);
            for (TransitionArc event : sts.getArcs()) {
                if (event.getFirst().equals(state)) {
                    events.add(event);
                }
            }
        }
        return stateOutgoingEvents;
    }

    public static boolean checkAxiomOne(final STS sts) {
        return checkReachability(sts).isEmpty();
    }
}
