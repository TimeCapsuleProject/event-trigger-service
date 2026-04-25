package br.com.timecapsuleproject.timeCapsuleTriggerService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeCapsuleTriggerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeCapsuleTriggerServiceApplication.class, args);
	}

}
