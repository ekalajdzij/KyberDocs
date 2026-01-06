package com.kyberdocs.docs.security;

import com.kyberdocs.docs.users.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig {

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private AuthTokenFilter authenticationJwtTokenFilter;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/api/kyber-server").permitAll()
                                // Permissions Check
                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/audit/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/documents/admin/**").hasAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/beneficiaries/admin/**").hasAnyAuthority("ROLE_ADMIN")
                                .requestMatchers("/api/documents/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                .requestMatchers("/api/beneficiaries/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                                .requestMatchers(
                                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                );

        // --- FIX 3: Use the injected variable ---
        http.addFilterBefore(
                authenticationJwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}