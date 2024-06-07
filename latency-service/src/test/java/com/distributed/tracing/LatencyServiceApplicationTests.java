package com.distributed.tracing;

import com.distributed.tracing.service.LatencyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class LatencyServiceApplicationTests {
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ObjectMapper objectMapper;
	@InjectMocks
	private LatencyService latencyService;
	String mockGraphData;


	@BeforeEach
	void setUp() throws JsonProcessingException {
		MockitoAnnotations.openMocks(this);
		mockGraphData = "{ \"adjacencyList\": { \"A\": { \"B\": 5, \"D\": 5, \"E\": 7 }, \"B\": { \"C\": 4 }, \"C\": { \"D\": 8, \"E\": 2 }, \"D\": { \"C\": 8, \"E\": 6 }, \"E\": { \"B\": 3 } } }";
		when(restTemplate.getForObject("http://localhost:8081/graph", String.class)).thenReturn(mockGraphData);
		when(objectMapper.readValue(eq(mockGraphData), any(TypeReference.class)))
				.thenReturn(Map.of("adjacencyList", Map.of(
						"A", Map.of("B", 5, "D", 5, "E", 7),
						"B", Map.of("C", 4),
						"C", Map.of("D", 8, "E", 2),
						"D", Map.of("C", 8, "E", 6),
						"E", Map.of("B", 3)
				)));

		latencyService = new LatencyService(restTemplate, objectMapper);
		latencyService.loadGraph();
	}

	@Test
	@DisplayName("Calculate Latency Valid Path")
	void testCalculateLatency_ValidPath() {
		try {
			int result = latencyService.calculateLatency("A", "B");
			assertEquals(5, result);
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	@DisplayName("Calculate Latency Invalid Path")
	void testCalculateLatency_InvalidPath() {
		try {
			assertThrows(IllegalArgumentException.class, () -> latencyService.calculateLatency("A", "F"));
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	@DisplayName("Find Average Latency")
	void testFindAverageLatency() {
		try {
			double result = latencyService.findAverageLatency("A-B-C");
			assertEquals(9, result);  // A-B = 5, B-C = 4, Total = 9
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	@DisplayName("Count Traces With Max Hops")
	void testCountTracesWithMaxHops() {
		try {
			int result = latencyService.countTracesWithMaxHops("C", "C", 3);
			assertEquals(2, result);  // C-D-C (2 hops), C-E-B-C (3 hops)
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}


	@Test
	@DisplayName("Count Traces With Exact Hops")
	void testCountTracesWithExactHops() {
		try {
			int result = latencyService.countTracesWithExactHops("A", "C", 4);
			assertEquals(3, result);  // A-B-C-D-C, A-D-C-D-C, A-E-B-C
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	@Test
	@DisplayName("Count Traces With Max Latency")
	void testCountTracesWithMaxLatency() {
		try {
			int result = latencyService.countTracesWithMaxLatency("C", "C", 30);
			assertEquals(7, result);  // Multiple traces with latency < 30
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

}
