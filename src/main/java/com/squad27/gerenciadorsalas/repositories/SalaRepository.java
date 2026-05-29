package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Sala;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Integer>{

    boolean existsByNomeIgnoreCase(String nome);

    void deleteByNome(String nome);
}
