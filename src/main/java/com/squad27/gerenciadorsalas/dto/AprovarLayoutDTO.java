package com.squad27.gerenciadorsalas.dto;

import java.util.List;

public record AprovarLayoutDTO(
        boolean aprovado,
        List<AssentoLayoutDTO> assentos
) {}