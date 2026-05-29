package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.RefreshToken;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken criarRefreshToken(Usuario usuario) {
        // Remove o token anterior se existir
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiracao(Instant.now().plusSeconds(604800)) // 7 dias
                .revogado(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken validar(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token inválido"));

        if (refreshToken.isRevogado()) {
            throw new RuntimeException("Refresh token revogado");
        }
        if (refreshToken.getExpiracao().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expirado");
        }

        return refreshToken;
    }

    @Transactional
    public void revogar(Usuario usuario) {
        refreshTokenRepository.deleteByUsuario(usuario);
    }
}