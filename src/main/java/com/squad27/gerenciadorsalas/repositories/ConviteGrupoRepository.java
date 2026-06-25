package com.squad27.gerenciadorsalas.repositories;

import com.squad27.gerenciadorsalas.domain.ConviteGrupo;
import com.squad27.gerenciadorsalas.enums.StatusConviteGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConviteGrupoRepository extends JpaRepository<ConviteGrupo, Integer> {

    List<ConviteGrupo> findAllByEmailConvidadoIgnoreCaseAndStatus(
            String emailConvidado,
            StatusConviteGrupo status
    );

    List<ConviteGrupo> findAllByGrupoIdAndStatus(
            Integer grupoId,
            StatusConviteGrupo status
    );

    boolean existsByGrupoIdAndEmailConvidadoIgnoreCaseAndStatus(
            Integer grupoId,
            String emailConvidado,
            StatusConviteGrupo status
    );

    Optional<ConviteGrupo> findByIdAndStatus(
            Integer id,
            StatusConviteGrupo status
    );
}