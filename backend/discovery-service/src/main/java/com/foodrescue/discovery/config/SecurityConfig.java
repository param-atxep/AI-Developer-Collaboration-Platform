package com.foodrescue.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Eureka Discovery Service.
 *
 * <p>Eureka clients communicate over {@code /eureka/**} using HTTP Basic
 * credentials. CSRF must be disabled for those endpoints because the Eureka
 * client does not send a CSRF token. The dashboard and actuator endpoints
 * remain protected with basic auth and CSRF where applicable.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Filter chain for the Eureka replication / client REST endpoints.
     * CSRF is disabled so that service instances can register and send
     * heartbeats without a CSRF token.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain eurekaClientFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/eureka/**")
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Default filter chain for the Eureka dashboard, actuator, and any other
     * endpoints. Basic authentication is required; CSRF is disabled globally
     * to keep the dashboard accessible via simple browser login.
     */
    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
