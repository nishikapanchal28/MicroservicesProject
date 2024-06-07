package com.distributed.tracing.controller;

import com.distributed.tracing.model.Graph;
import com.distributed.tracing.service.GraphService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GraphController {

    public GraphController(GraphService graphService){
        this.graphService = graphService;
    }
    private GraphService graphService;

    @GetMapping("/graph")
    public ResponseEntity<Graph> getGraph() {
        try {
            Graph graph = graphService.readGraph();
            return ResponseEntity.ok(graph);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
