package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Utilities.Security.SecurityUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final SecurityUtility securityUtility;
    private final Environment env;
    private final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(SecurityUtility securityUtility, Environment env) {
        this.securityUtility = securityUtility;
        this.env = env;
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) {
        http
                .authorizeHttpRequests((requests) -> requests
                                // give everyone access to register endpoint
                                .requestMatchers("/api/user/register").permitAll()
                                // but require authentication for any other endpoint
                                .anyRequest()
                                .authenticated()
                )
                .formLogin(formLogin ->
                        formLogin.successHandler(authenticationHandler())
                                .failureHandler(authenticationHandler())) // Use both BASIC and FORM logins
                .rememberMe(Customizer.withDefaults()) // remember me defaults test
                .cors(Customizer.withDefaults())
                // give everyone access to log out
                .logout(LogoutConfigurer::permitAll);
        if (Boolean.parseBoolean(env.getProperty("use-http-basic-authentication"))) {
            logger.warn("HTTP Basic authentication is enabled");
            // also allow http basic authentication if set
            http.httpBasic(Customizer.withDefaults());
        }

        if (Boolean.parseBoolean(env.getProperty("disable-csrf-protection"))) {
            logger.warn("CSRF protection is disabled");
            // disable csrf if set
            http.csrf(AbstractHttpConfigurer::disable); // blocks POST and cross-platform attacks
        }
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

    // Information about CORS setting
    private boolean checkIfCORSPropertiesAreSet(List<String> properties) {
        return properties.stream().filter(prop -> !prop.isEmpty()).toList().isEmpty();
    }

    private List<List<String>> setupCors() {
        List<String> properties = List.of(
                env.getProperty("cors-allowed-origins-patterns", ""),
                env.getProperty("cors-allowed-headers", ""),
                env.getProperty("cors-allowed-methods", ""),
                env.getProperty("cors-exposed-headers", "")
        );
        List<List<String>> collect = new ArrayList<>();
        if (checkIfCORSPropertiesAreSet(properties)) {
            logger.warn("CORS properties are not set!");
            return collect;
        }
        for (String property : properties) {
            collect.add(List.of(property.replaceAll("\"", "").split(",")));
        }
        return collect;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Apply CORS settings
        List<List<String>> corsSettings = setupCors();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if (corsSettings.isEmpty()) {
            return new CorsFilter(source);
        }
        configuration.setAllowedOriginPatterns(corsSettings.get(0));
        configuration.setAllowedHeaders(corsSettings.get(1));
        configuration.setAllowedMethods(corsSettings.get(2));
        long maxAge = Long.parseLong(env.getProperty("cors-maximum-age", "3600"));
        configuration.setMaxAge(maxAge);
        configuration.setAllowCredentials(Boolean.parseBoolean(env.getProperty("cors-allow-credentials")));
        configuration.setExposedHeaders(corsSettings.get(3)); //expose headers for JS to see
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }
}
