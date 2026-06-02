package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String operacao;

    @Column(nullable = false, length = 50)
    private String entidade;

    @Column(name = "entidade_id", length = 50)
    private String entidadeId;

    @Column(length = 255)
    private String usuario;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Auditoria() {}

    public Auditoria(String operacao, String entidade, String entidadeId,
                     String usuario, String detalhes) {
        this.operacao = operacao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.usuario = usuario;
        this.detalhes = detalhes;
        this.criadoEm = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getOperacao() { return operacao; }
    public String getEntidade() { return entidade; }
    public String getEntidadeId() { return entidadeId; }
    public String getUsuario() { return usuario; }
    public String getDetalhes() { return detalhes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}