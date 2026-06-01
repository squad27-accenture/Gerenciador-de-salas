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

    // Máximo de requisições por janela de tempo
    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MILLIS = 60_000; // 1 minuto

    // Apenas rotas sensíveis são limitadas
    private static final String[] ROTAS_LIMITADAS = {
            "/api/v1/auth/login",
            "/api/v1/auth/cadastro",
            "/api/v1/auth/recuperar-senha",
            "/api/v1/auth/refresh"
    };

    private final ConcurrentHashMap<String, long[]> contadores = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (!rotaLimitada(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getIp(request);
        long agora = Instant.now().toEpochMilli();

        contadores.compute(ip, (k, v) -> {
            if (v == null || agora - v[1] > WINDOW_MILLIS) {
                return new long[]{1, agora}; // [contador, início da janela]
            }
            v[0]++;
            return v;
        });

        long[] estado = contadores.get(ip);
        if (estado[0] > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            String body = new ObjectMapper().writeValueAsString(
                    Map.of("erro", "Muitas requisições. Tente novamente em instantes.")
            );
            response.getWriter().write(body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean rotaLimitada(String uri) {
        for (String rota : ROTAS_LIMITADAS) {
            if (uri.startsWith(rota)) return true;
        }
        return false;
    }

    private String getIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
