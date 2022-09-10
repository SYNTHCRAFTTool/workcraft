package org.workcraft.plugins.sts;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.plugins.sts.extractingregions.ExtractRts;
import org.workcraft.plugins.sts.utils.DataOfSTS;
import org.workcraft.serialisation.References;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;

public class STS extends AbstractMathModel implements STSModel {
    public static Map<String, Integer> eventsWithLocalities = new HashMap<>();
    public static Set<EventWithLocality> eventsWitlLoca = new HashSet<>();
    public static Set<String> E = new HashSet<>();

    public STS() {
        this(null, null);
//        eventsWithLocalities.clear();
    }

    public STS(Container root, References refs) {
        super(root, refs);
//        eventsWithLocalities.clear();
    }

    public final State createState(String name, Container container) {
        if (container == null) {
            container = getRoot();
        }
        State state = new State();
        container.add(state);
        if (name != null) {
            setName(state, name);
        }
        return state;
    }

    public TransitionArc connect(State first, State second) {
        TransitionArc con = new TransitionArc(first, second);
        getRoot().add(con);
        return con;
    }

    public State getInitialState() {
        for (State state : getStates()) {
            if (state.isInitial()) {
                return state;
            }
        }
        return null;
    }

    public Set<VisualLocalities> setAllLocalities() {
        Set<VisualLocalities> allLocalities = new HashSet<>();
        for (int i = 0; i <= createEvents().size(); i++) {
            Localities localities = new Localities();
            VisualLocalities vLocalities = new VisualLocalities(localities);
            allLocalities.add(vLocalities);
            eventsWithLocalities.put(vLocalities.getName(), vLocalities.getLocality());
        }
        return allLocalities;
    }

    @Override
    public final Collection<State> getStates() {
        return Hierarchy.getDescendantsOfType(getRoot(), State.class);
    }

    public Collection<TransitionArc> getArcs() {
        return Hierarchy.getDescendantsOfType(getRoot(), TransitionArc.class);
    }

    public static void recordTime(long time) {
        PrintWriter ExtractingRegionsTime = null;
        try {
            ExtractingRegionsTime = new PrintWriter(new BufferedWriter(
                    new FileWriter(System.getProperty("user.home") + "/Downloads/Extracting_Regions_Time.csv", true)));
        } catch (IOException e1) {
            // Auto-generated catch block
            e1.printStackTrace();
        }
        ExtractingRegionsTime.println(time);

        ExtractingRegionsTime.close();
    }
   
    @Override
    public final void fire() {
        fire(this);
    }

    // We need this method for simulation tool.
    public static final void fire(STS sts) {
        Instant start = Instant.now();
        ExtractRts.extractingNonTrivialRegions(sts);
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();
        System.out.println("Execution time is " + time + " milliseconds");
        recordTime(time);
    }

    // We need this method for converting STS to ENL.
    public static Set<Region> getNonTrvialRegions(STS sts) {
        return ExtractRts.extractingNonTrivialRegions(sts);
    }

    // This method is used to collect array of each step with its states (q-->q' U).
    // Ignoring non-singleton steps.
    public ArrayList<DataOfSTS> getStepsWithqToqForThin(STS sts) {
        ArrayList<DataOfSTS> fristStepSecond = new ArrayList<>();
        for (TransitionArc connection : Hierarchy.getDescendantsOfType(getRoot(), TransitionArc.class)) {
            if (connection instanceof TransitionArc) {
                Set<String> step = new HashSet<>();
                String name = connection.getName();
                if (name != null) {
                    if (!name.contains("{")) {
                        step.add(name);
                        if (step.size() != 0) {
                            State q0 = (State) connection.getFirst();
                            State q1 = (State) connection.getSecond();
                            DataOfSTS t = new DataOfSTS(step, new Pair<>(q0, q1));
                            fristStepSecond.add(t);
                        }
                    }
                }
            }
        }
        // Print q-->q' U. It calls twice in the code.
//      for (DataOfSTS step : fristStepSecond) {
//          System.out.print("" + sts.getNodeReference(step.getPairOfState().getFirst()));
//          System.out.print("--->" + sts.getNodeReference(step.getPairOfState().getSecond()));
//          System.out.print(" U=");
//          for (String s1 : step.getStep()) {
//              if (!s1.isEmpty())
//                  System.out.print(s1);
//          }
//          System.out.println(" ");
//      }
        return fristStepSecond;
    }

