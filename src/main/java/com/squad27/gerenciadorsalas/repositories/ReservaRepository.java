package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.domain.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reserva r
        WHERE r.sala.id = :salaId
        AND r.posicaoAssento = :posicaoAssento
        AND r.dataReserva = :dataReserva
        AND r.statusReserva <> :statusCancelada
        AND :horarioInicio < r.horarioFim
        AND :horarioFim > r.horarioInicio
    """)
    boolean existeConflitoDeHorario(
            Integer salaId,
            Integer posicaoAssento,
            LocalDate dataReserva,
            LocalTime horarioInicio,
            LocalTime horarioFim,
            StatusReserva statusCancelada
    );

    List<Reserva> findByCodigoGrupo(String codigoGrupo);

    @Query("""
    SELECT DISTINCT r.posicaoAssento
    FROM Reserva r
    WHERE r.sala.id = :salaId
    AND r.dataReserva = :dataReserva
    AND r.statusReserva <> :statusCancelada
    AND :horarioInicio < r.horarioFim
    AND :horarioFim > r.horarioInicio
    ORDER BY r.posicaoAssento
""")
    List<Integer> buscarPosicoesOcupadas(
            Integer salaId,
            LocalDate dataReserva,
            LocalTime horarioInicio,
            LocalTime horarioFim,
            StatusReserva statusCancelada
    );
}
