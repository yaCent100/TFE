package be.iccbxl.tfe.Driveshare;


import be.iccbxl.tfe.Driveshare.model.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling // Pour activer les tâches planifiées
public class DriveshareApplication {

	public static void main(String[] args) {
		SpringApplication.run(DriveshareApplication.class, args);

	}
	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedDoubleSlash(true); // Permettre les doubles barres obliques encodées
		return firewall;
	}
}



