package com.squad27.gerenciadorsalas.dto;

public record AssentoOpcaoDTO(
        Integer id,
        Integer posicao,
        String tipoAssento,
        Double coordenadaX,
        Double coordenadaY
) {
}
