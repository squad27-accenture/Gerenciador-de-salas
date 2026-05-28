package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Sala")
@Table(name = "salas")
@Builder
public class Sala {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private  String nome;
    private Integer capacidade;
    @Enumerated(EnumType.STRING)
    @Column(name = "equipamentos_sala")
    private EquipamentosSala equipamentosSala;
    @Enumerated(EnumType.STRING)
    private StatusSala status;
    private String local;
    private String estado;
    private String cidade;

    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL)
    private List<Assento> assentos = new ArrayList<>();

    public void adicionarasseto(Assento assento){
        assentos.add(assento);
        assento.setSala(this);
    }
}
