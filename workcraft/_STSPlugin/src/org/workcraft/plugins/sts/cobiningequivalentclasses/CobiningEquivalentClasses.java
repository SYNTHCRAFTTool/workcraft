package org.workcraft.plugins.sts.cobiningequivalentclasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.State;
import org.workcraft.plugins.sts.utils.STSUtils;

import java.util.Set;
import java.util.TreeSet;

public class CobiningEquivalentClasses {

    // Function to check if it is safe to assign color c to vertex v
    private static boolean isSafe(Graph graph, int[] color, int v, int c) {
        // check color of every adjacent vertex of v
        for (int u : graph.adjList.get(v))
            if (color[u] == c)
                return false;

        return true;
    }

    public static void kColorable(Graph g, int[] color, int k, int v, int N, Set<Map<Integer, Integer>> listAllPossib) {

        // if all colors are assigned, print the solution
        if (v == N) {
            // list one possible equivalent classes
            Map<Integer, Integer> edgeWithColor = new HashMap<>();
            for (v = 0; v < N; v++) {
                edgeWithColor.put(v, color[v]);
            }
            // To collect all possibilities
            listAllPossib.add(edgeWithColor);
            return;
        }
        // try all possible combinations of available colors
        for (int c = 1; c <= k; c++) {
            // if it is safe to assign color c to vertex v
            if (isSafe(g, color, v, c)) {
                // assign color c to vertex v
                color[v] = c;
                // recur for next vertex
                kColorable(g, color, k, v + 1, N, listAllPossib);
                // backtrack
                color[v] = 0;
            }
        }
    }

    // A recursive utility function to solve m coloring problem
    public static boolean graphColoringUtil(Graph graph, int m, int color[], int v, int N) {
        // base case: If all vertices are assigned a color then return true
        if (v == N)
            return true;
        // Consider this vertex v and try different colors
        for (int c = 1; c <= m; c++) {
            // Check if assignment of color c to v is fine
            if (isSafe(graph, color, v, c)) {
                color[v] = c;
                // recur to assign colors to rest of the vertices
                if (graphColoringUtil(graph, m, color, v + 1, N))
                    return true;
                // If assigning color c doesn't lead to a solution then remove it
                color[v] = 0;
            }
        }
        /*
         * If no color can be assigned to this vertex then return false
         */
        return false;
    }

    /*
     * This function solves the m Coloring problem using Backtracking. It mainly
     * uses graphColoringUtil() to solve the problem. It returns false if the m
     * colors cannot be assigned, otherwise return true and prints assignments of
     * colors to all vertices. Please note that there may be more than one
     * solutions, this function prints one of the feasible solutions.
     */
    public static boolean graphColoring(Graph graph, int m, int N) {
        // Initialize all color values as 0. This
        // initialization is needed correct
        // functioning of isSafe()
        int[] color = new int[N];
        for (int i = 0; i < N; i++)
            color[i] = 0;
        // Call graphColoringUtil() for vertex 0
        if (!graphColoringUtil(graph, m, color, 0, N)) {
            return false;
        }
        // Print the solution
        return true;
    }

    static /* A utility function to print solution */
    void printSolution(int color[], int N) {
        System.out.println("Solution Exists: Following" + " are the assigned colors");
        for (int i = 0; i < N; i++)
            System.out.print(" " + color[i] + " ");
        System.out.println();
    }

    public static Set<Set<Set<String>>> combiningEquivelentClasses(Graph g, int N, int k,
            Map<Set<String>, Integer> setClassToInt, Map<Integer, Set<String>> setIntToClass) {
        Set<Map<Integer, Integer>> listAllPossib = new HashSet<>();
        int[] color = new int[N];
        // List of all equivalent classes
        Set<Set<Set<String>>> allPossibilities = new HashSet<>();
        // print all k-colorable configurations of the graph
        kColorable(g, color, k, 0, N, listAllPossib);
        // List of all possibilities
        for (Map<Integer, Integer> e : listAllPossib) {
            Set<Set<String>> equivalentClasses = new HashSet<>();
            // List of one possibility
            for (int j = 1; j <= k; j++) {
                Set<String> oneEquiClass = new HashSet<>();
                // Collect edges with one color to combine classes
                for (Entry<Integer, Integer> entry : e.entrySet()) {
                    // If edge has this color
                    if (j == entry.getValue()) {
                        // Get equivalent class with this color using its no
                        if (!setIntToClass.get(entry.getKey()).isEmpty())
                            oneEquiClass.addAll(setIntToClass.get(entry.getKey()));
                    }
                }
                if (!oneEquiClass.isEmpty()) {
                    equivalentClasses.add(oneEquiClass);
                }
            }
            if (!equivalentClasses.isEmpty()) {
                allPossibilities.add(equivalentClasses);
            }
        }
        return allPossibilities;
    }

