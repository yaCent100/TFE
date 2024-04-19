package be.iccbxl.tfe.Driveshare;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DriveshareApplication {


	public static void main(String[] args) {
		SpringApplication.run(DriveshareApplication.class, args);
	}

}
