package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record IaAssentoEscolhidoDTO(
        Integer assentoId,
        Integer posicao,
        String label,
        String tipoAssento,
        List<String> equipamentos,
        Integer usuarioId,
        String usuarioNome,
        String usuarioEmail
) {
        public IaAssentoEscolhidoDTO(Integer assentoId, Integer posicao) {
                this(
                        assentoId,
                        posicao,
                        posicao != null ? "A" + posicao : null,
                        "ESTACAO_PADRAO",
                        List.of(),
                        null,
                        null,
                        null
                );
        }
}