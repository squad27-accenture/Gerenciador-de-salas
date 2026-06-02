package com.squad27.gerenciadorsalas.domain;

import com.squad27.gerenciadorsalas.enums.EquipamentosSala;
import com.squad27.gerenciadorsalas.enums.StatusSala;
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

    @ElementCollection(targetClass = EquipamentosSala.class)
    @CollectionTable(
        name = "sala_equipamentos",
            joinColumns = @JoinColumn(name = "sala_id")

    )
    @Enumerated(EnumType.STRING)
    @Column(name = "equipamento")
    private List<EquipamentosSala> equipamentosSala;


    @Builder.Default
    @Column(nullable = false)
    private Boolean deletado = false;

    @Column(name = "raio_proximidade", nullable = false)
    private Double raioProximidade = 5.0;



    @Enumerated(EnumType.STRING)
    private StatusSala status;
    private String local;
    private String estado;
    private String cidade;

    @Builder.Default
    @OneToMany(mappedBy = "sala", cascade = CascadeType.ALL)
    private List<Assento> assentos = new ArrayList<>();

    public void adicionarasseto(Assento assento){
        assentos.add(assento);
        assento.setSala(this);
    }
}
