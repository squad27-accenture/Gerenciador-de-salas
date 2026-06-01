package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.DiaSemana;
import java.time.LocalTime;
import java.util.List;

public record DisponibilidadeDTO(
        List<DiaSemana> diasSemana,
        Boolean aceitaReservas,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento,
        Integer antecedenciaMinimaDias
) {}
