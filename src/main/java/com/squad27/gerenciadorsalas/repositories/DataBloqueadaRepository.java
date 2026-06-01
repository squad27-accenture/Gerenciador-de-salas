package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.DataBloqueada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataBloqueadaRepository extends JpaRepository<DataBloqueada, Integer> {

    List<DataBloqueada> findBySalaId(Integer salaId);

    Optional<DataBloqueada> findBySalaIdAndData(Integer salaId, LocalDate data);
}