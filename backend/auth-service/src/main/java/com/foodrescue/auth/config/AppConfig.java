package com.foodrescue.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-level bean configuration.
 */
@Configuration
public class AppConfig {

    /**
     * Provides a BCrypt password encoder with strength 12 for hashing user passwords.
     * BCrypt is intentionally slow, making brute-force attacks computationally expensive.
     *
     * @return a BCrypt-based {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
