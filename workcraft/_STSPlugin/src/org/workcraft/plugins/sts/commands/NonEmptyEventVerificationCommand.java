package org.workcraft.plugins.sts.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NonEmptyEventVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Check for events with no pre- or post-regions [axiom-A2]";
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
        String msg = "";
        HashMap<Set<String>, Integer> emptyPrePostEvents = checkNonEmptyEvents(sts);
        HashSet<String> emptyEvents = new HashSet<>();
        if (emptyPrePostEvents.isEmpty()) {
            DialogUtils.showInfo("The ts satisfies axiom A2. There is no events with empty sets of pre- or/and post regions.", TITLE);
        } else {
            for (HashMap.Entry<Set<String>, Integer> entry : emptyPrePostEvents.entrySet()) {
                if (entry.getValue() == 1) {
                    msg = msg + "The ts does not satisfy axiom A2. There are events with empty sets of pre- and post-regions:\n\n";
                    emptyEvents.addAll(entry.getKey());
                } else if (entry.getValue() == 2) {
                    msg = msg + "The ts does not satisfy axiom A2. There are events with empty set of pre-regions:\n\n";
                    emptyEvents.addAll(entry.getKey());
                } else if (entry.getValue() == 3) {
                    msg = msg + "The ts does not satisfy axiom A2. There are events with empty set of post-regions:\n\n";
                    emptyEvents.addAll(entry.getKey());
                }
                String events = "";
                int i=0;
                for(String e: emptyEvents) {
                    i++;
                    events = events + e;
                    events = events + (i < emptyEvents.size() ? "," : "");
                }
                msg = msg + events;
                DialogUtils.showWarning(msg, TITLE);

            }
        }
        return emptyPrePostEvents.isEmpty();
    }

    private static HashMap<Set<String>, Integer> checkNonEmptyEvents(final STS sts) {
        HashMap<Set<String>, Integer> chechEvents = new HashMap<>();
        Set<Region> nonTrivialRegions = STS.getNonTrvialRegions(sts);
        Set<String> events = sts.createEvents();
        for (String event : events) {
            HashSet<Region> preRegionsOfEvents = new HashSet<>();
            HashSet<Region> postRegionsOfEvents = new HashSet<>();
            for (Region region : nonTrivialRegions) {
                // collect the set of pre-regions 0e of an event.
                if (region.getEventsLeavingR().contains(event)) {
                    preRegionsOfEvents.add(region);
                }
                // collect the set of post-regions e0 of an event.
                if (region.getEventsEnteringR().contains(event)) {
                    postRegionsOfEvents.add(region);
                }
            }
            Set<String> eventName = new HashSet<>();
            eventName.add(event);
            // Collect events with both pre-post regions are empty.
            if (preRegionsOfEvents.isEmpty() & postRegionsOfEvents.isEmpty()) {
                chechEvents.put(eventName, 1);
            } else {
                // Collect events with pre regions are empty.
                if (preRegionsOfEvents.isEmpty()) {
                    chechEvents.put(eventName, 2);
                }
                // Collect events with post regions are empty.
                if (postRegionsOfEvents.isEmpty()) {
                    chechEvents.put(eventName, 3);
                }
            }
        }
        return chechEvents;
    }

    private static HashMap<String, Integer> checkNonEmptyEventsWayTwo(Set<Region> nonTrivialRegions, final STS sts) {
        HashMap<String, Integer> chechEvents = new HashMap<>();
        Set<String> events = sts.createEvents();
        for (String event : events) {
            HashSet<Region> preRegionsOfEvents = new HashSet<>();
            HashSet<Region> postRegionsOfEvents = new HashSet<>();
            for (Region region : nonTrivialRegions) {
                // collect the set of pre-regions 0e of an event.
                if (region.getEventsLeavingR().contains(event)) {
                    preRegionsOfEvents.add(region);
                }
                // collect the set of post-regions e0 of an event.
                if (region.getEventsEnteringR().contains(event)) {
                    postRegionsOfEvents.add(region);
                }
            }
            // Collect events with both pre-post regions are empty.
            if (preRegionsOfEvents.isEmpty() & postRegionsOfEvents.isEmpty()) {
                chechEvents.put(event, 1);
            } // Collect events with pre-regions are empty.
            if (preRegionsOfEvents.isEmpty()) {
                chechEvents.put(event, 2);
            }
            // Collect events with post regions are empty.
            if (postRegionsOfEvents.isEmpty()) {
                chechEvents.put(event, 3);
            }
        }
        return chechEvents;
    }

    public static boolean checkAxiomTwo(Set<Region> nonTrivialRegions, final STS sts) {
        return checkNonEmptyEventsWayTwo(nonTrivialRegions, sts).isEmpty();

    }
}
