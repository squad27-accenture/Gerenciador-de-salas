package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Role;

public record RegisterDTO(String email, String senha, Role role, String username){
}
