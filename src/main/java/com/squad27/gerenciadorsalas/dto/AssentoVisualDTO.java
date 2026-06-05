package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AssentoVisualDTO(
        Integer id,
        Integer posicao,
        String tipoAssento,
        String status,
        Boolean sugerido,
        String tipoCadeira,
        String tipoMesa,
        List<String> equipamentos
) {
}
