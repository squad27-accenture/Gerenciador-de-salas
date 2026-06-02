package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Auditoria;
import com.squad27.gerenciadorsalas.repositories.AuditoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping
    public ResponseEntity<List<Auditoria>> listarTodos() {
        return ResponseEntity.ok(auditoriaRepository.findAll());
    }

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<Auditoria>> porUsuario(@PathVariable String usuario) {
        return ResponseEntity.ok(
                auditoriaRepository.findByUsuarioOrderByCriadoEmDesc(usuario));
    }

    @GetMapping("/{entidade}/{entidadeId}")
    public ResponseEntity<List<Auditoria>> porEntidade(
            @PathVariable String entidade,
            @PathVariable String entidadeId) {
        return ResponseEntity.ok(
                auditoriaRepository.findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(
                        entidade, entidadeId));
    }
}