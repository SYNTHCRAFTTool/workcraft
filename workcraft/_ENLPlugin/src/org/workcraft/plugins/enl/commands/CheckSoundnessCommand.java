package org.workcraft.plugins.enl.commands;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckSoundnessCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Check soundness";
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
        String msg = "";
        HashMap<HashSet<Transition>, Integer> emptyPrePostEvents = checkNonEmptyEvents(enl);
        HashSet<Transition> emptyEvents = new HashSet<>();
        if (emptyPrePostEvents.isEmpty()) {
            msg = "The enl-system has no events with empty pre and/or post conditions. \n";
        } else {
            for (HashMap.Entry<HashSet<Transition>, Integer> entry : emptyPrePostEvents.entrySet()) {
                if (entry.getValue() == 1) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl-system has events with empty pre and post conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());
                } else if (entry.getValue() == 2) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl-system has events with empty pre-conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());

                } else if (entry.getValue() == 3) {
                    String refStr = ReferenceHelper.getNodesAsString(enl, entry.getKey(), SizeHelper.getWrapLength());
                    msg = msg + "The enl-system has events with empty post-conditions:\n" + refStr + "\n";
                    emptyEvents.addAll(entry.getKey());
                }
            }
        }
            HashSet<Condition> isolatedConditions = checkConditions(enl);
            if (!isolatedConditions.isEmpty()) {
                String refStr = "";
                for(Condition condition : isolatedConditions) {
                     refStr = enl.getName(condition);
                }
                msg = msg + ("\n There are isolated conditions:  " + refStr) + "\n";
            }
            msg = msg + "\n Select events with empty Pre or/and Post conditions or isolated conditions?\n";
            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                VisualENL visualEnl = WorkspaceUtils.getAs(we, VisualENL.class);
                SelectionHelper.selectByReferencedComponents(visualEnl, emptyEvents, isolatedConditions);
            }
        
        return emptyPrePostEvents.isEmpty();
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

    private HashSet<Condition> checkConditions(final ENL enl) {
        HashSet<Condition> conditions = new HashSet<>();
        for (Condition condition : enl.getConditions() ) {
            if (enl.getPreset(condition).isEmpty() & enl.getPostset(condition).isEmpty()) {
                conditions.add(condition);
            }
        }
        return conditions;
    }
}
