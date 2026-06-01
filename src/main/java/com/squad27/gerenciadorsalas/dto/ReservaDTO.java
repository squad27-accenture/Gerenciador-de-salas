package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.CriterioProximidade;
import com.squad27.gerenciadorsalas.enums.TipoAssento;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservaDTO(
        LocalTime horarioInicio,
        LocalTime horarioFim,
        LocalDate dataReserva,
        Integer salaId,
        // até 3 tipos preferidos por pessoa, em ordem de preferência
        List<TipoAssento> tiposPreferidosPessoa1,
        List<TipoAssento> tiposPreferidosPessoa2,
        List<TipoAssento> tiposPreferidosPessoa3,
        Integer quantidadePessoas,
        CriterioProximidade criterioProximidade
) {}
