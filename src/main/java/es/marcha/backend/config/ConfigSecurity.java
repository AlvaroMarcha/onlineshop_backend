package es.marcha.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.marcha.backend.security.JwtFilter;

@Configuration
public class ConfigSecurity {

    private final JwtFilter jwtFilter;

    public ConfigSecurity(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        // Define the urls that are public and those that require authentication
                        .requestMatchers("/auth/**").permitAll() 
                        .requestMatchers("/publico").permitAll()
                        .requestMatchers("/backoffice/**").permitAll()  
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

}
