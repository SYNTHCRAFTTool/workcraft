package org.workcraft.plugins.sts.utils;

import java.util.Set;

import org.workcraft.plugins.sts.State;
import org.workcraft.types.Pair;

public class DataOfSTS {
    Pair<Set<State>, Set<State>> fromStoS;
    Pair<State, State> fStoS;
    Set<String> step;
    String stepString;

    public DataOfSTS(Pair<Set<State>, Set<State>> fromStoS, Set<String> step) {
        this.fromStoS = fromStoS;
        this.step = step;
    }

    public void setPairOfStates(Pair<Set<State>, Set<State>> fromStoS) {
        this.fromStoS = fromStoS;
    }

    public Pair<Set<State>, Set<State>> getPairOfStates() {
        return fromStoS;
    }

    public Set<String> getStep() {
        return step;
    }

    public void setStep(Set<String> step) {
        this.step = step;
    }

    // Second Constructor.
    public DataOfSTS(Set<String> step, Pair<State, State> fStoS) {
        this.fStoS = fStoS;
        this.step = step;
    }

    public void setPairOfState(Pair<State, State> fStoS) {
        this.fStoS = fStoS;
    }

    public Pair<State, State> getPairOfState() {
        return fStoS;
    }
}
