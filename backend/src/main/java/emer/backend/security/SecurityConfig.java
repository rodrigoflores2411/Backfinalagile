package emer.backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> corsConfigurationSource())
           .authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/prestamos").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/clientes/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/prestamos/totales/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/prestamos/historial**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/cronograma/descargar/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/cuadre-caja/excel").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/cuotas/**").permitAll()
    .requestMatchers(HttpMethod.PUT, "/api/cuotas/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/cuotas/**").permitAll()
    .requestMatchers(HttpMethod.DELETE, "/api/cuotas/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/comprobantes/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/comprobantes/**").permitAll()
    .requestMatchers(HttpMethod.PUT, "/api/comprobantes/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/pagos/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/pagos/**").permitAll()
    .requestMatchers(HttpMethod.PUT, "/api/pagos/**").permitAll()
    .requestMatchers(HttpMethod.DELETE, "/api/pagos/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/pagos/mp/link").permitAll()

    .anyRequest().authenticated()
);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://frontprestamoscastil-898-67f93.web.app", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
