package com.squad27.gerenciadorsalas.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.squad27.gerenciadorsalas.domain.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario , Integer> {

    Optional<Usuario> findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);

    boolean existsByEmail(String email);


}