    /**
     * Computing all possible co-location relations including \u224F ^{ts}_{min}.
     **/
    public static void computingAllColoRela(STS sts, Set<ArrayList<String>> tsmin,
            Map<State, Set<ArrayList<String>>> qAllStepsq, Map<State, Set<ArrayList<String>>> EqEq,
            Map<State, Set<String>> EStates, Set<Set<String>> localities) {
        if (checkComputingProcedure(sts, tsmin, qAllStepsq, EqEq)) {
            System.out.println("The result of computing all possible valid co-location relations including the "
                    + "\u224F" + "^{ts}_{min} is: ");
            Map<Set<String>, Set<Set<String>>> V = Gts.getVerix(EStates, localities);
            int maxColorNo = V.size();
            Map<Set<String>, Integer> setClassToInt = new HashMap<>();
            Map<Integer, Set<String>> setIntToClass = new HashMap<>();
            Set<Set<Set<String>>> allPossibilities = new HashSet<>();
            Gts.setEquivClassToNo(V, setClassToInt, setIntToClass);
            Graph g = Gts.buildGraph(V, setClassToInt, setIntToClass);
            allPossibilities = CobiningEquivalentClasses.combiningEquivelentClasses(g, V.size(), maxColorNo,
                    setClassToInt, setIntToClass);
            ArrayList<Set<Set<String>>> arrayList = new ArrayList<>();
            arrayList.addAll(allPossibilities);
            Collections.sort(arrayList, new Comparator<Set<?>>() {
                @Override
                public int compare(Set<?> o1, Set<?> o2) {
                    return Integer.valueOf(o2.size()).compareTo(o1.size());
                }
            });
            int o = 0;
            for (Set<Set<String>> list : arrayList) {
                int j = 0;
                System.out.print(++o);
                System.out.print(o < 10 ? " -{" : "-{");
                for (Set<String> locality : list) {
                    j++;
                    STSUtils.printSetOfString(locality);
                    System.out.print(j < list.size() ? "," : "");
                }
                System.out.println("}.");
            }
        }
    }

    /**
     * Computing valid co-location relations from \u224F ^{ts}_{min} with the
     * smallest no of equivalent classes.
     **/
    public static Set<Set<Set<String>>> computingMinColoRela2(STS sts, Set<ArrayList<String>> tsmin,
            Map<State, Set<ArrayList<String>>> qAllStepsq, Map<State, Set<ArrayList<String>>> EqEq,
            Map<State, Set<String>> EStates, Set<Set<String>> localities) {
        Set<Set<Set<String>>> minColoNOPossi = new HashSet<>();
        if (checkComputingProcedure(sts, tsmin, qAllStepsq, EqEq)) {
            Map<Set<String>, Set<Set<String>>> V = Gts.getVerix(EStates, localities);
            int colorNo = V.size();
            int N = V.size();
            int minNo = colorNo;
            Map<Set<String>, Integer> setClassToInt = new HashMap<>();
            Map<Integer, Set<String>> setIntToClass = new HashMap<>();
            Gts.setEquivClassToNo(V, setClassToInt, setIntToClass);
            Graph g = Gts.buildGraph(V, setClassToInt, setIntToClass);
            int lower = 0;
            int mid = 0;
            int higher = colorNo - 1;
            while (lower <= higher) {
                mid = (lower + higher) / 2;
                if (graphColoring(g, mid, N)) {
                    minNo = mid;
                    higher = mid - 1;
                } else {
                    lower = mid + 1;
                }
            }
            minColoNOPossi = CobiningEquivalentClasses.combiningEquivelentClasses(g, V.size(), minNo, setClassToInt,
                    setIntToClass);
            int o = 0;
            if (minColoNOPossi.contains(localities)) {
                for (Set<Set<String>> list : minColoNOPossi) {
                    int j = 0;
                    System.out.print("{");
                    for (Set<String> locality : list) {
                        j++;
                        STSUtils.printSetOfString(locality);
                        System.out.print(j < list.size() ? "," : "");
                    }
                    System.out.print("}, ");
                }
                System.out.println("which is the same as " + "\u224F" + "^{ts}_{min}.");
                System.out.println();
            } else {
                for (Set<Set<String>> list : minColoNOPossi) {
                    int j = 0;
                    System.out.print(++o);
                    System.out.print(o < 10 ? " -{" : "-{");
                    for (Set<String> locality : list) {
                        j++;
                        STSUtils.printSetOfString(locality);
                        System.out.print(j < list.size() ? "," : "");
                    }
                    System.out.println("}.");
                }
            }
        }
        return minColoNOPossi;
    }

