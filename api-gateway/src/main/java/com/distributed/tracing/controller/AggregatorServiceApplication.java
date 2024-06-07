package com.distributed.tracing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/aggregate")
public class AggregatorServiceApplication {

    private final RestTemplate restTemplate;

    public AggregatorServiceApplication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/results")
    public String getResults() {
        StringBuilder output = new StringBuilder();

        // 1. Total latency of A-B-C
        String latencyABCString = restTemplate.getForObject("http://localhost:8082/latency/average-latency?trace=A-B-C", String.class);
        double latencyABC = Double.parseDouble(latencyABCString);
        output.append("1. ").append((int)latencyABC).append("\n");

        // 2. Total latency of A-D
        String latencyADString = restTemplate.getForObject("http://localhost:8082/latency/average-latency?trace=A-D", String.class);
        double latencyAD = Double.parseDouble(latencyADString);
        output.append("2. ").append((int)latencyAD).append("\n");

        // 3. Total latency of A-D-C
        String latencyADCString = restTemplate.getForObject("http://localhost:8082/latency/average-latency?trace=A-D-C", String.class);
        double latencyADC = Double.parseDouble(latencyADCString);
        output.append("3. ").append((int)latencyADC).append("\n");

        // 4. Total latency of A-E-B-C-D
        String latencyAEBCDString = restTemplate.getForObject("http://localhost:8082/latency/average-latency?trace=A-E-B-C-D", String.class);
        double latencyAEBCD = Double.parseDouble(latencyAEBCDString);
        output.append("4. ").append((int)latencyAEBCD).append("\n");

        try {
            String latencyAED = restTemplate.getForObject("http://localhost:8082/latency/average-latency?trace=A-E-D", String.class);
            output.append("5. ").append("NO SUCH TRACE".equals(latencyAED) ? "NO SUCH TRACE" : latencyAED).append("\n");
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST && ex.getResponseBodyAsString().equals("NO SUCH TRACE")) {
                output.append("5. NO SUCH TRACE\n");
            }
        }
        // 6. Number of trips from C to C with a maximum of 3 stops
        String tripsMax3Stops = restTemplate.getForObject("http://localhost:8082/latency/count-traces-max-hops?start=C&end=C&maxHops=3", String.class);
        output.append("6. ").append(tripsMax3Stops).append("\n");

        // 7. Number of trips from A to C with exactly 4 stops
        String tripsExact4Stops = restTemplate.getForObject("http://localhost:8082/latency/count-traces-exact-hops?start=A&end=C&exactHops=4", String.class);
        output.append("7. ").append(tripsExact4Stops).append("\n");

        // 8. Length of the shortest route (in terms of latency) from A to C
        String shortestPathAC = restTemplate.getForObject("http://localhost:8084/shortest-path?from=A&to=C", String.class);
        output.append("8. ").append(shortestPathAC).append("\n");

        // 9. Length of the shortest route (in terms of latency) from B to B
        String shortestPathBB = restTemplate.getForObject("http://localhost:8084/shortest-path?from=B&to=B", String.class);
        output.append("9. ").append(shortestPathBB).append("\n");

        // 10. Number of different routes from C to C with a latency of less than 30
        String pathsMaxLatency = restTemplate.getForObject("http://localhost:8082/latency/count-traces-max-latency?start=C&end=C&maxLatency=30", String.class);
        output.append("10. ").append(pathsMaxLatency).append("\n");

        return output.toString();
    }
}
