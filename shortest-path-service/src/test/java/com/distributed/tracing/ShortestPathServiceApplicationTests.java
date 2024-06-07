package com.distributed.tracing;

import com.distributed.tracing.service.ShortestPathService;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ShortestPathServiceApplicationTests {
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ObjectMapper objectMapper;
	@InjectMocks
	private ShortestPathService shortestPathService;
	private String graphData;
	String mockGraphData;

	@BeforeEach
	void setUp() throws JsonProcessingException {
		shortestPathService = new ShortestPathService(restTemplate, objectMapper);
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
		graphData = mockGraphData;
	}

	@Test
	@DisplayName("Find Shortest Trace Valid Trace")
	void testFindShortestTrace_ValidTrace() throws JsonProcessingException {
		int result = shortestPathService.findShortestTrace("A", "C");
		assertEquals(9, result);
		verify(objectMapper).readValue(eq(mockGraphData), any(TypeReference.class));
	}


	@Test
	@DisplayName("Find Shortest Trace Invalid Trace")
	void testFindShortestTrace_InvalidTrace() {
		try {
			assertThrows(IllegalArgumentException.class, () -> shortestPathService.findShortestTrace("A", "F"));
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}
	@Test
	@DisplayName("Find Shortest Trace Cycle")
	void testFindShortestTrace_Cycle() {
		try {
			int result = shortestPathService.findShortestTrace("C", "C");
			assertTrue(result < Integer.MAX_VALUE);
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}


}
