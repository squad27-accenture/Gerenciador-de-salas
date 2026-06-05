package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Grupo;

import java.util.List;

public record GrupoResponseDTO(
        Integer id,
        String nome,
        String descricao,
        Boolean ativo,
        List<UsuarioGrupoDTO> usuarios
) {

    public GrupoResponseDTO(Grupo grupo) {
        this(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getAtivo(),
                grupo.getUsuarios()
                        .stream()
                        .map(UsuarioGrupoDTO::new)
                        .toList()
        );
    }
}