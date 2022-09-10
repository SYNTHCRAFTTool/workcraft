package org.workcraft.plugins.enl.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.utils.ElementaryNetUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ENLStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "An enl-system analysis";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, ENL.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        ENL enl = WorkspaceUtils.getAs(we, ENL.class);
        Collection<Condition> conditions = enl.getConditions();
        String msg = "";
        HashMap<HashSet<Transition>, Integer> emptyPrePostEvents = checkNonEmptyEvents(enl);
        HashSet<Transition> emptyEvents = new HashSet<>();
        if (emptyPrePostEvents.isEmpty()) {
            msg = "There is no events with empty sets of pre- or/and post conditions";
        } else {
            for (HashMap.Entry<HashSet<Transition>, Integer> entry : emptyPrePostEvents.entrySet()) {
                if (entry.getValue() == 1) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl has events with empty Pre-Post conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());
                } else if (entry.getValue() == 2) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl has events with empty Pre-conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());
                } else if (entry.getValue() == 3) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl has events with empty Post-conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());
                }
            }
        }
        int tokenCount = 0;
        int markedCount = 0;
        int isolatedConditionCount = 0;

        for (Condition condition : conditions) {
            Set<MathNode> conditionPreset = enl.getPreset(condition);
            Set<MathNode> conditionnPostset = enl.getPostset(condition);
            if (condition.getTokens() > 0) {
                tokenCount += condition.getTokens();
                markedCount++;
            }
            if (conditionPreset.isEmpty() && conditionnPostset.isEmpty()) {
                isolatedConditionCount++;
            }
        }
        return "An enl-system analysis:" + "\n  Event count -  " + enl.getTransitions().size()
                + "\n  Condition count -  " + enl.getConditions().size() + "\n  Arc count -  "
                + enl.getConnections().size() 
                + "\n  Token count / marked conditions -  " + tokenCount + " / "
                + markedCount + "\n "
                + " " +  msg 
                + "\n  Isolated conditions -  " + isolatedConditionCount
                + "\n  Co-location relations \u224F on E " + ElementaryNetUtils.getCoRe(enl);
    }

    private HashMap<HashSet<Transition>, Integer> checkNonEmptyEvents(final ENL enl) {
        HashMap<HashSet<Transition>, Integer> transitions = new HashMap<>();
        HashSet<Transition> prePostEmptyTransitions = new HashSet<>();
        HashSet<Transition> preEmptyTransitions = new HashSet<>();
        HashSet<Transition> postEmptyTransitions = new HashSet<>();

        for (Transition transition : enl.getTransitions()) {
            if (enl.getPreset(transition).isEmpty()) {
                if (enl.getPostset(transition).isEmpty()) {
                    prePostEmptyTransitions.add(transition);
                } else {
                    preEmptyTransitions.add(transition);
                }
            }
            if (enl.getPostset(transition).isEmpty()) {
                if (enl.getPreset(transition).isEmpty()) {
                    prePostEmptyTransitions.add(transition);
                } else {
                    postEmptyTransitions.add(transition);
                }
            }
        }
        // Collect events with both pre-post conditions are empty.
        if (!prePostEmptyTransitions.isEmpty()) {
            transitions.put(prePostEmptyTransitions, 1);
        }
        // Collect events with pre conditions are empty.
        if (!preEmptyTransitions.isEmpty()) {
            transitions.put(preEmptyTransitions, 2);
        }
        // Collect events with post conditions are empty.
        if (!postEmptyTransitions.isEmpty()) {
            transitions.put(postEmptyTransitions, 3);
        }
        return transitions;
    }
}
