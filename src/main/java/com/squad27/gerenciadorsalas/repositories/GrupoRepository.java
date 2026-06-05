package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    List<Grupo> findAllByAtivoTrue();

    Optional<Grupo> findByIdAndAtivoTrue(Integer id);
}