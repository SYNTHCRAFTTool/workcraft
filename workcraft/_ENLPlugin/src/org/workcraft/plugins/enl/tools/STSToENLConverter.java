package org.workcraft.plugins.enl.tools;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.workcraft.plugins.sts.commands.EventStateSeparationPropertyCommand;
import org.workcraft.plugins.sts.commands.NonEmptyEventVerificationCommand;
import org.workcraft.plugins.sts.commands.ReachabilityVerificationCommand;
import org.workcraft.plugins.sts.commands.StateSeparationPropertyCommand;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.STSUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;

public class STSToENLConverter {
    static VisualENL venl;
    static VisualSTS vsts;
    static STS sts;
    static Map<VisualCondition, Pair<Set<VisualTransition>, Set<VisualTransition>>> convertRegToCond;
    static Set<Region> nonTrivialRegions = new HashSet<>();
    static boolean result = false;
    Set<String> transitions = new HashSet<>();
    static Set<VisualTransition> covertTras = new HashSet<>();
    String msg = "";
    boolean hasLocalities = true;

    public STSToENLConverter(VisualSTS vsts, boolean check) {
        this.vsts = vsts;
        this.sts = vsts.getStepTransitionSystemModel();
        this.venl = new VisualENL(new ENL());

        // Way 1: check axioms A1-A4 for ts.
        // Check axiom A1.
        if (ReachabilityVerificationCommand.checkAxiomOne(sts)) {
            LogUtils.logInfo("The ts satisfies axiom A1.");
            // Check axiom A2.
            Instant start = Instant.now();
            nonTrivialRegions = STS.getNonTrvialRegions(sts);
            Instant end = Instant.now();
            long time = Duration.between(start, end).toMillis();
            STS.recordTime(time);
            if (check)
                applyStratigy231();
            Map<String, Set<Region>> PreRegions = new HashMap<>();
            Map<String, Set<Region>> PostRegions = new HashMap<>();
            STSUtils.getPerPosRegsOfEvents(sts, nonTrivialRegions, PreRegions, PostRegions);
            if (!nonTrivialRegions.isEmpty()) {
                if (NonEmptyEventVerificationCommand.checkAxiomTwo(nonTrivialRegions, sts)) {
                    LogUtils.logInfo("The ts satisfies axiom A2.");
                    HashMap<State, HashSet<Region>> allRq = STSUtils.calcRqForEachState(nonTrivialRegions, sts);
                    // Check axiom A3.
                    if (StateSeparationPropertyCommand.checkAxiomThree(nonTrivialRegions, sts, allRq)) {
                        LogUtils.logInfo("The ts satisfies axiom A3.");
                        this.transitions = sts.createEvents();
                        if (transitions.size() != STS.getELocs().size()) {
                            Set<String> eWithoutLoc = new HashSet<>();
                            for (String event : transitions) {
                                if (!STS.getELocs().containsKey(event)) {
                                    eWithoutLoc.add(event);
                                }
                            }
                            if (!eWithoutLoc.isEmpty()) {
                                hasLocalities = false;
                                String msgLoc = "";
                                if (eWithoutLoc.size() == 1) {
                                    msgLoc = msgLoc + "The event " + eWithoutLoc + "is without an asociated locality.";
                                } else {
                                    msgLoc = msgLoc + "The events " + eWithoutLoc
                                            + " are without asociated localities.";
                                }
                                DialogUtils.showWarning(msgLoc);
                            } // Check axiom A4.
                        } else if (EventStateSeparationPropertyCommand.checkAxiomFour(nonTrivialRegions, sts, allRq,
                                PreRegions, PostRegions)) {
                            LogUtils.logInfo("The ts satisfies axiom A4.");
                            System.out.println("-------------------------------");
                            result = true;
                            if (hasLocalities) {
                                covertTras = convertEvents();
                                convertRegToCond = convertRegions();
                                try {
                                    connectENL();
                                } catch (InvalidConnectionException e) {
                                    // throw new RuntimeException(e);
                                }
                            }
                        } else {
                            msg = msg + " A4";
                        }
                    } else {
                        msg = msg + " A3";
                    }
                } else {
                    msg = msg + " A2";
                }
            } else {
                DialogUtils.showWarning("Can not build a net. There is no non-trivial region");
            }
        } else {
            msg = msg + " A1";
        }
    }

    public String getMessage() {
        return msg;
    }

    public boolean getHasLocalities() {
        return hasLocalities;
    }

    public void setHasLocalities() {
        hasLocalities = true;
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

    /** This method is for getting events and condition of regions. **/
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
                if (sts.getName(state).equals("q0")) {
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

    public static void applyStratigy231() {
        System.out.println("The number of all non-trivial regions are " + nonTrivialRegions.size() + " regions.\n");
        nonTrivialRegions = ReductionRuleTwo.compatibleRegions(nonTrivialRegions, ExtractRts.getStsData(), sts);
        System.out.println(
                "Number of non-trivial regions after applying reduction rule 2 is " + nonTrivialRegions.size());
        if (!STSUtils.isThin(sts))
            nonTrivialRegions = ReductionRuleThree.redundantCompanionRegions(nonTrivialRegions, sts);
        System.out.println(
                "Number of non-trivial regions after applying reduction rule 3 is " + nonTrivialRegions.size());
        nonTrivialRegions = ReductionRuleOne.reduceComplementRegions(nonTrivialRegions, ExtractRts.getallRegsWithCom(),
                sts);
        System.out.println(
                "Number of non-trivial regions after applying reduction rule 1 is " + nonTrivialRegions.size());
        System.out.println("The number of non-trivial regions after applying Reduction rules 2, 3, and 1 are "
                + nonTrivialRegions.size() + " regions.");
//      System.out.println("------------------------------------------");
//      System.out.println("The non-trivial regions after applying rules 2, 3, 1 are: ");
//      ExtractRts.printRegions(sts1, nonTrivialRegions);  
    }

    public VisualENL getENL() {
        return venl;
    }

    public static VisualSTS getFirstSTS() {
        return vsts;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult() {
        result = false;
    }
}
