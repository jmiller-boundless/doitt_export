package gov.nyc.doitt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BikelaneExportApplication {

	public static void main(String[] args) {
		System.setProperty("org.geotools.referencing.forceXY", "true");
		SpringApplication.run(BikelaneExportApplication.class, args);
	}
}
