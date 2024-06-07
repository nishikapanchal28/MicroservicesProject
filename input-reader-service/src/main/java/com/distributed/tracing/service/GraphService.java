package com.distributed.tracing.service;

import com.distributed.tracing.model.Graph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class GraphService {
    public GraphService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    private Graph graph;

    @Value("${graph.file.path}")
    private String graphFilePath;
    private final ResourceLoader resourceLoader;

  public Graph readGraph() throws IOException {
      InputStream inputStream = getClass().getClassLoader().getResourceAsStream(graphFilePath);
      if (inputStream == null) {
          throw new IOException("File not found: " + graphFilePath);
      }
      Graph graph = new Graph();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
          String line;
          while ((line = br.readLine()) != null) {
              String[] parts = line.split(",");
              for (String part : parts) {
                  String from = part.substring(0, 1);
                  String to = part.substring(1, 2);
                  int weight = Integer.parseInt(part.substring(2));
                  graph.addEdge(from, to, weight);
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
      return graph;
  }

public int findShortestPath(String from, String to) {
    Map<String, Integer> distances = new HashMap<>();
    PriorityQueue<String> pq = new PriorityQueue<>((a, b) -> distances.get(a) - distances.get(b));

    for (String node : graph.getAdjacencyList().keySet()) {
        if (node.equals(from)) {
            distances.put(node, 0);
        } else {
            distances.put(node, Integer.MAX_VALUE);
        }
    }

    pq.add(from);

    while (!pq.isEmpty()) {
        String current = pq.poll();

        for (Map.Entry<String, Integer> neighbor : graph.getAdjacencyList().get(current).entrySet()) {
            int newDist = distances.get(current) + neighbor.getValue();

            if (newDist < distances.get(neighbor.getKey())) {
                distances.put(neighbor.getKey(), newDist);
                pq.add(neighbor.getKey());
            }
        }
    }

    return distances.get(to);
}

public Graph getGraph() {
    return graph;
}
}
