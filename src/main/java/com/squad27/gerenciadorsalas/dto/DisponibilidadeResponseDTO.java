package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.DisponibilidadeSala;
import com.squad27.gerenciadorsalas.enums.DiaSemana;

import java.time.LocalTime;

public record DisponibilidadeResponseDTO(
        Integer id,
        DiaSemana diaSemana,
        Boolean aceitaReservas,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento,
        Integer antecedenciaMinimaDias
) {
    public DisponibilidadeResponseDTO(DisponibilidadeSala d) {
        this(
                d.getId(),
                d.getDiaSemana(),
                d.getAceitaReservas(),
                d.getHorarioAbertura(),
                d.getHorarioFechamento(),
                d.getAntecedenciaMinimaDias()
        );
    }
}