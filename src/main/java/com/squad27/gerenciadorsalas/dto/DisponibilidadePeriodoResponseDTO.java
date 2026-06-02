package com.squad27.gerenciadorsalas.dto;

import java.time.LocalDate;

public record DisponibilidadePeriodoResponseDTO(
        LocalDate data,
        String diaSemana,
        String status,
        String motivo,
        Integer totalAssentos,
        Integer assentosLivres,
        Integer assentosOcupados,
        Integer assentosInativos
) {}