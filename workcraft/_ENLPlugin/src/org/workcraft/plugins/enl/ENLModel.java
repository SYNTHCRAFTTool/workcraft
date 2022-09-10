package org.workcraft.plugins.enl;

import java.util.Collection;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;


public interface ENLModel extends MathModel{

    
    Collection<? extends Transition> getTransitions();
    Collection<? extends Condition> getConditions();
    Collection<? extends MathConnection> getConnections();

    boolean isEnabled(Transition t);
    void fire(Transition t);

    boolean isUnfireEnabled(Transition t);
    void unFire(Transition t);
}
