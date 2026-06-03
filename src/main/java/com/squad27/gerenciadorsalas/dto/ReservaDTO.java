package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.CriterioProximidade;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReservaDTO(
        LocalTime horarioInicio,
        LocalTime horarioFim,
        LocalDate dataReserva,
        Integer salaId,
        // até 3 tipos preferidos por pessoa, em ordem de preferência
        List<String> tiposPreferidosPessoa1,
        CriterioProximidade criterioProximidade
) {}
