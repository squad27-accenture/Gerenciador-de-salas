package com.squad27.gerenciadorsalas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaDTO(
        LocalTime horarioInicio,
        LocalTime horarioFim,
        LocalDate dataReserva,
        Integer salaId,
        Integer posicaoAssento
) {
}