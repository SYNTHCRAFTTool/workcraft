package org.workcraft.plugins.enl.tools;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.VisualCondition;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.plugins.enl.VisualTransition;
import org.workcraft.plugins.enl.reductionrules.ReductionRuleOne;
import org.workcraft.plugins.enl.reductionrules.ReductionRuleThree;
import org.workcraft.plugins.enl.reductionrules.ReductionRuleTwo;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.DataOfSTS;
import org.workcraft.plugins.sts.utils.STSUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;

public class STSToENLWayTowConverter {

    VisualENL venl;
    VisualSTS vsts, vsts2;
    static STS sts1, sts2;
    static ENLToSTSConverter enlTosts;
    static Map<VisualCondition, Pair<Set<VisualTransition>, Set<VisualTransition>>> convertRegToCond;
    static Set<Region> nonTrivialRegions = new HashSet<>();
    Set<String> transitions = new HashSet<>();
    static Set<VisualTransition> covertTras = new HashSet<>();
    Set<Region> compatibleRegions = new HashSet<>();
    Set<Region> regionsWithTwo = new HashSet<>();
    String msg = "";
    boolean hasLocalities = true;
    boolean isENlsystem = true;
    public STSToENLWayTowConverter(VisualSTS vsts, boolean check) {
        this.vsts = vsts;
        this.sts1 = vsts.getStepTransitionSystemModel();
        this.venl = new VisualENL(new ENL());
        // Method II: check if the step transition system enl_ts is isomorphic to ts.
        this.transitions = sts1.createEvents();
//        STS.setLocalities(transitions);
        if (transitions.size() != STS.getELocs().size()) {
            Set<String> eWithoutLoc = new HashSet<>();
            for (String event : transitions) {
                if (!STS.getELocs().containsKey(event)) {
                    eWithoutLoc.add(event);
                }
            }
            if (!eWithoutLoc.isEmpty()) {
                String msg = "";
                hasLocalities = false;
                if (eWithoutLoc.size() == 1) {
                    msg = msg + "The event " + eWithoutLoc + "is without an asociated locality.";
                } else {
                    msg = msg + "The events " + eWithoutLoc + " are without asociated localities.";
                }
                DialogUtils.showWarning(msg);
            }
        }
        if (hasLocalities) {
            Instant start = Instant.now();
            nonTrivialRegions = STS.getNonTrvialRegions(sts1);
            Instant end = Instant.now();
            long time = Duration.between(start, end).toMillis();
            STS.recordTime(time);
//            System.out.println("Execution time of extracting non-trivial regions is " + time + " MillSeconds");
            if (STSUtils.isENLSystemForMethodII(sts1, nonTrivialRegions)) {
                isENlsystem = true;
                if (check)
                    applyStratigy231();
                covertTras = convertEvents();
                convertRegToCond = convertRegions();
                try {
                    connectENL();
                } catch (InvalidConnectionException e) {
                    // throw new RuntimeException(e);
                }
                enlTosts = new ENLToSTSConverter(venl);
                vsts2 = enlTosts.getSTS();
                sts2 = vsts2.getStepTransitionSystemModel();
            } else {
                isENlsystem = false;
            }
        }
    }

    public boolean getHasLocalities() {
        return hasLocalities;
    }

    public void setHasLocalities() {
        hasLocalities = true;
    }

    public boolean getIsENlsystem() {
        return isENlsystem;
    }

    public void setIsENlsystem() {
        isENlsystem = true;
    }

