package be.iccbxl.tfe.Driveshare.security;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


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
		authenticationProvider.setUserDetailsService(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return authenticationProvider;
	}


	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll();
					auth.requestMatchers("/chat/**", "/chat-websocket/**").permitAll();
					auth.requestMatchers("/account/**").hasRole("USER");
					auth.anyRequest().permitAll();
				})
				.formLogin(form -> {
					form.loginPage("/login").permitAll();
					form.defaultSuccessUrl("/home");
				})
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.ignoringRequestMatchers(new AntPathRequestMatcher("/chat/**"))
						.ignoringRequestMatchers(new AntPathRequestMatcher("/chat-websocket/**"))
				);

		return http.build();
	}



}

	

	

	



