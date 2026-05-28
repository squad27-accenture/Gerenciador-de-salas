package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Assento")
@Table(name = "assentos")
public class Assento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;

    private int posicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipamento_assento")
    private EquipamentosAssento equipamentoAssento;
}
