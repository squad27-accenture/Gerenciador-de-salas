package com.squad27.gerenciadorsalas.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IaOpcoesResponseDTO(
        String mensagem,

        @JsonAlias({"pedidoInterpretado", "pedido_interpretado"})
        Object pedidoInterpretado,

        @JsonAlias({"opcoes", "opções", "options", "recomendacoes", "recomendações"})
        List<IaOpcaoReservaDTO> opcoes
) {
}