    public static void applyStratigy231() {
        System.out.println("The number of all non-trivial regions are " + nonTrivialRegions.size() + " regions.");
        nonTrivialRegions = ReductionRuleTwo.compatibleRegions(nonTrivialRegions, ExtractRts.getStsData(), sts1);
        System.out.println("The number of non-trivial regions after applying reduction rule 2 is "
                + nonTrivialRegions.size() + ".");
        if (!STSUtils.isThin(sts1))
            nonTrivialRegions = ReductionRuleThree.redundantCompanionRegions(nonTrivialRegions, sts1);
        System.out.println("The number of non-trivial regions after applying reduction rule 3 is "
                + nonTrivialRegions.size() + ".");
        nonTrivialRegions = ReductionRuleOne.reduceComplementRegions(nonTrivialRegions, ExtractRts.getallRegsWithCom(),
                sts1);
        System.out.println(
                "The number of non-trivial regions after applying reduction rule 1 is " + nonTrivialRegions.size());
        System.out.println();
        System.out.println("The number of non-trivial regions after applying Reduction rules 2, 3, and 1 are "
                + nonTrivialRegions.size() + " regions.");
//      System.out.println("------------------------------------------");
//      System.out.println("The non-trivial regions after applying rules 2, 3, 1 are: ");
//      ExtractRts.printRegions(sts1, nonTrivialRegions);  
    }

    /** This method is for creating all events. **/
    private Set<VisualTransition> convertEvents() {
        Set<VisualTransition> events = new HashSet<>();
        Map<VisualTransition, Integer> eventsWithLocs = new HashMap<>();
        for (String eventName : transitions) {
            VisualTransition event = venl.createTransition(eventName, null);
            event.getReferencedTransition().setLocality(STS.getELocs().get(eventName));
            events.add(event);
            eventsWithLocs.put(event, STS.getELocs().get(eventName));
        }
        for (Integer loc : STS.getLocalities()) {
            Color locColor = new Color((int) (Math.random() * 0x1000000));

            for (VisualTransition e : events) {
                if (e.getReferencedTransition().getLocality() == loc) {
                    e.setFillColor(locColor);
                }
            }
        }
        return events;
    }

    /** This method is for getting the events and the conditions of regions. **/
    private Map<VisualCondition, Pair<Set<VisualTransition>, Set<VisualTransition>>> convertRegions() {
        Map<VisualCondition, Pair<Set<VisualTransition>, Set<VisualTransition>>> convertReg = new HashMap<>();
        int regCount = 0;
        Collection<VisualTransition> transition = Hierarchy.getDescendantsOfType(venl.getRoot(),
                VisualTransition.class);
        for (Region reg : nonTrivialRegions) {
            Set<VisualTransition> TransInCond = new HashSet<>();
            Set<String> namesIn = reg.getEventsEnteringR();
            for (String in : namesIn) {
                if (!in.equals(null)) {
                    for (VisualTransition tra1 : transition) {
                        String nameIn = venl.getMathModel().getNodeReference(tra1.getReferencedTransition());
                        if (!nameIn.equals(null)) {
                            if (nameIn.equals(in)) {
                                TransInCond.add(tra1);
                            }
                        }
                    }
                }
            }
            Set<VisualTransition> TransOutCond = new HashSet<>();
            Set<String> namesOut = reg.getEventsLeavingR();
            for (String out : namesOut) {
                if (!out.equals(null)) {
                    for (VisualTransition tra2 : transition) {
                        String nameOut = venl.getMathModel().getNodeReference(tra2.getReferencedTransition());
                        if (!nameOut.equals(null)) {
                            if (nameOut.equals(out)) {
                                TransOutCond.add(tra2);
                            }
                        }
                    }
                }
            }

            /** Create condition from the region **/
            String conditionName = "r" + (++regCount);
            VisualCondition condition = venl.createCondition(conditionName, null);
            for (State state : reg.getRinRegion()) {
                if (sts1.getName(state).equals("q0")) {
                    int value = 1;
                    condition.getReferencedCondition().setTokens(value);
                }
            }
            convertReg.put(condition, new Pair<>(TransInCond, TransOutCond));
        }
        return convertReg;
    }

    private void connectENL() throws InvalidConnectionException {
        for (Entry<VisualCondition, Pair<Set<VisualTransition>, Set<VisualTransition>>> entry : convertRegToCond
                .entrySet()) {
            for (VisualTransition tra1 : entry.getValue().getFirst()) {
                if (!tra1.equals(null)) {
                    VisualConnection fromTtoC = venl.connect(tra1, entry.getKey());
                    Point2D pos = getBestPredPosition(venl, tra1);
                    entry.getKey().setPosition(pos);
                }
            }
            for (VisualTransition tra2 : entry.getValue().getSecond()) {
                if (!tra2.equals(null)) {

                    Point2D pos = getBestSuccPosition(venl, tra2);
                    entry.getKey().setPosition(pos);
                    VisualConnection fromCtoT = venl.connect(entry.getKey(), tra2);
                }
            }
        }
    }

