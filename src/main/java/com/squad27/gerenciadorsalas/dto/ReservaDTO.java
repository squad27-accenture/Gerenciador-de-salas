package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.StatusReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public record ReservaDTO (LocalTime horarioInicio,
                          LocalTime horarioFim,
                          LocalDate dataReserva,
                          Integer posicaoAssento,
                          Integer salaId,
                          Integer usuarioId
        ){
}
