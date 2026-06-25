package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record GrupoRequestDTO(
        String nome,
        String descricao,
        Integer liderId,
        List<Integer> usuarioIds
) {
}