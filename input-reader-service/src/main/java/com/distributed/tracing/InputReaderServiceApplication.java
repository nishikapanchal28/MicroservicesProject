package com.distributed.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class InputReaderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InputReaderServiceApplication.class, args);
	}

}
