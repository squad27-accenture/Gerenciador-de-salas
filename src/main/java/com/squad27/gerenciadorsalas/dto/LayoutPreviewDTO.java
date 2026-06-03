package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.enums.StatusLayout;
import java.util.List;

public record LayoutPreviewDTO(
        Integer salaId,
        String nomeSala,
        StatusLayout statusLayout,
        String imagemUrl,
        List<AssentoReponseDTO> assentos
) {}