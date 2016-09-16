package com.vs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vs"})
public class GeospatialsearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeospatialsearchApplication.class, args);
	}
}
