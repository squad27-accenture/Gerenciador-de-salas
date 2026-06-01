package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterDTO(@NotBlank(message = "O e-mail não pode estar vazio")
                          @Email(message = "Por favor, insira um endereço de e-mail válido")
                          String email,
                          @NotBlank @Size(min = 6 , message = "Senha deve ter no minimo 6 caracteres")
                          String senha,
                          @NotNull
                          Role role,
                          String username){
}
