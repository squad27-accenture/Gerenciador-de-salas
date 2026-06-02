package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    List<Usuario> findAllByDeletadoFalse();

    Optional<Usuario> findByIdAndDeletadoFalse(Integer id);

    @Modifying
    @Query("UPDATE Usuario u SET u.deletado = true WHERE u.id = :id")
    void softDeleteById(Integer id);

    @Modifying
    @Query("UPDATE Usuario u SET u.deletado = true WHERE u.email = :email")
    void softDeleteByEmail(String email);
}