package com.squad27.gerenciadorsalas.dto;

import java.io.Serializable;

public record LayoutProcessingMessageDTO(
        Integer salaId,
        String imagemUrl,
        String callbackUrl
) implements Serializable {}