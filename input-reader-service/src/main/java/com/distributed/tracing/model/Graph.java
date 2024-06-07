package com.distributed.tracing.model;

import java.util.HashMap;
import java.util.Map;

public class Graph {
    private Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();

    public void addEdge(String from, String to, int weight) {
        adjacencyList.computeIfAbsent(from, k -> new HashMap<>()).put(to, weight);
    }

    public Map<String, Map<String, Integer>> getAdjacencyList() {
        return adjacencyList;
    }
}
