package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "salas")
@Table(name = "salas")
@Builder
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private  String nome;
    private Integer capacidade;
    @Enumerated(EnumType.STRING)
    private StatusSala status;
    private String local;

}
