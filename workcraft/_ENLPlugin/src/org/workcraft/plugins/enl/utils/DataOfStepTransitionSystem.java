package org.workcraft.plugins.enl.utils;

import java.util.Set;

import org.workcraft.plugins.enl.Condition;
import org.workcraft.plugins.enl.Transition;
import org.workcraft.plugins.sts.VisualState;
import org.workcraft.types.Pair;

public class DataOfStepTransitionSystem {
    
    Set<Transition> event;
    Set<Transition> transition;
    Pair<Set<Condition>,Set<Condition>> fromStoS;
    Pair<VisualState,VisualState> fromCtoC;

    public DataOfStepTransitionSystem(Pair<Set<Condition>,Set<Condition>> fromStoS, Set<Transition> event){
        this.event = event;
        this.fromStoS = fromStoS;
    }
    
    // using this constructor to convert ENL conditions (ex {b0,b1}) to STS states(q0,q1)
    public DataOfStepTransitionSystem(Set<Transition> transition,Pair<VisualState,VisualState> fromCtoC){
    	this.fromCtoC = fromCtoC;
    	this.transition = transition;
    	
    }
    public Set<Transition> getEvent() {
        return event;
    }

    public Pair<Set<Condition>, Set<Condition>> getPair() {
        return fromStoS;
    }

    public Pair<VisualState, VisualState> getPairOfStates() {
        return fromCtoC;
    }
   
    public Set<Transition> getTransition() {
        return transition;
    }

}
