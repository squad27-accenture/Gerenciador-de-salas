package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.TipoAssentoRequestDTO;
import com.squad27.gerenciadorsalas.dto.TipoAssentoResponseDTO;
import com.squad27.gerenciadorsalas.services.TipoAssentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-assento")
public class TipoAssentoController {

    private final TipoAssentoService service;

    public TipoAssentoController(TipoAssentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TipoAssentoResponseDTO> criar(@RequestBody TipoAssentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<TipoAssentoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<TipoAssentoResponseDTO> inativar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.inativar(id));
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<TipoAssentoResponseDTO> reativar(@PathVariable Integer id) {
        return ResponseEntity.ok(service.reativar(id));
    }
}