package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaResponseDTO(Integer id,
                                 LocalTime horarioInicio,
                                 LocalTime horarioFim,
                                 LocalDate dataReserva,
                                 Integer posicaoAssento,
                                 String statusReserva,
                                 String codigoGrupo,
                                 Integer salaId,
                                 String nomeSala,
                                 String motivoCancelamento) {
    public ReservaResponseDTO(Reserva reserva) {
        this(
                reserva.getId(),
                reserva.getHorarioInicio(),
                reserva.getHorarioFim(),
                reserva.getDataReserva(),
                reserva.getPosicaoAssento(),
                reserva.getStatusReserva().name(),
                reserva.getCodigoGrupo(),
                reserva.getSala().getId(),
                reserva.getSala().getNome(),
                reserva.getMotivoCancelamento()
        );
    }
}