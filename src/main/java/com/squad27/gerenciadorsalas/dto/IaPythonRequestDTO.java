package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.CriterioProximidade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record IaPythonRequestDTO(
        LocalDate dataReserva,
        LocalTime horarioInicio,
        LocalTime horarioFim,

        Integer quantidadeAssentos,
        Integer quantidadePessoas,

        Integer grupoId,
        Integer salaId,
        String nomeSala,

        String tipoFuncionario,

        Boolean proximidade,
        CriterioProximidade criterioProximidade,

        List<String> equipamentosObrigatorios,
        List<String> equipamentosDesejaveis,
        List<String> tiposAssentoPreferidos,

        String cidade,
        String estado,

        Integer limiteResultados,
        Boolean incluirIncompativeis,
        Integer compatibilidadeMinima,
        Boolean usarIaGenerativa,
        Boolean incluirMapaAssentos,

        List<IaUsuarioDTO> usuarios,
        List<IaSalaDTO> salas
) {
}