    // This method is used to collect array of each step with its states (q-->q' U).
    public ArrayList<DataOfSTS> getStepsWithqToq(STS sts) {
        ArrayList<DataOfSTS> fristStepSecond = new ArrayList<>();
        for (TransitionArc connection : Hierarchy.getDescendantsOfType(getRoot(), TransitionArc.class)) {
            if (connection instanceof TransitionArc) {
                Set<String> step = new HashSet<>();
                String name = connection.getName();
                String eventName = "";
                if (name != null) {
                    if (!name.contains("{")) {
                        step.add(name);
                        if (step.size() != 0) {
                            State q0 = (State) connection.getFirst();
                            State q1 = (State) connection.getSecond();
                            fristStepSecond.add(new DataOfSTS(step, new Pair<>(q0, q1)));
                        }
                    } else {
                        for (int i = 1; i <= name.length() - 1; i++) {

                            if (name.charAt(i) != '}') {

                                if (name.charAt(i) != ',') {
                                    eventName = eventName + name.charAt(i);
                                } else {
                                    step.add(eventName);
                                    eventName = "";
                                }
                            } else {
                                step.add(eventName);
                                eventName = "";
                            }
                        }
                        if (step.size() != 0) {
                            State q0 = (State) connection.getFirst();
                            State q1 = (State) connection.getSecond();
                            DataOfSTS t = new DataOfSTS(step, new Pair<>(q0, q1));
                            fristStepSecond.add(t);
                        }
                    }
                }
            }
        }
        // Print q-->q' U. It calls twice in the code.
//		for (DataOfSTS step : fristStepSecond) {
//			System.out.print("" + sts.getNodeReference(step.getPairOfState().getFirst()));
//			System.out.print("--->" + sts.getNodeReference(step.getPairOfState().getSecond()));
//			System.out.print(" U=");
//			for (String s1 : step.getStep()) {
//				if (!s1.isEmpty())
//					System.out.print(s1);
//			}
//			System.out.println(" ");
//		}
        return fristStepSecond;
    }

    // This method is used to collect all events in the st, ex: E={e,f,g,h}.
    public Set<String> createEvents() {
        Set<String> events = new HashSet<>();
        for (TransitionArc connection : Hierarchy.getDescendantsOfType(getRoot(), TransitionArc.class)) {
            if (connection instanceof TransitionArc) {
                String name = connection.getName();
                String eventName = "";
                if (name != null) {
                    if (!name.contains("{")) {
                        events.add(name);
                    } else {
                        for (int i = 1; i <= name.length() - 1; i++) {
                            if (name.charAt(i) != '}') {
                                if (name.charAt(i) != ',') {
                                    eventName = eventName + name.charAt(i);
                                } else {
                                    events.add(eventName);
                                    eventName = "";
                                }
                            } else {
                                events.add(eventName);
                                eventName = "";
                            }
                        }
                    }
                }
            }
        }
        E.addAll(events);
        return events;
    }

    public Set<String> getEvents() {
        return E;
    }

    public static Set<EventWithLocality> getELocs2() {
        return eventsWitlLoca;
    }

    public static void setEventsLocalities(Map<String, Integer> eventsWithLocalities) {
        STS.eventsWithLocalities = eventsWithLocalities;
    }

    public static void setClearLocalities() {
        STS.eventsWithLocalities.clear();
    }

    public static Map<String, Integer> getELocs() {
        return eventsWithLocalities;
    }

