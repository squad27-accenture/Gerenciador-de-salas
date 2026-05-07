package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "usuario")
@Entity

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "email" , unique = true)
    private String email;

    @Column(name = "senha" , unique = true)
    private String senha;


    @Enumerated(EnumType.STRING)
    @Column(name = "role" ,  nullable = false)
    private Role role;

}
