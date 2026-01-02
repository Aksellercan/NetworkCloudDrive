package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Utilities.SecurityUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityUtility securityUtility;

    public SecurityConfig(SecurityUtility securityUtility) {
        this.securityUtility = securityUtility;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                                // give everyone access to these 2 endpoints
                                .requestMatchers("/api/user/register").permitAll()
                                // but require authentication for any other endpoint
                                .anyRequest()
                                .authenticated()
                        // temporarily disable csrf protection
                )
                .formLogin(formLogin ->
                        formLogin.successHandler(authenticationHandler())
                                .failureHandler(authenticationHandler())) // Use both BASIC and FORM logins
                .csrf(AbstractHttpConfigurer::disable) // blocks POST and cross-platform attacks
                .cors(Customizer.withDefaults())
                // give everyone access to log out
                .logout(LogoutConfigurer::permitAll);
        return http.build();
    }

    @Bean
    public AuthenticationHandler authenticationHandler() {
        return new AuthenticationHandler();
    }

    // default strength is 10 might bump it up to 16
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(securityUtility);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:3000", "http://192.168.1.*:3000", "http://localhost:5173", "http://192.168.1.*:5173"));
        configuration.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "responseType", "Authorization"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS", "DELETE"));
        configuration.setMaxAge(3600L);
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Content-Disposition")); //expose disposition for JS to see
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }


}
