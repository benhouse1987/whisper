package com.fane.whisper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WhisperApplication {

	public static void main(String[] args)
	{
		ConfigurableApplicationContext configurableApplicationContext= SpringApplication.run(WhisperApplication.class, args);
	}

}
