package com.distributed.tracing.controller;

import com.distributed.tracing.service.LatencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/latency")
public class LatencyController {

    public LatencyController(LatencyService latencyService){
        this.latencyService = latencyService;
    }
    private LatencyService latencyService;

    @GetMapping("/average-latency")
    public ResponseEntity<String> getAverageLatency(@RequestParam String trace) {
        try {
            double averageLatency = latencyService.findAverageLatency(trace);
            return ResponseEntity.ok(String.valueOf(averageLatency));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("NO SUCH TRACE");
        }
    }

    @GetMapping("/count-traces-max-hops")
    public ResponseEntity<String> countTracesWithMaxHops(@RequestParam String start, @RequestParam String end, @RequestParam int maxHops) {
        try {
            int count = latencyService.countTracesWithMaxHops(start, end, maxHops);
            return ResponseEntity.ok(String.valueOf(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("NO SUCH TRACE");
        }
    }

    @GetMapping("/count-traces-exact-hops")
    public ResponseEntity<String> countTracesWithExactHops(@RequestParam String start, @RequestParam String end, @RequestParam int exactHops) {
        try {
            int count = latencyService.countTracesWithExactHops(start, end, exactHops);
            return ResponseEntity.ok(String.valueOf(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("NO SUCH TRACE");
        }
    }

    @GetMapping("/count-traces-max-latency")
    public ResponseEntity<String> countTracesWithMaxLatency(@RequestParam String start, @RequestParam String end, @RequestParam int maxLatency) {
        try {
            int count = latencyService.countTracesWithMaxLatency(start, end, maxLatency);
            return ResponseEntity.ok(String.valueOf(count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("NO SUCH TRACE");
        }
    }
}