    public static Map<Integer, Set<String>> getEventsLocalities() {
        Map<Integer, Set<String>> eventsLocalities = new HashMap<>();
        Map<String, Integer> eventWithLoc = getELocs();
        for (Integer loc : getLocalities()) {
            Set<String> eventsWithOneLoc = new HashSet<>();
            for (Map.Entry<String, Integer> eWithLoc : eventWithLoc.entrySet()) {
                if (eWithLoc.getValue() == loc) {
                    eventsWithOneLoc.add(eWithLoc.getKey());
                }
            }
            eventsLocalities.put(loc, eventsWithOneLoc);
        }
        return eventsLocalities;
    }

    // This method needed for message (ts satisfies A1-A4 w.r.t ... this )
    public static Set<Set<String>> getColocationRelations() {
        Set<Set<String>> eventsOneLocalities = new HashSet<>();
        for (Entry<Integer, Set<String>> e : getEventsLocalities().entrySet()) {
            eventsOneLocalities.add(e.getValue());
        }
        return eventsOneLocalities;
    }

    public static Set<Integer> getLocalities() {
        Set<Integer> localities = new HashSet<>();
        for (Entry<String, Integer> e : eventsWithLocalities.entrySet()) {
            localities.add(e.getValue());
        }
        return localities;
    }

    public static Map<String, Integer> setLocalities(Set<String> events) {
        getELocs();
        return eventsWithLocalities;
    }

    /** This method is used to collect array of each step with its states (q-->q' U).
        Ignoring non-singleton steps.**/
    public static boolean getArcsWithIgnoringThichArcs(STS sts, ArrayList<DataOfSTS> stsData, Set<String> events) {
        boolean result = false;
//    Map<String, Integer> eventsWithLoc = setLocalities(events);
      Map<String, Integer> eventsWithLoc = getELocs();
        if (!eventsWithLoc.isEmpty() & eventsWithLoc.size() == events.size()) {
            Set<State> allStates = new HashSet<>();
            allStates.addAll(sts.getStates());
            Map<State, Set<Set<String>>> allStepsStates = sts.getAllStepsOfStates(sts, allStates);
            for (Map.Entry<State, Set<Set<String>>> entry : allStepsStates.entrySet()) {
                for (Set<String> U : entry.getValue()) {
                    if (U.size() > 1) {
                        ArrayList<Integer> listOfLocalities = new ArrayList<>();
                        for (String e : U) {
                            if (!listOfLocalities.contains(eventsWithLoc.get(e))) {
                                listOfLocalities.add(eventsWithLoc.get(e));
                            } else {
                                result = true;
                                break;
                            }
                        }
                    }
                    if (result)
                        break;
                }
                if (result)
                    break;
            }
        } else {
            LogUtils.logInfo("No localities");
        }
        return result;
    }

    // Calculate all Steps q is the set of all steps labelling arcs outgoing from q.
    public Map<State, Set<Set<String>>> getAllStepsOfStates(STS sts, Set<State> states) {
        Map<State, Set<Set<String>>> allStepsq = new HashMap<>();
        ArrayList<DataOfSTS> allSteps = getStepsWithqToq(sts);
        for (State state : states) {
            Set<Set<String>> allStepsOfq = new HashSet<>();
            for (DataOfSTS qStepq : allSteps) {
                if (qStepq.getPairOfState().getFirst().equals(state)) {
                    allStepsOfq.add(qStepq.getStep());
                }
            }
            allStepsq.put(state, allStepsOfq);
        }
        return allStepsq;
    }

    public static String getCoRe() {
        String msg = " = \n ";
        int k = 0;
        Set<Set<String>> allColoRelation = STS.getColocationRelations();
        for (Set<String> equClass : allColoRelation) {
            k++;
            String oneLoc = "{";
            int i = 0;
            for (String e : equClass) {
                i++;
                oneLoc = oneLoc + e + (i < equClass.size() ? "," : "");
            }
            oneLoc = oneLoc + "}";
            oneLoc = oneLoc + " \u00d7 " + oneLoc;
            if (oneLoc.length() > 20)
                oneLoc = oneLoc + "\n";
            msg = msg + oneLoc + (k < allColoRelation.size() ? " \u222A " : "");
        }
        return msg;
    }
}
