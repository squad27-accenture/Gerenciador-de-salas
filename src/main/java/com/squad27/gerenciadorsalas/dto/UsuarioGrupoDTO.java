package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.enums.TipoFuncionario;

public record UsuarioGrupoDTO(
        Integer id,
        String nome,
        String email,
        TipoFuncionario tipoFuncionario
) {

    public UsuarioGrupoDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getTipoFuncionario()
        );
    }
}