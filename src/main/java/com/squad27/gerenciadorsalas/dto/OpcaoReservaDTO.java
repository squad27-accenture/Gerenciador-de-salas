package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record OpcaoReservaDTO(
        Integer numeroOpcao,
        Integer salaId,
        String nomeSala,
        String localizacaoSala,
        String andar,
        String bloco,
        String cidade,
        String estado,
        List<String> equipamentosSala,
        List<AssentoVisualDTO> assentosSugeridos,
        List<AssentoVisualDTO> assentosDaSala,
        String criterioProximidade,
        String observacao) {
}
