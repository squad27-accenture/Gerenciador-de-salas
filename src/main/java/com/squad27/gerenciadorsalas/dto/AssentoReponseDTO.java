package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AssentoReponseDTO(
        Integer id,
        Integer posicao,
        String tipoAssento,
        Double coordenadaX,
        Double coordenadaY,
        Boolean ativo,
        List<String> equipamentos) {
}