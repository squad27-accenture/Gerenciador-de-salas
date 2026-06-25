package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AssentoOpcaoDTO(
        Integer id,
        Integer posicao,
        String label,
        String tipoAssento,
        List<String> equipamentos,
        Integer usuarioId,
        String usuarioNome,
        String usuarioEmail
) {
}