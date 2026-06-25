package com.squad27.gerenciadorsalas.dto;

public record IaUsuarioDTO(
        Integer id,
        String nome,
        String email,
        String tipoFuncionario
) {
}