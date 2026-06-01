package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.DataBloqueada;

import java.time.LocalDate;

public record DataBloqueadaResponseDTO(
        Integer id,
        LocalDate data,
        String motivo
) {
    public DataBloqueadaResponseDTO(DataBloqueada d) {
        this(d.getId(), d.getData(), d.getMotivo());
    }
}