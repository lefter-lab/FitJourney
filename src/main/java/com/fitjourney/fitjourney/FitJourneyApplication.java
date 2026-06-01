package com.fitjourney.fitjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableMethodSecurity
public class FitJourneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitJourneyApplication.class, args);
	}

}
