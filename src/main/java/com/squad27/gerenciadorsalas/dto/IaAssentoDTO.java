package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record IaAssentoDTO(
        Integer id,
        Integer posicao,
        String codigo,
        String tipoAssento,
        Double coordenadaX,
        Double coordenadaY,
        String tipoCadeira,
        String tipoMesa,
        Boolean temComputador,
        Boolean temMonitor,
        Boolean temTela4k,
        List<String> equipamentos
) {
}