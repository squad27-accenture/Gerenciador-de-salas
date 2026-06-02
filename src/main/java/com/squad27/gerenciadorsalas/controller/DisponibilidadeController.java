package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.services.DisponibilidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salas/{salaId}/disponibilidade")
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    public DisponibilidadeController(DisponibilidadeService disponibilidadeService) {
        this.disponibilidadeService = disponibilidadeService;
    }

    @PostMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>> configurar(
            @PathVariable Integer salaId,
            @RequestBody DisponibilidadeDTO dto
    ) {
        return ResponseEntity.ok(disponibilidadeService.configurarDisponibilidade(salaId, dto));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<DisponibilidadePeriodoResponseDTO>> consultarPorPeriodo(
            @PathVariable Integer salaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim,
            @RequestParam(defaultValue = "08:00:00") LocalTime horarioInicio,
            @RequestParam(defaultValue = "18:00:00") LocalTime horarioFim
    ) {
        return ResponseEntity.ok(
                disponibilidadeService.consultarDisponibilidadePorPeriodo(
                        salaId, dataInicio, dataFim, horarioInicio, horarioFim)
        );
    }
    @GetMapping("/assentos")
    public ResponseEntity<List<AssentoStatusDTO>> consultarStatusAssentos(
            @PathVariable Integer salaId,
            @RequestParam LocalDate data,
            @RequestParam(defaultValue = "08:00:00") LocalTime horarioInicio,
            @RequestParam(defaultValue = "18:00:00") LocalTime horarioFim
    ) {
        return ResponseEntity.ok(
                disponibilidadeService.consultarStatusAssentos(salaId, data, horarioInicio, horarioFim));
    }

    @GetMapping
    public ResponseEntity<List<DisponibilidadeResponseDTO>> listar(
            @PathVariable Integer salaId
    ) {
        return ResponseEntity.ok(disponibilidadeService.listarDisponibilidade(salaId));
    }

    @PostMapping("/bloquear")
    public ResponseEntity<DataBloqueadaResponseDTO> bloquearData(
            @PathVariable Integer salaId,
            @RequestBody DataBloqueadaDTO dto
    ) {
        return ResponseEntity.ok(disponibilidadeService.bloquearData(salaId, dto));
    }

    @GetMapping("/bloqueadas")
    public ResponseEntity<List<DataBloqueadaResponseDTO>> listarBloqueadas(
            @PathVariable Integer salaId
    ) {
        return ResponseEntity.ok(disponibilidadeService.listarDatasBloqueadas(salaId));
    }

    @DeleteMapping("/bloqueadas")
    public ResponseEntity<Void> desbloquearData(
            @PathVariable Integer salaId,
            @RequestParam LocalDate data
    ) {
        disponibilidadeService.desbloquearData(salaId, data);
        return ResponseEntity.noContent().build();
    }
}
