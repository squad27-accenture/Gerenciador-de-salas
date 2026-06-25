package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.enums.Role;
import com.squad27.gerenciadorsalas.enums.TipoFuncionario;

public record UsuarioListagemDTO(
        Integer id,
        String nome,
        String email,
        Role role,
        TipoFuncionario tipoFuncionario
) {

    public UsuarioListagemDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getTipoFuncionario()
        );
    }
}