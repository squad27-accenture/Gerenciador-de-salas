package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.DiaSemana;
import java.time.LocalTime;

public record DisponibilidadeDTO(
        DiaSemana diaSemana,
        Boolean aceitaReservas,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento,
        Integer antecedenciaMinimaDias
) {}
