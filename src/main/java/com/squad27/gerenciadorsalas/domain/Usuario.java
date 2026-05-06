package com.squad27.gerenciadorsalas.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Usuario {

    private String email;
    private String senha;
    private Role role;

}
