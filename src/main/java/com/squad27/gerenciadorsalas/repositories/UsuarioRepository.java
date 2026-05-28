package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

    Usuario findByEmail(String email);

    boolean existsByEmail(String email);
}
