package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AgentLayoutResultDTO(
        Integer salaId,
        List<AgentPosicaoDTO> posicoes
) {}