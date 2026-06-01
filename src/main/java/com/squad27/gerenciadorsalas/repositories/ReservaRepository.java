package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @Query("""
        SELECT r FROM Reserva r
        WHERE (:usuarioId IS NULL OR r.usuario.id = :usuarioId)
        AND (:salaId IS NULL OR r.sala.id = :salaId)
        AND (CAST(:dataInicio AS java.time.LocalDate) IS NULL OR r.dataReserva >= :dataInicio)
        AND (CAST(:dataFim AS java.time.LocalDate) IS NULL OR r.dataReserva <= :dataFim)
        ORDER BY r.dataReserva DESC, r.horarioInicio DESC
        """)
    List<Reserva> buscarHistorico(
            @Param("usuarioId") Integer usuarioId,
            @Param("salaId") Integer salaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );
}
