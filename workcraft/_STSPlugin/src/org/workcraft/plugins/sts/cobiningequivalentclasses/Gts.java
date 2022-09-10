package org.workcraft.plugins.sts.cobiningequivalentclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.workcraft.plugins.sts.State;

public class Gts {

    // Return map of each set in ts_min and its edges
    public static Map<Set<String>, Set<Set<String>>> getVerix(Map<State, Set<String>> EStates, Set<Set<String>> tsmin) {
        Map<Set<String>, Set<Set<String>>> V = new HashMap<>();
        for (Set<String> equivClass : tsmin) {
            Set<Set<String>> hasEdgeWith = new HashSet<>();
            V.put(equivClass, hasEdgeWith);
        }
        Set<Set<String>> test = new HashSet<>();
        for (Set<String> equivClass1 : tsmin) {
            test.add(equivClass1);
            for (Set<String> equivClass2 : tsmin) {

                if (!equivClass1.equals(equivClass2)) {
                    if (!test.contains(equivClass2)) {
                        int count = 0;
                        for (Entry<State, Set<String>> Eq : EStates.entrySet()) {
                            boolean checkClass1 = false;
                            boolean checkClass2 = false;
                            // check if equivClass1 and equivClass2 don't chair any events
                            for (String e : Eq.getValue()) {
                                if (equivClass1.contains(e))
                                    checkClass1 = true;
                                if (equivClass2.contains(e))
                                    checkClass2 = true;
                            }
                            // If yes, don't chair
                            if (checkClass1 & checkClass2) {
                                count++;
                                break;
                            }
                        }
                        if (count != 0) {
                            V.get(equivClass1).add(equivClass2);
                            V.get(equivClass2).add(equivClass1);
                        }
                    }
                }
            }
        }
        return V;
    }
    
    // Assigns each equivalent class to number.
    public static void setEquivClassToNo(Map<Set<String>, Set<Set<String>>> V, Map<Set<String>, Integer> setClassToInt,
            Map<Integer, Set<String>> setIntToClass) {
        int i = 0;
        for (Map.Entry<Set<String>, Set<Set<String>>> entry : V.entrySet()) {
            // Set equivalent class as a map key.
            setClassToInt.put(entry.getKey(), i);
            // Set number as a map key.
            setIntToClass.put(i, entry.getKey());
            i++;
        }
    }

    // Build G^ts = (V,E) whish is (vertices,edges).
    public static Graph buildGraph(Map<Set<String>, Set<Set<String>>> V, Map<Set<String>, Integer> setClassToInt,
            Map<Integer, Set<String>> setIntToClass) {
        // List of graph edges as per above diagram
        List<Edge> edges = new ArrayList<>();
        for (Map.Entry<Set<String>, Set<Set<String>>> entry : V.entrySet()) {
            for (Set<String> equiClass : entry.getValue())
                // Assigns each equivalent class with its edges as numbers.
                edges.add(new Edge(setClassToInt.get(entry.getKey()), setClassToInt.get(equiClass)));
        }
        // Set number of vertices in the graph
        final int N = V.size();
        // create a graph from edges
        Graph g = new Graph(edges, N);
        return g;
    }
}
