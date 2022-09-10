package org.workcraft.plugins.sts.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.DataOfSTS;
import org.workcraft.plugins.sts.utils.STSUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class EventStateSeparationPropertyCommand extends AbstractVerificationCommand {
    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Check event/state separation property with local maximal concurrency [axiom-A4]";
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
        Set<DataOfSTS> EventStateSeparationProperty = checkEventStateSeparationProperty(sts);
        String msg = "";
        if (EventStateSeparationProperty.isEmpty()) {
            DialogUtils.showInfo(
                    "The ts satisfies axiom A4 [Event/State Separation Property and maximal execution manner].", TITLE);
        } else {
            msg = msg + "The ts does not satisfy axiom A4. ";

            if (EventStateSeparationProperty.size() == 1) {
                msg = msg + "There is a missing transition:" + "\n";
            } else {
                msg = msg + "There are missing transitions:" + "\n";
            }
            for (DataOfSTS qu : EventStateSeparationProperty) {
                msg = msg + "(" + sts.getName(qu.getPairOfState().getFirst()) + ",{";
                int i = 0;
                for (String step : qu.getStep()) {
                    i++;
                    msg = msg + step;
                    String s = (i < qu.getStep().size() ? "," : "");
                    msg = msg + s;
                }
                msg = msg + "}," + sts.getName(qu.getPairOfState().getSecond()) + ")." + "\n\n";
            }
            DialogUtils.showWarning(msg, TITLE);
//            }
//            msg = msg + "\n Select states violating State Separation Property?\n";
//            if (DialogUtils.showConfirmInfo(msg, TITLE, true)) {
//                final Framework framework = Framework.getInstance();
//                final MainWindow mainWindow = framework.getMainWindow();
//                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
//                VisualSTS visualSts = WorkspaceUtils.getAs(we, VisualSTS.class);
//                SelectionHelper.selectByReferencedComponents(visualSts, states);
//            }
        }
        return EventStateSeparationProperty.isEmpty();
    }

    // check state separation property for axiom A4.
    public static Set<DataOfSTS> checkEventStateSeparationProperty(final STS sts) {
        Set<Region> nonTrivialRegions = STS.getNonTrvialRegions(sts);
        Set<DataOfSTS> result = new HashSet<>();
        HashMap<State, HashSet<Region>> allRq = calcRqForEachState(nonTrivialRegions, sts);
        Map<String, Set<Region>> PreRegions = new HashMap<>();
        Map<String, Set<Region>> PostRegions = new HashMap<>();
        STSUtils.getPerPosRegsOfEvents(sts, nonTrivialRegions, PreRegions, PostRegions);
        result = checkEventStateSeparationPropertyWayTwo(nonTrivialRegions, sts, allRq, PreRegions, PostRegions);
        return result;
    }

    public static boolean checkAxiomFour(Set<Region> nonTrivialRegions, final STS sts,
            HashMap<State, HashSet<Region>> allRq, Map<String, Set<Region>> PreRegions,
            Map<String, Set<Region>> PostRegions) {
        return checkEventStateSeparationPropertyWayTwo(nonTrivialRegions, sts, allRq, PreRegions, PostRegions)
                .isEmpty();
    }

    // check event/state separation property for axiom A4.
    public static Set<DataOfSTS> checkEventStateSeparationPropertyWayTwo(Set<Region> nonTrivialRegions, final STS sts,
            HashMap<State, HashSet<Region>> allRq, Map<String, Set<Region>> PreRegions,
            Map<String, Set<Region>> PostRegions) {
        Set<DataOfSTS> result = new HashSet<>();
        Set<DataOfSTS> qU = new HashSet<>();
        Set<Set<String>> allPotenialSteps = setOfPotenialSteps(sts, sts.createEvents(),
                conflictedEvents(sts, nonTrivialRegions, PreRegions, PostRegions));
        HashMap<Set<String>, HashSet<Region>> preu = calcPreRegForEachStep(allPotenialSteps, nonTrivialRegions);
        HashMap<Set<String>, HashSet<Region>> postu = calcPostRegForEachStep(allPotenialSteps, nonTrivialRegions);
        for (Entry<State, HashSet<Region>> Rq : allRq.entrySet()) {
            for (Set<String> u : allPotenialSteps) {
                if (!u.isEmpty()) {
                    HashSet<Region> preRegions = preu.get(u);
                    HashSet<Region> RqRegios = Rq.getValue();
                    int count = 0;
                    for (Region r : preRegions) {
                        if (RqRegios.contains(r)) {
                            count++;
                        }
                    }
                    if (count == preRegions.size()) {
                        List<Region> postuRq = postu.get(u).stream().filter(Rq.getValue()::contains)
                                .collect(Collectors.toList());
                        if (postuRq.isEmpty()) {
                            State q1 = null;
                            DataOfSTS qu = new DataOfSTS(u, new Pair<>(Rq.getKey(), q1));
                            qU.add(qu);
                        }
                    }
                }
            }
        }
        Set<DataOfSTS> SourceStep = checkEnlagedSteps(sts, nonTrivialRegions, qU, allRq, PreRegions, PostRegions);
        Set<DataOfSTS> qStepq = calcTargetsForEachTransition(allRq, preu, postu, SourceStep, sts);
        ArrayList<DataOfSTS> transitions = sts.getStepsWithqToq(sts);
        for (DataOfSTS qWithU : qStepq) {
            if (!transitions.stream()
                    .anyMatch(o -> o.getPairOfState().getFirst().equals(qWithU.getPairOfState().getFirst())
                            & o.getStep().equals(qWithU.getStep()))) {
                result.add(qWithU);
            }
        }
        if (!result.isEmpty()) {
            int o = 0;
            if (result.size() == 1) {
                LogUtils.logError("There is a missing transition: ");
            } else {
                LogUtils.logError("There are missing transitions: ");
            }
            for (DataOfSTS qu : result) {
                System.out.print(++o);
                System.out.print(o < 10 ? " -(" : "-(");
                System.out.print(qu.getPairOfState().getFirst() == null ? "\u2205"
                        : sts.getName(qu.getPairOfState().getFirst()));
                System.out.print(",{");
                int i = 0;
                for (String step : qu.getStep()) {
                    i++;
                    System.out.print(step);
                    System.out.print(i < qu.getStep().size() ? "," : "");
                }
                System.out.print("},");
                System.out.print(qu.getPairOfState().getSecond() == null ? "\u2205"
                        : sts.getName(qu.getPairOfState().getSecond()));
                System.out.println(").");
            }
            System.out.println();
        }
        return result;
    }

    // Calculate if q ----> q' then Rq\Rq' = 0u and Rq'\Rq = u0.
    public static Set<DataOfSTS> calcTargetsForEachTransition(HashMap<State, HashSet<Region>> allRq,
            HashMap<Set<String>, HashSet<Region>> preu, HashMap<Set<String>, HashSet<Region>> postu, Set<DataOfSTS> qUq,
            STS sts) {
        for (DataOfSTS qu : qUq) {
            // Get Rq
            HashSet<Region> Rq = allRq.get(qu.getPairOfState().getFirst());
            // Get 0u
            HashSet<Region> preStep = preu.get(qu.getStep());
            // Get u0
            HashSet<Region> postStep = postu.get(qu.getStep());
            // Get q'
            for (Entry<State, HashSet<Region>> toq : allRq.entrySet()) {
                if (!toq.getKey().equals(qu.getPairOfState().getFirst())) {
                    // Get Rq'
                    HashSet<Region> Rtoq = toq.getValue();
                    HashSet<Region> inRqnotRtoq = new HashSet<>();
                    HashSet<Region> inRtoqnotRq = new HashSet<>();
                    // Calculate Rq\Rq'
                    for (Region rq : Rq) {
                        if (!Rtoq.contains(rq)) {
                            inRqnotRtoq.add(rq);
                        }
                        // Calculate Rq'\Rq
                        for (Region rToq : Rtoq) {
                            if (!Rq.contains(rToq)) {
                                inRtoqnotRq.add(rToq);
                            }
                        }
                    }
                    // Check if Rq\Rq' = 0u and Rq'\Rq = u0.
                    if (inRqnotRtoq.equals(preStep) & inRtoqnotRq.equals(postStep)) {
                        // If yes, set (q,u,q')
                        qu.setPairOfState(new Pair<>(qu.getPairOfState().getFirst(), toq.getKey()));
                        break;
                    }
                }
            }
        }
        return qUq;
    }

    public static Set<DataOfSTS> checkEnlagedSteps(STS sts, Set<Region> nonTrivialRegions, Set<DataOfSTS> qU,
            HashMap<State, HashSet<Region>> allRq, Map<String, Set<Region>> PreRegions,
            Map<String, Set<Region>> PostRegions) {
        Set<DataOfSTS> qUtoRemove = new HashSet<>();
        Map<String, Set<String>> conflictedEvent = conflictedEvents(sts, nonTrivialRegions, PreRegions, PostRegions);
        Map<Integer, Set<String>> eventsWithLoc = STS.getEventsLocalities();
        for (DataOfSTS qu : qU) {
            for (Integer loc : STS.getLocalities()) {
                Set<String> eventsLocinStep = getEventsLocinStep(sts, qu.getStep(), loc);
                if (!eventsLocinStep.isEmpty()) {
                    // if e is already in step
                    for (String event : eventsWithLoc.get(loc)) {
                        if (qu.getStep().contains(event)) {
                            continue;
                        } else { // if e is not in step, but has conflict
                            boolean hasConflict = false;
                            for (String e : qu.getStep()) {
                                if (!conflictedEvent.get(event).isEmpty()) {
                                    if (conflictedEvent.get(event).contains(e)) {
                                        hasConflict = true;
                                        continue;
                                    }
                                }
                            }
                            if (!hasConflict & iseEnabledAtRq(sts, nonTrivialRegions,
                                    allRq.get(qu.getPairOfState().getFirst()), event)) {
                                qUtoRemove.add(qu);
                                break;
                            }
                        }
                    }
                }
            }
        }
        qU.removeAll(qUtoRemove);

        return qU;
    }

    public static boolean iseEnabledAtRq(STS sts, Set<Region> nonTrivialRegions, HashSet<Region> Rq, String event) {
        boolean result = false;
        HashSet<Region> preRegionsOfEvent = new HashSet<>();
        HashSet<Region> postRegionsOfEvent = new HashSet<>();
        for (Region region : nonTrivialRegions) {
            // collect the set of pre-regions 0e of an event.
            if (region.getEventsLeavingR().contains(event)) {
                preRegionsOfEvent.add(region);
            }
            // collect the set of post-regions e0 of an event.
            if (region.getEventsEnteringR().contains(event)) {
                postRegionsOfEvent.add(region);
            }
        }
        // 1. Check 0e part of Rq.
        int count = 0;
        for (Region r : preRegionsOfEvent) {
            if (Rq.contains(r)) {
                count++;
            }
        }
        // If 1 is true, check e0 /\ Rq.
        if (count == preRegionsOfEvent.size()) {
            List<Region> posteRq = postRegionsOfEvent.stream().filter(Rq::contains).collect(Collectors.toList());
            if (posteRq.isEmpty()) {
                result = true;
            }
        }
        return result;
    }

    public static Set<String> getEventsLocinStep(STS sts, Set<String> u, Integer loc) {
        Set<String> eventsLocinStep = new HashSet<>();
        Set<String> eWithLoc = STS.getEventsLocalities().get(loc);
        for (String event : eWithLoc) {
            if (u.contains(event)) {
                eventsLocinStep.add(event);
            }
        }
        return eventsLocinStep;
    }

    // Calculate Rq for every state q. Rq is the set of all non-trivial regions
    // containing state q for (axiom A4).
    public static HashMap<State, HashSet<Region>> calcRqForEachState(Set<Region> nonTrivialRegions, STS sts) {
        HashMap<State, HashSet<Region>> Rq = new HashMap<State, HashSet<Region>>();
        for (State q : sts.getStates()) {
            HashSet<Region> reg = new HashSet<>();
            Rq.put(q, reg);
            for (Region r : nonTrivialRegions) {
                if (r.getRinRegion().contains(q)) {
                    reg.add(r);
                }
            }
        }
        return Rq;
    }

    // Calculate 0u (pre-regions) for every potential step for (axiom A4).
    public static HashMap<Set<String>, HashSet<Region>> calcPreRegForEachStep(Set<Set<String>> allPotenialSteps,
            Set<Region> nonTrivialRegions) {
        HashMap<Set<String>, HashSet<Region>> preu = new HashMap<>();
        for (Set<String> step : allPotenialSteps) {
            if (!step.isEmpty()) {
                HashSet<Region> preRegions = new HashSet<>();
                for (Region r : nonTrivialRegions) {
                    for (String s : step) {
                        if (r.getEventsLeavingR().contains(s)) {
                            preRegions.add(r);
                        }
                    }
                }
                preu.put(step, preRegions);
            }
        }
        return preu;
    }

    // Calculate u0 (post-regions) for every potential step for (axiom A4).
    public static HashMap<Set<String>, HashSet<Region>> calcPostRegForEachStep(Set<Set<String>> allPotenialSteps,
            Set<Region> nonTrivialRegions) {
        HashMap<Set<String>, HashSet<Region>> postu = new HashMap<>();
        for (Set<String> step : allPotenialSteps) {
            if (!step.isEmpty()) {
                HashSet<Region> postRegions = new HashSet<>();
                for (Region r : nonTrivialRegions) {
                    for (String s : step) {
                        if (r.getEventsEnteringR().contains(s)) {
                            postRegions.add(r);
                        }
                    }
                }
                postu.put(step, postRegions);
            }
        }
        return postu;
    }

    // Return set of all potential steps without conflicting.
    public static Set<Set<String>> setOfPotenialSteps(STS sts, Set<String> originalSet,
            Map<String, Set<String>> conflictedEvent) {
        Set<Set<String>> sets = new HashSet<Set<String>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<String>());
            return sets;
        }
        List<String> list = new ArrayList<String>(originalSet);
        String head = list.get(0);
        Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
        for (Set<String> set : setOfPotenialSteps(sts, rest, conflictedEvent)) {
            Set<String> newSet = new HashSet<String>();
            newSet.add(head);
            newSet.addAll(set);
            if (hasConflict(sts, head, newSet, conflictedEvent)) {
                sets.add(set);
            } else {
                sets.add(newSet);
                sets.add(set);
            }
        }
        return sets;
    }

    // Check if the newSet has events that in conflict with head, or
    // verse.
    public static boolean hasConflict(STS sts, String head, Set<String> newSet,
            Map<String, Set<String>> conflictedEvent) {
        boolean result = false;
        Set<String> events = conflictedEvent.get(head);
        Set<String> headInString = new HashSet<>();
        headInString.add(head);
        if (!headInString.equals(newSet)) {
            if (!events.isEmpty()) {
                for (String step : events) {
                    // Check if newSet has any event that cannot be with e in potential step.
                    if (newSet.contains(step)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    // Building a map to connect each e with any other event in conflicting with it.
    public static Map<String, Set<String>> conflictedEvents(STS sts, Set<Region> nonTrivialRegions,
            Map<String, Set<Region>> PreRegions, Map<String, Set<Region>> PostRegions) {
        Map<String, Set<String>> allConflictedEvents = new HashMap<>();
        for (String e : sts.createEvents()) {
            Set<String> eventsConflicted = new HashSet<>();
            allConflictedEvents.put(e, eventsConflicted);
            Set<Region> ePrePostRegions = new HashSet<>();
            if (PreRegions.get(e) != null)
                ePrePostRegions.addAll(PreRegions.get(e));
            if (PostRegions.get(e) != null)
                ePrePostRegions.addAll(PostRegions.get(e));
            for (String f : sts.createEvents()) {
                if (!f.equals(e)) {
                    if (!allConflictedEvents.containsKey(f)) {
                        Set<Region> fPrePostRegions = new HashSet<>();
                        if (PreRegions.get(f) != null)
                            fPrePostRegions.addAll(PreRegions.get(f));
                        if (PostRegions.get(f) != null)
                            fPrePostRegions.addAll(PostRegions.get(f));
                        // 1. Check 0e\/e0 \/ 0f/\f0
                        List<Region> ef = ePrePostRegions.stream().filter(fPrePostRegions::contains)
                                .collect(Collectors.toList());
                        if (!ef.isEmpty()) {
                            // If 1 is not empty, then e & f are conflicted.
                            eventsConflicted.add(f);
                        }
                    } else if (allConflictedEvents.containsKey(f) & !allConflictedEvents.get(f).contains(e)) {
                        Set<Region> fPrePostRegions = new HashSet<>();
                        if (PreRegions.get(f) != null)
                            fPrePostRegions.addAll(PreRegions.get(f));
                        if (PostRegions.get(f) != null)
                            fPrePostRegions.addAll(PostRegions.get(f));
                        // 1. Check 0e\/e0 \/ 0f/\f0
                        List<Region> ef = ePrePostRegions.stream().filter(fPrePostRegions::contains)
                                .collect(Collectors.toList());
                        if (!ef.isEmpty()) {
                            // If 1 is not empty, then e & f are conflicted.
                            eventsConflicted.add(f);
                        }
                    } else if (allConflictedEvents.containsKey(f) & allConflictedEvents.get(f).contains(e)) {
                        eventsConflicted.add(f);
                    }
                }
            }
        }
        return allConflictedEvents;
    }
}
