package com.squad27.gerenciadorsalas.dto;

import java.time.LocalDate;

public record OcupacaoResponseDTO(
        Integer salaId,
        String nomeSala,
        LocalDate dataInicio,
        LocalDate dataFim,
        int totalReservas,
        int totalAssentos,
        double taxaOcupacaoPercent
) {}
