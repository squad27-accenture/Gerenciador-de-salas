package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record OpcaoReservaDTO(
        Integer id,
        Integer salaId,
        String salaNome,
        Double compatibilidade,
        String observacao,
        List<AssentoOpcaoDTO> assentos
) {
}