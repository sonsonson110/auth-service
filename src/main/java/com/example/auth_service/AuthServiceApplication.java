package com.example.auth_service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AuthServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	public ApplicationRunner printActiveProfiles(Environment environment) {
		return args -> {
			String[] profiles = environment.getActiveProfiles();
			System.out.println("🚀 Active profiles: " + String.join(", ", profiles));
		};
	}
}
