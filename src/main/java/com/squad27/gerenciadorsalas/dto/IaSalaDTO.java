package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record IaSalaDTO(
        Integer id,
        String nome,
        Integer capacidade,
        String localizacao,
        String andar,
        String bloco,
        String cidade,
        String estado,
        Double raioProximidade,
        List<String> equipamentosSala,
        List<IaAssentoDTO> assentosLivres
) {
}