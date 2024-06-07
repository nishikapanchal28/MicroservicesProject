package com.distributed.tracing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LatencyService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, Integer>> adjacencyMap;

    public LatencyService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.adjacencyMap = loadGraph();

    }

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

    public Map<String, Map<String, Integer>> loadGraph() {
        String graphData = restTemplate.getForObject("http://localhost:8081/graph", String.class);
        System.out.println("graph loaded");
        return parseGraphData(graphData);
    }

    public int calculateLatency(String from, String to) {
        Map<String, Integer> neighbors = adjacencyMap.getOrDefault(from, new HashMap<>());
        Integer latency = neighbors.get(to);
        if (latency == null) {
            throw new IllegalArgumentException("No such path exists");
        }
        return latency;
    }

    public double findAverageLatency(String trace) {
        String[] segments = trace.split("-");
        int totalLatency = 0;

        for (int i = 0; i < segments.length - 1; i++) {
            String from = segments[i];
            String to = segments[i + 1];
            int latency = calculateLatency(from, to);
            totalLatency += latency;
        }

        return totalLatency;
    }

    public int countTracesWithMaxHops(String start, String end, int maxHops) {
        return dfsCountHops(start, end, 0, maxHops);
    }

    public int countTracesWithExactHops(String start, String end, int exactHops) {
        return dfsCountHops(start, end, 0, exactHops, true);
    }

    private int dfsCountHops(String current, String end, int currentHops, int maxHops) {
        if (currentHops > maxHops) {
            return 0;
        }
        int count = 0;
        if (current.equals(end) && currentHops != 0) {
            count++;
        }
        Map<String, Integer> neighbors = adjacencyMap.get(current);
        if (neighbors != null) {
            for (String neighbor : neighbors.keySet()) {
                count += dfsCountHops(neighbor, end, currentHops + 1, maxHops);
            }
        }
        return count;
    }

    private int dfsCountHops(String current, String end, int currentHops, int exactHops, boolean exact) {
        if (currentHops > exactHops) {
            return 0;
        }
        int count = 0;
        if (current.equals(end) && currentHops == exactHops) {
            count++;
        }
        Map<String, Integer> neighbors = adjacencyMap.get(current);
        if (neighbors != null) {
            for (String neighbor : neighbors.keySet()) {
                count += dfsCountHops(neighbor, end, currentHops + 1, exactHops, exact);
            }
        }
        return count;
    }

    public int countTracesWithMaxLatency(String start, String end, int maxLatency) {
        return dfsCountLatency(start, end, 0, maxLatency);
    }

    private int dfsCountLatency(String current, String end, int currentLatency, int maxLatency) {
        if (currentLatency >= maxLatency) {
            return 0;
        }
        int count = 0;
        if (current.equals(end) && currentLatency != 0) {
            count++;
        }
        Map<String, Integer> neighbors = adjacencyMap.get(current);
        if (neighbors != null) {
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                count += dfsCountLatency(neighbor.getKey(), end, currentLatency + neighbor.getValue(), maxLatency);
            }
        }
        return count;
    }
}

