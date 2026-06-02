package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.TipoAssentoCustom;

public record TipoAssentoResponseDTO(Integer id, String nome, Boolean ativo) {
    public TipoAssentoResponseDTO(TipoAssentoCustom t) {
        this(t.getId(), t.getNome(), t.getAtivo());
    }
}