package com.squad27.gerenciadorsalas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    SecurityFilter securityFilter;
    @Autowired
    RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            String body = new ObjectMapper().writeValueAsString(
                                    Map.of("erro", "Token ausente ou inválido. Faça login primeiro.")
                            );
                            response.getWriter().write(body);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            if (request.getUserPrincipal() == null) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                String body = new ObjectMapper().writeValueAsString(
                                        Map.of("erro", "Token ausente ou inválido. Faça login primeiro.")
                                );
                                response.getWriter().write(body);
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                String body = new ObjectMapper().writeValueAsString(
                                        Map.of("erro", "Acesso negado. Você não tem permissão para realizar esta ação.")
                                );
                                response.getWriter().write(body);
                            }
                        })
                )
                .authorizeHttpRequests(authorizate -> authorizate
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Auth — público
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/cadastro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/recuperar-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        // Salas — operações de escrita exigem ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/v1/salas/CadastrarSala").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/salas/AtualizarSala").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/salas/DeletarSala").hasRole("ADMIN")
                        // Assentos — criar/editar/inativar exigem ADMIN
                        .requestMatchers(HttpMethod.POST,  "/api/v1/salas/*/assentos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,   "/api/v1/salas/*/assentos/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/salas/*/assentos/*/inativar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/salas/*/assentos/*/reativar").hasRole("ADMIN")
                        // Disponibilidade — configuração exige ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/v1/salas/*/disponibilidade").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/v1/salas/*/disponibilidade/bloquear").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/salas/*/disponibilidade/bloqueadas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/api/v1/salas/*/disponibilidade").authenticated()
                        .requestMatchers(HttpMethod.GET,    "/api/v1/salas/*/disponibilidade/**").authenticated()
                        // Auditoria — somente ADMIN
                        .requestMatchers("/api/v1/auditoria/**").hasRole("ADMIN")
                        // Usuários
                        .requestMatchers(HttpMethod.GET,    "/api/v1/usuarios/listarUsuarios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/deletarConta").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/**").hasRole("ADMIN")
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}