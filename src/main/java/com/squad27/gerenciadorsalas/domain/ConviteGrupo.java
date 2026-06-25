package com.squad27.gerenciadorsalas.domain;

import com.squad27.gerenciadorsalas.enums.StatusConviteGrupo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "convites_grupo")
public class ConviteGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(name = "email_convidado", nullable = false)
    private String emailConvidado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convidado_por_id", nullable = false)
    private Usuario convidadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConviteGrupo status = StatusConviteGrupo.PENDENTE;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;
}