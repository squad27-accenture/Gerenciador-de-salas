package com.squad27.gerenciadorsalas.dto;

import com.squad27.gerenciadorsalas.domain.ConviteGrupo;
import com.squad27.gerenciadorsalas.enums.StatusConviteGrupo;

import java.time.LocalDateTime;

public record ConviteGrupoResponseDTO(
        Integer id,
        Integer grupoId,
        String grupoNome,
        String emailConvidado,
        String convidadoPor,
        StatusConviteGrupo status,
        LocalDateTime criadoEm,
        LocalDateTime respondidoEm
) {

    public ConviteGrupoResponseDTO(ConviteGrupo convite) {
        this(
                convite.getId(),
                convite.getGrupo().getId(),
                convite.getGrupo().getNome(),
                convite.getEmailConvidado(),
                convite.getConvidadoPor().getEmail(),
                convite.getStatus(),
                convite.getCriadoEm(),
                convite.getRespondidoEm()
        );
    }
}