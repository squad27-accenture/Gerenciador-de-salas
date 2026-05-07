package com.squad27.gerenciadorsalas.repository;

import com.squad27.gerenciadorsalas.domain.Sala;
import jakarta.transaction.Transactional;
import org.hibernate.sql.Delete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalaRepository extends JpaRepository<Sala ,Integer> {

    Optional<Sala> findByNumerosala(String numerosala);

    @Transactional
    void deleteByNumerosala(String numerosala);

    boolean existsByNumerosala(String numerosala);

}
