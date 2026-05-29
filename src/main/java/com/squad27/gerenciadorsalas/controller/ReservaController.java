package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.dto.ReservaGrupoDTO;
import com.squad27.gerenciadorsalas.dto.ReservaResponseDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reserva/")
public class ReservaController {

@Autowired
private ReservaService reservaService;

    @PostMapping("realizarReserva")
    public ResponseEntity<Reserva> RealizarReserva(@RequestBody ReservaDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        {

            Reserva reserva = reservaService.ReservarAssento(dto, userDetails.getUsername());
            return ResponseEntity.ok(reserva);
        }
    }

    @PostMapping("reservaGrupo")
    public ResponseEntity<List<ReservaResponseDTO>> reservaGrupo(
            @RequestBody ReservaGrupoDTO grupoDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Reserva> reservas = reservaService.reservaGrupo(
                grupoDTO,
                userDetails.getUsername()
        );

        List<ReservaResponseDTO> resposta = reservas.stream()
                .map(ReservaResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }


    @PutMapping("{id}/cancelar")
    public ResponseEntity<Reserva> cancelarReserva(
            @PathVariable Integer id, @AuthenticationPrincipal UserDetails userDetails)
    {
        Reserva reserva = reservaService.cancelarReserva(id, userDetails.getUsername());
        return ResponseEntity.ok(reserva);
    }

    @PutMapping("grupo/{codigoGrupo}/cancelar")
    public ResponseEntity<List<Reserva>> cancelarReservaGrupo(
            @PathVariable String codigoGrupo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Reserva> reservas = reservaService.cancelarReservaGrupo(
                codigoGrupo,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(reservas);
    }

    }
