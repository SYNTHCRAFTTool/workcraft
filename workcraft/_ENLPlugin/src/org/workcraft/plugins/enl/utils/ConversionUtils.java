package org.workcraft.plugins.enl.utils;

import java.util.HashSet;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.enl.VisualCondition;
import org.workcraft.plugins.enl.VisualTransition;
import org.workcraft.utils.Hierarchy;

public class ConversionUtils {

   
    // check if there is arc already leaving the event (producing arc).
    public static boolean hasProducingArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualCondition codition = null;
        VisualTransition transition = null;
        if (first instanceof VisualTransition) {
            transition = (VisualTransition) first;
        }
        if (second instanceof VisualCondition) {
            codition = (VisualCondition) second;
        }
        if ((transition != null) && (codition != null)) {

            if (!found) {
                VisualConnection connection = visualModel.getConnection(transition, codition);
                found = (connection instanceof VisualConnection);
            }
        }
        return found;
    }

    // check if there is arc already entering the transition (consuming arc).
    public static boolean hasConsumingArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualCondition codition = null;
        VisualTransition transition = null;
        if (first instanceof VisualCondition) {
            codition = (VisualCondition) first;
        }
        if (second instanceof VisualTransition) {
            transition = (VisualTransition) second;
        }
        if ((codition != null) && (transition != null)) {           
            if (!found) {
                VisualConnection connection = visualModel.getConnection(codition, transition);
                found = (connection instanceof VisualConnection);
            }
        }
        return found;
    }
    
    // check if there is connection already between a transition and a condition.
    public static boolean hasConsumingORProducingArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {       
        boolean hasConsumingArc = hasConsumingArcConnection(visualModel,first,second);
        boolean hasProducingArc = hasProducingArcConnection(visualModel,first,second);
          if ((hasConsumingArc) || (hasProducingArc)) return true;
          return false;
    }
           
    // Return a hash set of all consuming arcs.
    public static HashSet<VisualConnection> getVisualConsumingArcs(VisualModel visualModel) {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
            if (isVisualConsumingArc(connection)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    // Return a hash set of all producing arcs.
    public static HashSet<VisualConnection> getVisualProducingArcs(VisualModel visualModel) {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
            if (isVisualProducingArc(connection)) {
                connections.add(connection);
            }
        }
        return connections;
    }

   
    // Find all conditions and get them in Hash set.
    public static HashSet<VisualCondition> getVisualConditions(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualCondition.class));
    }

    // Find all transitions and get them in Hash set.
    public static HashSet<VisualTransition> getVisualTransitions(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualTransition.class));
    }
    
    // check if the node is a transition.
    public static boolean isVisualTransition(VisualNode node) {
        return node instanceof VisualTransition;
    }

    // check if the node is a condition.
    public static boolean isVisualCondition(VisualNode node) {
        return (node instanceof VisualCondition);
    }

    // check if the arc connects a transition to a condition, which means the arc is a producing arc (leaving transition) #-->o.
    public static boolean isVisualProducingArc(VisualNode node) {
        if (node instanceof VisualConnection){
            VisualConnection connection = (VisualConnection) node;
            return isVisualTransition(connection.getFirst()) && isVisualCondition(connection.getSecond());
        }
        return false;
    }

    // check if the arc connects a condition to a transition, which means the arc is a consuming arc (entering transition) o-->#.
    public static boolean isVisualConsumingArc(VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            return isVisualCondition(connection.getFirst()) && isVisualTransition(connection.getSecond());
        }
        return false;
    }
}
