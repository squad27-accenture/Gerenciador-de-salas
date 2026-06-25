package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Grupo;

import java.util.List;

public record GrupoResponseDTO(
        Integer id,
        String nome,
        String descricao,
        Boolean ativo,
        UsuarioListagemDTO lider,
        List<UsuarioListagemDTO> usuarios,
        List<ConviteGrupoResponseDTO> convitesPendentes
) {

    public GrupoResponseDTO(Grupo grupo, List<ConviteGrupoResponseDTO> convitesPendentes) {
        this(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getAtivo(),
                grupo.getLider() != null ? new UsuarioListagemDTO(grupo.getLider()) : null,
                grupo.getUsuarios() != null
                        ? grupo.getUsuarios().stream().map(UsuarioListagemDTO::new).toList()
                        : List.of(),
                convitesPendentes != null ? convitesPendentes : List.of()
        );
    }
}