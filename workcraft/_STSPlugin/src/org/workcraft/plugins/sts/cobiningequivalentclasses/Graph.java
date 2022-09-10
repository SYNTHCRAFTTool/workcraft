package org.workcraft.plugins.sts.cobiningequivalentclasses;

import java.util.ArrayList;
import java.util.List;

//class to represent a graph object

public class Graph {
    // A List of Lists to represent an adjacency list
    List<List<Integer>> adjList = null;
 
    // Constructor
    Graph(List<Edge> edges, int N)
    {
        adjList = new ArrayList<>();
 
        for (int i = 0; i < N; i++) {
            adjList.add(new ArrayList<>());
        }
 
        // add edges to the undirected graph
        for (Edge edge: edges)
        {
            int src = edge.source;
            int dest = edge.dest;
            adjList.get(src).add(dest);
            adjList.get(dest).add(src);
        }
    }
}
