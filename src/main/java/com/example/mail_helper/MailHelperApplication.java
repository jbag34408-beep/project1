package com.example.mail_helper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MailHelperApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailHelperApplication.class, args);
	}

}
