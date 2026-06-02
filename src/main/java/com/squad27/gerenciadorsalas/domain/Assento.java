package com.squad27.gerenciadorsalas.domain;

import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;
import com.squad27.gerenciadorsalas.enums.TipoAssento;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "tipo_assento")
    private TipoAssento tipoAssento;

    @Column(name = "coordenada_x")
    private Double coordenadaX;

    @Column(name = "coordenada_y")
    private Double coordenadaY;

    @Column(name = "tipo_cadeira", length = 50)
    private String tipoCadeira;

    @Column(name = "tipo_mesa", length = 50)
    private String tipoMesa;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @ElementCollection(targetClass = EquipamentosAssento.class)
    @CollectionTable(
            name = "assento_equipamentos",
            joinColumns = @JoinColumn(name = "assento_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "equipamento")
    private List<EquipamentosAssento> equipamentos = new ArrayList<>();
}