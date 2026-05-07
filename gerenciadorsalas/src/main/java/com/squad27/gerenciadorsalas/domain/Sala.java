package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "sala")
@Entity

public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "NumeroSala" , unique = true)
    private  String numerosala;

    @Column(name = "capacidade")
    private int capacidade;

}
