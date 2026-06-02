package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AssentoStatusDTO(
        Integer id,
        Integer posicao,
        String tipoAssento,
        Double coordenadaX,
        Double coordenadaY,
        String status,
        List<String> equipamentos
) {}