package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalaRepository extends JpaRepository<Sala, Integer> {

    boolean existsByNomeIgnoreCaseAndDeletadoFalse(String nome);

    List<Sala> findAllByDeletadoFalse();

    Optional<Sala> findByIdAndDeletadoFalse(Integer id);

    @Modifying
    @Query("UPDATE Sala s SET s.deletado = true WHERE s.id = :id")
    void softDeleteById(Integer id);

    @Modifying
    @Query("UPDATE Sala s SET s.deletado = true WHERE s.nome = :nome")
    void softDeleteByNome(String nome);
}