    private Point2D getBestPredPosition(VisualModel model, VisualTransformableNode node) {
        double dx = 0.0;
        double dy = 0.0;
        int count = 0;
        for (Connection connection : model.getConnections(node)) {
            Node second = connection.getSecond();
            if ((second != node) && (connection instanceof VisualConnection)) {
                Point2D pos = ((VisualConnection) connection).getMiddleSegmentCenterPoint();
                dx += pos.getX() - node.getX();
                dy += pos.getY() - node.getY();
                count++;
            }
        }
        double x = (count > 0) ? node.getX() - dx / count : node.getX() + 5.0;
        double y = (count > 0) ? node.getY() - dy / count : node.getY();
        return new Point2D.Double(x, y);
    }

    private Point2D getBestSuccPosition(VisualModel model, VisualTransformableNode node) {
        double dx = 0.0;
        double dy = 0.0;
        int count = 0;
        for (Connection connection : model.getConnections(node)) {
            Node first = connection.getFirst();
            if ((first != node) && (connection instanceof VisualConnection)) {
                Point2D pos = ((VisualConnection) connection).getMiddleSegmentCenterPoint();
                dx += node.getX() - pos.getX();
                dy += node.getY() - pos.getY();
                count++;
            }
        }
        double x = (count > 0) ? node.getX() + dx / count : node.getX() - 5.0;
        double y = (count > 0) ? node.getY() + dy / count : node.getY();
        return new Point2D.Double(x, y);
    }

    public VisualENL getENL() {
        return venl;
    }

    public static boolean isIsomorphic() {
        boolean result = false;
        ArrayList<DataOfSTS> getStepsqToqOfsts1 = new ArrayList<>();
        ArrayList<DataOfSTS> getStepsqToqOfsts2 = new ArrayList<>();
        getStepsqToqOfsts1 = sts1.getStepsWithqToq(sts1);
        getStepsqToqOfsts2 = sts2.getStepsWithqToq(sts2);
        int check = 0;
        // Check if the number of steps in sts1 is equal to the number of steps in sts2.
        if (getStepsqToqOfsts1.size() == getStepsqToqOfsts2.size()) {
            Map<State, Set<Set<String>>> stsOneMap = new HashMap<>();
            Map<State, Set<Set<String>>> stsTwoMap = new HashMap<>();
            for (State q1 : sts1.getStates()) {
                Set<Set<String>> allStepsOutq = new HashSet<>();
                for (DataOfSTS qToq1 : getStepsqToqOfsts1) {
                    if (qToq1.getPairOfState().getFirst().equals(q1))
                        allStepsOutq.add(qToq1.getStep());
                }
                stsOneMap.put(q1, allStepsOutq);
            }
            for (State q1 : sts2.getStates()) {
                Set<Set<String>> allStepsOutq = new HashSet<>();
                for (DataOfSTS qToq1 : getStepsqToqOfsts2) {
                    if (qToq1.getPairOfState().getFirst().equals(q1))
                        allStepsOutq.add(qToq1.getStep());
                }
                stsTwoMap.put(q1, allStepsOutq);
            }
            Set<State> q = new HashSet<>();
            for (Set<Set<String>> steps1 : stsOneMap.values()) {
                for (Map.Entry<State, Set<Set<String>>> steps2 : stsTwoMap.entrySet()) {
                    if (steps1.equals(steps2.getValue()) & !q.contains(steps2.getKey())) {
                        q.add(steps2.getKey());
                        check = check + 1;
                        break;
                    }
                }
            }
            if (check == sts1.getStates().size() & sts1.getStates().size() == sts2.getStates().size()) {
                result = true;
            }
        }
        return result;
    }
}
