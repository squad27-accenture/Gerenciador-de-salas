package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "salas")
@Table(name = "salas")
public class Salas{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private  String nome;
    private int capacidade;
    @Enumerated(EnumType.STRING)
    private StatusSala status;
    private String local;

}
