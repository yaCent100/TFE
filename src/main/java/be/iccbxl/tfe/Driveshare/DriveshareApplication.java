package be.iccbxl.tfe.Driveshare;


import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class DriveshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriveshareApplication.class, args);


	}


}

