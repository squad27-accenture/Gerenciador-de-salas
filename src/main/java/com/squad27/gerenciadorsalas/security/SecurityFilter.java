package com.squad27.gerenciadorsalas.security;

import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository repository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String token = recoverToken(request);

        System.out.println(">>> PATH: " + path);
        System.out.println(">>> Authorization existe? " + (request.getHeader("Authorization") != null));
        System.out.println(">>> Token extraído? " + (token != null && !token.isBlank()));

        if (token != null && !token.isBlank()) {
            String email = tokenService.validateToken(token);

            System.out.println(">>> Email extraído do token: " + email);

            if (email != null && !email.isBlank()) {
                Usuario usuario = repository.findByEmail(email).orElse(null);

                System.out.println(">>> Usuário encontrado? " + (usuario != null));

                if (usuario != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            usuario.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println(">>> AUTH SETADA: " + usuario.getEmail());
                    System.out.println(">>> AUTHORITIES: " + usuario.getAuthorities());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.replace("Bearer ", "").trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/docs")
                || path.startsWith("/api/swagger-ui")
                || path.equals("/swagger-ui.html")

                // Auth correto com /api/v1
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/cadastro")
                || path.equals("/api/v1/auth/recuperar-senha")
                || path.equals("/api/v1/auth/redefinir-senha")
                || path.equals("/api/v1/auth/refresh")
                || path.equals("/api/v1/auth/logout")

                // Health
                || path.equals("/api/v1/health")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info");
    }
}