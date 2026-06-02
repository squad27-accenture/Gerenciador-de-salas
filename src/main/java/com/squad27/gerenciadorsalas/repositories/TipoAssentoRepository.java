package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.TipoAssentoCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoAssentoRepository extends JpaRepository<TipoAssentoCustom, Integer> {
    Optional<TipoAssentoCustom> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}