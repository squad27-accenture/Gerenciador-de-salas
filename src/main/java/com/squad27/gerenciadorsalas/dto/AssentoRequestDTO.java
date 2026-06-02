package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;

import java.util.List;

public record AssentoRequestDTO(
        String tipoAssento,
        Double coordenadaX,
        Double coordenadaY,
        String tipoCadeira,
        String tipoMesa,
        List<EquipamentosAssento> equipamentos
) {}