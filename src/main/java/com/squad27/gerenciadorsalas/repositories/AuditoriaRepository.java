package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(String entidade, String entidadeId);
    List<Auditoria> findByUsuarioOrderByCriadoEmDesc(String usuario);
}