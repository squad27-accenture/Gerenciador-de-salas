package com.squad27.gerenciadorsalas.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservaGrupoDTO (
        LocalTime horarioInicio,
        LocalTime horarioFim,
        LocalDate dataReserva,
        Integer salaId,
        List<Integer> posicoesAssentos
){
}
