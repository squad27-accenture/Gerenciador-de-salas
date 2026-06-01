package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.enums.DiaSemana;
import com.squad27.gerenciadorsalas.domain.DisponibilidadeSala;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisponibilidadeSalaRepository extends JpaRepository<DisponibilidadeSala, Integer> {

    List<DisponibilidadeSala> findBySalaId(Integer salaId);

    Optional<DisponibilidadeSala> findBySalaIdAndDiaSemana(Integer salaId, DiaSemana diaSemana);
}