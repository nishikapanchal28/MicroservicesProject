package com.distributed.tracing.controller;

import com.distributed.tracing.service.ShortestPathService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortestPathController {
    private ShortestPathService shortestPathService;

    public ShortestPathController(ShortestPathService shortestPathService){
        this.shortestPathService = shortestPathService;
    }

    @GetMapping("/shortest-path")
    public ResponseEntity<String> getShortestPath(@RequestParam String from, @RequestParam String to) {
        try {
            int shortestPathLength = shortestPathService.findShortestTrace(from, to);
            return ResponseEntity.ok(String.valueOf(shortestPathLength));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("NO SUCH PATH");
        }
    }

}
