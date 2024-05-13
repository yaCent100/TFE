package be.iccbxl.tfe.Driveshare.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	CustomUserDetailService customUserDetailService;

	private static final String[] SECURED = {
			"/user/**", "/stripe/**", "/create-payment-intent",
			"/forgot-password", "/reset-password",
			"/change-lang/**", "/admin/**", "/exportCSV", "rss/shows"
			, "/access-denied", "/confirmationReservation","/account/**"
	};

	/*private static final String[] UNSECURED = {  "/login", "/css/**", "/js/**", "/images/**",
			"/shows/**", "/home", "/register"

	};
*/

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationManager(
			CustomUserDetailService userDetailsService,
			BCryptPasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(customUserDetailService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return authenticationProvider;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http.authorizeHttpRequests(auth -> {
					auth.anyRequest().permitAll();
				})
				.formLogin(form -> {
					form.loginPage("/login");
					form.defaultSuccessUrl("/home");

				})
				.csrf(csrf -> csrf.disable())

				.build();

	}



	
}
	

	



