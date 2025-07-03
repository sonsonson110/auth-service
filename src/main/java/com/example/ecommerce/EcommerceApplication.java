package com.example.ecommerce;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EcommerceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Bean
	public ApplicationRunner printActiveProfiles(Environment environment) {
		return args -> {
			String[] profiles = environment.getActiveProfiles();
			System.out.println("🚀 Active profiles: " + String.join(", ", profiles));
		};
	}
}
