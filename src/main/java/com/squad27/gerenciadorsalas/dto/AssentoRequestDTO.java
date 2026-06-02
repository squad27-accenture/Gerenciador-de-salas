package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;
import com.squad27.gerenciadorsalas.enums.TipoAssento;

import java.util.List;

public record AssentoRequestDTO(
        TipoAssento tipoAssento,
        Double coordenadaX,
        Double coordenadaY,
        String tipoCadeira,
        String tipoMesa,
        List<EquipamentosAssento> equipamentos
) {}