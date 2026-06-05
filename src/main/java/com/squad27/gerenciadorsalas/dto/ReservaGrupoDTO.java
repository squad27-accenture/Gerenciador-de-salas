package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.CriterioProximidade;
import com.squad27.gerenciadorsalas.enums.TipoAssento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservaGrupoDTO(
        LocalTime horarioInicio,
        LocalTime horarioFim,
        LocalDate dataReserva,
        Integer salaId,
        Integer quantidadePessoas,
        List<List<String>> tiposPreferidosPorPessoa, // cada índice = uma pessoa, até 3 tipos
        CriterioProximidade criterioProximidade


) {}
