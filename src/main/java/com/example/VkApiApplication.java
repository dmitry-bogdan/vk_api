package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableAutoConfiguration
@PropertySource(value = "vk.properties")
@PropertySource(value = "network.properties")
public class VkApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VkApiApplication.class, args);
	}
}
