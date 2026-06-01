package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.Role;

public record UsuarioDTO(String email, String senha, Role role, String username ) {
}
