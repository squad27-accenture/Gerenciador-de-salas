package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.GrupoRequestDTO;
import com.squad27.gerenciadorsalas.dto.GrupoResponseDTO;
import com.squad27.gerenciadorsalas.services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody GrupoRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(grupoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(grupoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable Integer id,
            @RequestBody GrupoRequestDTO dto
    ) {
        return ResponseEntity.ok(grupoService.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Integer id) {
        grupoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}