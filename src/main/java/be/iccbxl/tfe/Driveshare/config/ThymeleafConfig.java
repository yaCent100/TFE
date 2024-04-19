package be.iccbxl.tfe.Driveshare.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafConfig {
    @Bean
    LayoutDialect thymeleafDialect() {
        return new LayoutDialect();
    }
}
