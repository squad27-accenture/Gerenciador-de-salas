package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.RefreshToken;
import com.squad27.gerenciadorsalas.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}