package com.distributed.tracing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
@Service
public class ShortestPathService {
    public ShortestPathService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private Map<String, Map<String, Integer>> parseGraphData(String graphData) {
        try {
            Map<String, Map<String, Map<String, Integer>>> data = objectMapper.readValue(graphData, new TypeReference<Map<String, Map<String, Map<String, Integer>>>>() {});
            return data.containsKey("adjacencyList") ? data.get("adjacencyList") : new HashMap<>();
        } catch (Exception e) {
            System.err.println("Error parsing graph data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public int findShortestTrace(String start, String end) {
        var distances = new HashMap<String, Integer>();
        var pq = new PriorityQueue<String>(Comparator.comparingInt(distances::get));

        String graphData = restTemplate.getForObject("http://localhost:8081/graph", String.class);
        var adjacencyMap = parseGraphData(graphData);

        if (adjacencyMap == null) {
            throw new IllegalArgumentException("Graph data is unavailable");
        }
        if (!adjacencyMap.containsKey(start) || !adjacencyMap.containsKey(end)) {
            throw new IllegalArgumentException("NO SUCH PATH");
        }

        adjacencyMap.keySet().forEach(node -> distances.put(node, Integer.MAX_VALUE));
        distances.put(start, 0);
        pq.add(start);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            int currentDistance = distances.get(current);

            adjacencyMap.getOrDefault(current, Map.of()).forEach((neighbor, value) -> {
                int newDist = currentDistance + value;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    pq.add(neighbor);
                }
            });
        }

        int result = distances.getOrDefault(end, Integer.MAX_VALUE);
        if (start.equals(end)) {
            System.out.println("Case cycle");
            result = findShortestCycle(adjacencyMap, start);
        }
        if (result == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("NO SUCH PATH");
        }
        return result;
    }

    private int findShortestCycle(Map<String, Map<String, Integer>> adjacencyMap, String start) {
        int shortestCycleLength = Integer.MAX_VALUE;

        for (var neighborEntry : adjacencyMap.getOrDefault(start, Map.of()).entrySet()) {
            int currentDistance = neighborEntry.getValue();
            String nextNode = neighborEntry.getKey();

            Map<String, Integer> distances = new HashMap<>();
            PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));

            adjacencyMap.keySet().forEach(node -> distances.put(node, Integer.MAX_VALUE));
            distances.put(nextNode, currentDistance);
            pq.add(nextNode);

            while (!pq.isEmpty()) {
                String current = pq.poll();
                int distance = distances.get(current);

                for (var nextNeighborEntry : adjacencyMap.getOrDefault(current, Map.of()).entrySet()) {
                    int newDist = distance + nextNeighborEntry.getValue();
                    if (newDist < distances.get(nextNeighborEntry.getKey())) {
                        distances.put(nextNeighborEntry.getKey(), newDist);
                        pq.add(nextNeighborEntry.getKey());
                    }
                }
            }

            shortestCycleLength = Math.min(shortestCycleLength, distances.getOrDefault(start, Integer.MAX_VALUE));
        }

        return shortestCycleLength;
    }

}
