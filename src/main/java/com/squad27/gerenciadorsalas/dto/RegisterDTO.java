package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDTO(@NotBlank(message = "O e-mail não pode estar vazio")
                          @Email(message = "Por favor, insira um endereço de e-mail válido")
                          String email,
                          String senha,
                          Role role,
                          String username){
}
