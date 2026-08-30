package com.example.pi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Digital Banking Application entry point.
 *
 * @ConfigurationPropertiesScan discovers all @ConfigurationProperties beans
 * (JwtConfig, TransactionLimitConfig) without requiring @EnableConfigurationProperties
 * on each consumer (doc item #61).
 */
@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan("com.example.pi.config")
public class PiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PiApplication.class, args);
	}
}
