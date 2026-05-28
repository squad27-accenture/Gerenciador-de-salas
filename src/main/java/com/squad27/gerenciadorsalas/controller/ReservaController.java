package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    }
