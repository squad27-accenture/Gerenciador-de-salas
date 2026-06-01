package com.squad27.gerenciadorsalas.dto;

import java.time.LocalDate;

public record DataBloqueadaDTO(
        LocalDate data,
        String motivo
) {}