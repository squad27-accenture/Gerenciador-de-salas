package com.squad27.gerenciadorsalas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
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
                .authorizeHttpRequests(auth -> auth

                        /*
                         * CORS / PRE-FLIGHT
                         */
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        /*
                         * SWAGGER / DOCS
                         */
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/docs",
                                "/api/docs/**",
                                "/api/swagger-ui/**"
                        ).permitAll()

                        /*
                         * AUTH — PÚBLICO
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/cadastro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/recuperar-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/redefinir-senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()

                        /*
                         * HEALTH
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        /*
                         * SALAS
                         *
                         * Controller:
                         * GET    /api/v1/salas
                         * GET    /api/v1/salas/{id}
                         * POST   /api/v1/salas
                         * PUT    /api/v1/salas/{id}
                         * DELETE /api/v1/salas/{id}
                         * GET    /api/v1/salas/ocupados
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/ocupados").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/salas")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/salas/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/salas/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * LAYOUT DE SALA
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/salas/*/layout/upload")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/layout-preview")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/salas/*/layout")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/salas/*/layout/resultado")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * ASSENTOS
                         *
                         * Se teu AssentoController usa:
                         * /api/v1/salas/{salaId}/assentos
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/assentos")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/salas/*/assentos")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/salas/*/assentos/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/salas/*/assentos/*/inativar")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/salas/*/assentos/*/reativar")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * DISPONIBILIDADE
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/disponibilidade")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/salas/*/disponibilidade/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/salas/*/disponibilidade")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/v1/salas/*/disponibilidade/bloquear")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/salas/*/disponibilidade/bloqueadas")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * RESERVAS
                         *
                         * Controller:
                         * POST /api/v1/reservas
                         * POST /api/v1/reservas/confirmar-opcao
                         * PUT  /api/v1/reservas/{id}/cancelar
                         * PUT  /api/v1/reservas/grupo/{codigoGrupo}/cancelar
                         * GET  /api/v1/reservas/historico
                         * GET  /api/v1/reservas/ocupacao
                         */
                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/reservas/confirmar-opcao")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservas/*/cancelar")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/reservas/grupo/*/cancelar")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/historico")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/ocupacao")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * USUÁRIOS
                         *
                         * Controller:
                         * GET    /api/v1/usuarios
                         * GET    /api/v1/usuarios/listarUsuarios
                         * GET    /api/v1/usuarios/meuPerfil
                         * PUT    /api/v1/usuarios
                         * PUT    /api/v1/usuarios/me/tipo-funcionario
                         * DELETE /api/v1/usuarios/deletarConta
                         * DELETE /api/v1/usuarios/{id}
                         */
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/listarUsuarios")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/meuPerfil")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios")
                        .authenticated()

                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/me/tipo-funcionario")
                        .authenticated()

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/deletarConta")
                        .hasAnyAuthority(
                                "ROLE_USER", "USER",
                                "ROLE_ADMIN", "ADMIN",
                                "ROLE_TECHLEADER", "TECHLEADER"
                        )

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * GRUPOS
                         *
                         * Controller:
                         * POST   /api/v1/grupos
                         * GET    /api/v1/grupos
                         * GET    /api/v1/grupos/{id}
                         * PUT    /api/v1/grupos/{id}
                         * DELETE /api/v1/grupos/{id}
                         * POST   /api/v1/grupos/{id}/convites
                         * GET    /api/v1/grupos/convites/me
                         * POST   /api/v1/grupos/convites/{id}/aceitar
                         * POST   /api/v1/grupos/convites/{id}/recusar
                         */

                        // Convites primeiro, para não confundir com /grupos/{id}
                        .requestMatchers(HttpMethod.GET, "/api/v1/grupos/convites/me")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/grupos/convites/*/aceitar")
                        .authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/grupos/convites/*/recusar")
                        .authenticated()

                        // Listar/ver grupos
                        .requestMatchers(HttpMethod.GET, "/api/v1/grupos")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/v1/grupos/*")
                        .authenticated()

                        // Criar/editar/deletar/convidar
                        .requestMatchers(HttpMethod.POST, "/api/v1/grupos")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_TECHLEADER", "TECHLEADER")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/grupos/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_TECHLEADER", "TECHLEADER")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/grupos/*")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_TECHLEADER", "TECHLEADER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/grupos/*/convites")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_TECHLEADER", "TECHLEADER")

                        /*
                         * IA
                         */
                        .requestMatchers("/api/v1/ia/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_TECHLEADER", "TECHLEADER")

                        /*
                         * AUDITORIA
                         */
                        .requestMatchers("/api/v1/auditoria/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        /*
                         * QUALQUER OUTRA ROTA
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * IMPORTANTE:
                 * rateLimit antes do securityFilter pode bloquear login se ele estiver mal configurado.
                 * Se continuar dando problema no login, comenta temporariamente a linha do rateLimit.
                 */
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*"
        ));

        config.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}