package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.CriterioProximidade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record IaReservaRequestDTO(
        LocalDate dataReserva,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        Integer grupoId,
        List<Integer> usuarioIds,
        CriterioProximidade criterioProximidade,
        Boolean proximidade
) {
}
