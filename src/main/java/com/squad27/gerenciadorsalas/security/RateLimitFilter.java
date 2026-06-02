package com.squad27.gerenciadorsalas.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Rotas públicas — limite mais restrito por IP
    private static final int MAX_REQUESTS_PUBLICO = 20;

    // Endpoints autenticados — limite por token
    private static final int MAX_REQUESTS_AUTENTICADO = 200;

    private static final long WINDOW_MILLIS = 60_000; // janela de 1 minuto

    private static final String[] ROTAS_PUBLICAS = {
            "/api/v1/auth/login",
            "/api/v1/auth/cadastro",
            "/api/v1/auth/recuperar-senha",
            "/api/v1/auth/refresh"
    };

    // contadores por IP para rotas públicas
    private final ConcurrentHashMap<String, long[]> contadoresIp = new ConcurrentHashMap<>();

    // contadores por token para rotas autenticadas
    private final ConcurrentHashMap<String, long[]> contadoresToken = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (rotaPublica(uri)) {
            String ip = getIp(request);
            if (excedeuLimite(contadoresIp, ip, MAX_REQUESTS_PUBLICO)) {
                bloquear(response);
                return;
            }
        } else {
            String token = extrairToken(request);
            if (token != null) {
                if (excedeuLimite(contadoresToken, token, MAX_REQUESTS_AUTENTICADO)) {
                    bloquear(response);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean excedeuLimite(ConcurrentHashMap<String, long[]> contadores,
                                  String chave, int limite) {
        long agora = Instant.now().toEpochMilli();

        contadores.compute(chave, (k, v) -> {
            if (v == null || agora - v[1] > WINDOW_MILLIS) {
                return new long[]{1, agora};
            }
            v[0]++;
            return v;
        });

        return contadores.get(chave)[0] > limite;
    }

    private boolean rotaPublica(String uri) {
        for (String rota : ROTAS_PUBLICAS) {
            if (uri.startsWith(rota)) return true;
        }
        return false;
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String getIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void bloquear(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = new ObjectMapper().writeValueAsString(
                Map.of("erro", "Muitas requisições. Tente novamente em instantes.")
        );
        response.getWriter().write(body);
    }
}