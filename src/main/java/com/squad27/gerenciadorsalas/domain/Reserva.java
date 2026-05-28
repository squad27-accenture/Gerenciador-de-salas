package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Reserva")
@Table(name = "reservas")
public class Reserva{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "horario_inicio")
    private LocalTime horarioInicio;
    @Column(name = "horario_fim")
    private LocalTime horarioFim;
    @Column(name = "data_reserva")
    private LocalDate dataReserva;
    @Column(name = "status_reserva")
    @Enumerated(EnumType.STRING)
    private StatusReserva statusReserva;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "posicao")
    private Integer posicaoassento;

    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;
}