    /** Computing valid co-location relations from \u224F ^{ts}_{min} with the
        smallest no of equivalent classes.**/
    public static Set<Set<Set<String>>> computingMinBalancedColoRela(Set<Set<Set<String>>> minColoNOPossi,
            Set<Set<String>> localities) {
        Set<Set<Set<String>>> minBalancedColoNOPossi = new HashSet<>();
        System.out.println("\n Valid co-location relations computed from " + "\u224F"
                + "^{ts}_{min} with the smallest number of equivalence classes and the most balanced distribution of events in their equivalence classes (given as a set of equivalence classes unless it is "
                + "\u224F" + "^{ts}_{min}):");
        Map<Set<Set<String>>, Integer> setWithsubMaxMin = new HashMap<>();
        for (Set<Set<String>> sets : minColoNOPossi) {
            int maxSize = Integer.MIN_VALUE;
            int minSize = Integer.MAX_VALUE;
            int subMaxMin = 0;
            for (Set<String> set : sets) {
                maxSize = Math.max(maxSize, set.size());
                minSize = Math.min(minSize, set.size());
            }
            subMaxMin = maxSize - minSize;
            setWithsubMaxMin.put(sets, subMaxMin);
        }
        int minNum = Integer.MAX_VALUE;
        for (Map.Entry<Set<Set<String>>, Integer> entry : setWithsubMaxMin.entrySet()) {
            minNum = Math.min(minNum, entry.getValue());
        }
        for (Map.Entry<Set<Set<String>>, Integer> s : setWithsubMaxMin.entrySet()) {
            if (s.getValue() == minNum) {
                minBalancedColoNOPossi.add(s.getKey());
            }
        }
        int o = 0;
        if (minBalancedColoNOPossi.size() == localities.size() & minBalancedColoNOPossi.contains(localities)) {
            System.out.println("1- " + "\u224F" + "^{ts}_{min},");
        } else {
            for (Set<Set<String>> list : minBalancedColoNOPossi) {
                int j = 0;
                System.out.print(++o);
                System.out.print(o < 10 ? " -{" : "-{");
                for (Set<String> locality : list) {
                    j++;
                    STSUtils.printSetOfString(locality);
                    System.out.print(j < list.size() ? "," : "");
                }
                System.out.println("},");
            }
        }
        System.out.println("with the smallest balance indicator = " + minNum + ".");
        return minBalancedColoNOPossi;
    }

    // Check Procedure on page 13.
    public static boolean checkComputingProcedure(STS sts, Set<ArrayList<String>> tsmin,
            Map<State, Set<ArrayList<String>>> qAllStepsq, Map<State, Set<ArrayList<String>>> EqEq) {
        boolean result = false;
        int count = 0;
        // Each state q with its U*U where U belongs to allStepsq.
        for (Entry<State, Set<ArrayList<String>>> entry : qAllStepsq.entrySet()) {
            // Get (Eq*Eq) /\ tsmin to eliminate them.
            Set<ArrayList<String>> tsminEqEq = tsmin.stream().filter(EqEq.get(entry.getKey())::contains)
                    .collect(Collectors.toSet());
            Set<ArrayList<String>> inEqEqNottsmin = new HashSet<>();
            // Get sets in Eq*Eq only (Eq*Eq) \ tsmin.
            for (ArrayList<String> set : EqEq.get(entry.getKey())) {
                if (!tsminEqEq.contains(set)) {
                    inEqEqNottsmin.add(set);
                }
            }
            // Check if (Eq*Eq) \ tsmin part of U*U where U belongs to allStepsq.
            if (entry.getValue().containsAll(inEqEqNottsmin)) {
                count++;
            } else {
                break;
            }
        }
        if (count == qAllStepsq.size()) {
            result = true;
        }
        return result;
    }

    // Function to assign colors to vertices of graph
    public static int colorGraph(Graph graph, int N, Set<Map<Integer, Integer>> listAllPossib) {
        int colore = 1;

        // stores color assigned to each vertex
        Map<Integer, Integer> result = new HashMap<>();

        // assign color to vertex one by one
        for (int u = 0; u < N; u++) {
            // set to store color of adjacent vertices of u
            Set<Integer> assigned = new TreeSet<>();
            // check colors of adjacent vertices of u and store in set
            for (int i : graph.adjList.get(u)) {
                if (result.containsKey(i)) {
                    assigned.add(result.get(i));
                }
            }
            // check for first free color
            for (Integer c : assigned) {
                if (colore != c) {
                    break;
                }
                colore++;
            }
            // assigns vertex u the first available color
            result.put(u, colore);
        }
        listAllPossib.add(result);
        return colore;
    }

    // Function to assign colors to vertices of graph
    public static void colorGraph2(Graph graph, int N, Set<Map<Integer, Integer>> listAllPossib) {
        int colore = 3;

        // stores color assigned to each vertex
        Map<Integer, Integer> result = new HashMap<>();

        // assign color to vertex one by one
        for (int u = 0; u < N; u++) {
            // set to store color of adjacent vertices of u
            Set<Integer> assigned = new TreeSet<>();
            // check colors of adjacent vertices of u and store in set
            for (int i : graph.adjList.get(u)) {
                if (result.containsKey(i)) {
                    assigned.add(result.get(i));
                }
            }
            // check for first free color
            for (Integer c : assigned) {
                if (colore != c) {
                    break;
                }
                colore++;
            }
            // assigns vertex u the first available color
            result.put(u, colore);
        }
        listAllPossib.add(result);
    }
}
