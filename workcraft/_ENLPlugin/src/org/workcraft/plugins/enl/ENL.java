package org.workcraft.plugins.enl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.serialisation.References;
import org.workcraft.types.MultiSet;
import org.workcraft.utils.Hierarchy;

public class ENL extends AbstractMathModel implements ENLModel {
    static Set<Transition> events = new HashSet<>();

    public ENL() {
        this(null, null);
    }

    public ENL(Container root, References refs) {
        super(root, refs);
    }

    public final Condition createCondition(String name, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Condition condition = new Condition();
        container.add(condition);
        if (name != null) {
            setName(condition, name);
        }
        return condition;
    }

    public final Transition createTransition(String name, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Transition transition = new Transition();
        container.add(transition);
        if (name != null) {
            setName(transition, name);
        }
        return transition;
    }

    public static final boolean isUnfireEnabled(ENL net, Transition t) {
        // gather number of connections for each post-Condition
        Map<Condition, Integer> map = new HashMap<>();
        for (MathConnection c : net.getConnections(t)) {
            if (c.getFirst() == t) {
                if (map.containsKey(c.getSecond())) {
                    map.put((Condition) c.getSecond(), map.get(c.getSecond()) + 1);
                } else {
                    map.put((Condition) c.getSecond(), 1);
                }
            }
        }
        // If any post-condition has a token return false, because it can not fire.
        for (Node n : net.getPostset(t)) {
            if (((Condition) n).getTokens() == 1) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isEnabled(ENL net, Transition t) {
        // gather number of connections for each pre-Condition
        Map<Condition, Integer> map = new HashMap<>();
        for (MathConnection c : net.getConnections(t)) {
            if (c.getSecond() == t) {
                Condition p = (Condition) c.getFirst();
                if (map.containsKey(p)) {
                    map.put(p, map.get(p) + 1); //
                    System.out.println(map.get(p));
                } else {
                    map.put(p, 1);
                }
            }
        }
        for (Node n : net.getPreset(t)) {
            Condition p = (Condition) n;
            if (p.getTokens() == 0) {
                return false;
            }
        }
        for (Node n1 : net.getPostset(t)) {
            Condition p1 = (Condition) n1;

            if (p1.getTokens() == 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final void fire(Transition t) {
        fire(this, t);
    }

    @Override
    public final void unFire(Transition t) {
        unFire(this, t);
    }

    public static final void fire(ENL net, Transition t) {
        if (net.isEnabled(t)) {
            // first consume a token and then produce a token
            for (MathConnection c : net.getConnections(t)) {
                if (t == c.getSecond()) {
                    Condition from = (Condition) c.getFirst();
                    from.setTokens(0);
                }
            }
            for (MathConnection c : net.getConnections(t)) {
                if (t == c.getFirst()) {
                    Condition to = (Condition) c.getSecond();
                    to.setTokens(1);
                }
            }
        }
    }

    public static final void unFire(ENL net, Transition t) {
        // the opposite action to fire, no additional checks,
        // (the transition must be "unfireble")
        // first consumes a token and then produces a token
        for (MathConnection c : net.getConnections(t)) {
            if (t == c.getFirst()) {
                Condition to = (Condition) c.getSecond();
                to.setTokens(0);
            }
        }
        for (MathConnection c : net.getConnections(t)) {
            if (t == c.getSecond()) {
                Condition from = (Condition) c.getFirst();
                from.setTokens(1);
            }
        }
    }

    @Override
    public final Collection<Condition> getConditions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Condition.class);
    }

    @Override
    public final Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    public final Collection<MathConnection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), MathConnection.class);
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return isUnfireEnabled(this, t);
    }

    @Override
    public final boolean isEnabled(Transition t) {
        return isEnabled(this, t);
    }

    @Override
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Conditions: ", getConditions().size());
        result.add("Events: ", getTransitions().size());
        result.add("Arcs: ", getConnections().size());
        return result;
    }
}
