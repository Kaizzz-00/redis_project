package com.example.redisPractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "Controller")
public class RedisPracticeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisPracticeApplication.class, args);
	}

}
