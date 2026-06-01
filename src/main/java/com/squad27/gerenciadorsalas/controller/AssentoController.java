package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
import com.squad27.gerenciadorsalas.dto.AssentoRequestDTO;
import com.squad27.gerenciadorsalas.services.AssentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/salas/{salaId}/assentos")
public class AssentoController {

    private final AssentoService assentoService;

    public AssentoController(AssentoService assentoService) {
        this.assentoService = assentoService;
    }

    @PostMapping
    public ResponseEntity<AssentoReponseDTO> criar(
            @PathVariable Integer salaId,
            @RequestBody AssentoRequestDTO dto) {

        Assento assento = assentoService.criarAssento(salaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(assento));
    }

    @PutMapping("/{posicao}")
    public ResponseEntity<AssentoReponseDTO> atualizar(
            @PathVariable Integer salaId,
            @PathVariable Integer posicao,
            @RequestBody AssentoRequestDTO dto) {

        Assento assento = assentoService.atualizarAssento(salaId, posicao, dto);
        return ResponseEntity.ok(toDTO(assento));
    }

    @PatchMapping("/{posicao}/inativar")
    public ResponseEntity<AssentoReponseDTO> inativar(
            @PathVariable Integer salaId,
            @PathVariable Integer posicao) {

        Assento assento = assentoService.inativarAssento(salaId, posicao);
        return ResponseEntity.ok(toDTO(assento));
    }

    @PatchMapping("/{posicao}/reativar")
    public ResponseEntity<AssentoReponseDTO> reativar(
            @PathVariable Integer salaId,
            @PathVariable Integer posicao) {

        Assento assento = assentoService.reativarAssento(salaId, posicao);
        return ResponseEntity.ok(toDTO(assento));
    }

    private AssentoReponseDTO toDTO(Assento assento) {
        return new AssentoReponseDTO(
                assento.getId(),
                assento.getPosicao(),
                assento.getTipoAssento() == null ? null : assento.getTipoAssento().name(),
                assento.getCoordenadaX(),
                assento.getCoordenadaY(),
                assento.getAtivo(),
                assento.getEquipamentos().stream().map(Enum::name).toList()
        );
    }
}