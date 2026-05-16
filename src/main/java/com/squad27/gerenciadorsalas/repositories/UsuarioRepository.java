package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuarios, Integer>{

    Usuarios findByEmail(String email);

    boolean existsByEmail(String email);
}
