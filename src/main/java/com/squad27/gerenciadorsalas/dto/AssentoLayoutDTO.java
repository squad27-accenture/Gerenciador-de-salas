package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;
import java.util.List;

public record AssentoLayoutDTO(
        Integer posicao,
        Double coordenadaX,
        Double coordenadaY,
        String tipoAssento,
        List<EquipamentosAssento> equipamentos
) {}