package com.squad27.gerenciadorsalas.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IaOpcaoReservaDTO(
        @JsonAlias({"opcaoId", "numeroOpcao", "opcao_id", "numero_opcao", "id"})
        Integer opcaoId,

        @JsonAlias({"salaId", "sala_id"})
        Integer salaId,

        @JsonAlias({"salaNome", "nomeSala", "sala_nome", "nome_sala"})
        String salaNome,

        @JsonAlias({"compatibilidade", "compatibilidadePercentual", "compatibilidade_percentual", "score", "pontuacao"})
        Double compatibilidade,

        @JsonAlias({"motivo", "observacao", "justificativa"})
        String motivo,

        @JsonAlias({"assentos", "assentosEscolhidos", "assentos_escolhidos"})
        List<IaAssentoEscolhidoDTO> assentos,

        @JsonAlias({"posicoesAssentos", "posicoes_assentos", "posicoes", "posicoesEscolhidas", "posicoes_escolhidas"})
        List<Integer> posicoesAssentos
) {
}