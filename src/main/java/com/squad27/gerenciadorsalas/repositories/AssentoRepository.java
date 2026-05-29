package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Assento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssentoRepository extends JpaRepository<Assento, Integer> {

    Optional<Assento> findBySalaIdAndPosicao(Integer salaId, Integer posicao);

    List<Assento> findBySalaIdOrderByPosicao(Integer salaId);
}
