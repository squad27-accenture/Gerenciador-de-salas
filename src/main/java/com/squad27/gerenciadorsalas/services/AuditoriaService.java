package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Auditoria;
import com.squad27.gerenciadorsalas.repositories.AuditoriaRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(String operacao, String entidade, String entidadeId,
                          String usuario, String detalhes) {
        auditoriaRepository.save(new Auditoria(operacao, entidade, entidadeId, usuario, detalhes));
    }
}