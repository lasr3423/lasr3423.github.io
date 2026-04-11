package com.bookstore.shop.readme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling  // PaymentCleanupScheduler @Scheduled 활성화
@SpringBootApplication
public class ReadmeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReadmeApplication.class, args);
	}

}
