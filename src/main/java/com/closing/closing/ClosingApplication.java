package com.closing.closing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ClosingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClosingApplication.class, args);
	}

}
