package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record OpcoesReservaResponseDTO(
        String mensagem,
        Object pedidoInterpretado,
        List<OpcaoReservaDTO> opcoes) {
}
