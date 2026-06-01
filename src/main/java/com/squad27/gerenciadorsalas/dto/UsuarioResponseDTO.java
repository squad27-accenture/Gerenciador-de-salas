package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.Role;

public record UsuarioResponseDTO(Integer id,
                                 String username,
                                 String email,
                                 Role role) {}
