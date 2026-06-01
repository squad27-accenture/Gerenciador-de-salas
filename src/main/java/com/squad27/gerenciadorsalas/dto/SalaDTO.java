package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.EquipamentosSala;
import com.squad27.gerenciadorsalas.enums.StatusSala;

import java.util.List;

public record SalaDTO(
        String nome,
        Integer capacidade,
        StatusSala statusSala,
        String local,
        List<EquipamentosSala> equipamentosSala,
        String cidade,
        String estado,
        Double raioProximidade
) {}