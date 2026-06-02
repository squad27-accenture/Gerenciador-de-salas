package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.dto.OcupacaoResponseDTO;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.dto.ReservaGrupoDTO;
import com.squad27.gerenciadorsalas.dto.ReservaResponseDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reserva/")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    @PostMapping("realizarReserva")
    public ResponseEntity<ReservaResponseDTO> realizarReserva(@RequestBody ReservaDTO dto, @AuthenticationPrincipal UserDetails userDetails) {

        Reserva reserva = reservaService.ReservarAssento(dto, userDetails.getUsername());
        return ResponseEntity.ok(new ReservaResponseDTO(reserva));
    }

    @PostMapping("reservaGrupo")
    public ResponseEntity<List<ReservaResponseDTO>> reservaGrupo(@RequestBody ReservaGrupoDTO grupoDTO, @AuthenticationPrincipal UserDetails userDetails) {

        List<Reserva> reservas = reservaService.reservaGrupo(grupoDTO, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservas.stream().map(ReservaResponseDTO::new).toList());
    }

    @PutMapping("{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(
            @PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reserva reserva = reservaService.cancelarReserva(id, userDetails.getUsername(), motivo);
        return ResponseEntity.ok(new ReservaResponseDTO(reserva));
    }

    @PutMapping("grupo/{codigoGrupo}/cancelar")
    public ResponseEntity<List<ReservaResponseDTO>> cancelarReservaGrupo(
            @PathVariable String codigoGrupo,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<Reserva> reservas = reservaService.cancelarReservaGrupo(codigoGrupo, userDetails.getUsername(), motivo);
        return ResponseEntity.ok(reservas.stream().map(ReservaResponseDTO::new).toList());
    }

    @GetMapping("historico")
    public ResponseEntity<List<ReservaResponseDTO>> historico(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer salaId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            Principal principal
    ) {
        return ResponseEntity.ok(
                reservaService.buscarHistorico(usuarioId, salaId, dataInicio, dataFim)
                        .stream()
                        .map(ReservaResponseDTO::new)
                        .toList()
        );
    }

    @GetMapping("ocupacao")
    public ResponseEntity<OcupacaoResponseDTO> relatorioOcupacao(
            @RequestParam Integer salaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim
    ) {
        return ResponseEntity.ok(reservaService.relatórioOcupacao(salaId, dataInicio, dataFim));
    }
}