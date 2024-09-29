package be.iccbxl.tfe.Driveshare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] PUBLIC_ENDPOINTS = {
			"/css/**", "/images/**", "/icons/**","/icones/**", "/js/**", "/uploads/**",
			"/home", "/register", "/login", "/cars/**", "/api/cars/top-rated",
			"/reset-password**", "/swagger-ui/**", "/v3/api-docs/**",
			"/swagger-ui.html", "/swagger-ui/index.html", "/api/claims/**",
			"/api/notifications/complaint", "/api/stats/**", "/api/cars/**",
			"/api/gearbox", "/api/cars/search", "/api/categories", "/api/files/**",
			"/api/password/**","/faq", "/comment-ca-marche", "/condition-utilisation",
			"/api/messages/reservation/**","/api/motorisation", "/api/kilometrage", "/api/places","/chat-websocket/**",
			"/api/format-date","/api/deleteCar/**","/api/addPhoto","/api/deletePhoto/**","/documents/**","/louer-ma-voiture"
			,"/api/unavailable-dates/**"
	};

	private static final String[] ADMIN_ENDPOINTS = {
			"/api/admin/**", "/admin/**", "/api/dashboard/**", "/api/review",
			"/api/admin/users/**", "/api/admin/payments", "/api/admin/evaluations","/api/admin/cars/**","/api/files/**"
	};

	private static final String[] USER_ENDPOINTS = {
			"/account/**", "/reservation/**", "/checkout","/api/reservations/**",
			"/evaluations/exists/**","/api/notifications/**","/api/claims/close/**","/api/claims/**","/api/getOwnerReservations",
			"/api/getRenterReservations"
	};

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider(CustomUserDetailService customUserDetailService) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(customUserDetailService);
		authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
		return authenticationProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> {
					// Public access
					auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
					// Authenticated user access
					auth.requestMatchers(USER_ENDPOINTS).authenticated();
					// Admin access
					auth.requestMatchers(ADMIN_ENDPOINTS).hasRole("Admin");

					// All other requests
					auth.anyRequest().authenticated();
				})
				.formLogin(form -> form
						.loginPage("/login").permitAll()
						.defaultSuccessUrl("/home", true)
				)
				.exceptionHandling(handling -> handling
						.accessDeniedHandler(accessDeniedHandler())  // Custom handler for Access Denied
				)
				.logout(logout -> logout
						.logoutUrl("/logout").permitAll()
						.logoutSuccessUrl("/home")
				)
				.headers(frameOptions->frameOptions.disable())

		;

		return http.build();
	}

	// Custom AccessDeniedHandler to manage 403 errors
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> {
			response.sendRedirect("/error/403"); // Redirect to custom 403 error page
		};
	}
}
