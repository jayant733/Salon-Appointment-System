package de.salonplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SalonplanApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalonplanApplication.class, args);
	}

}
