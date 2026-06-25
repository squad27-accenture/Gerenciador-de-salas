package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.IaOpcoesResponseDTO;
import com.squad27.gerenciadorsalas.dto.IaReservaRequestDTO;
import com.squad27.gerenciadorsalas.services.IaReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ia")
public class IaController {
    private final IaReservaService iaReservaService;

    public IaController(IaReservaService iaReservaService) {
        this.iaReservaService = iaReservaService;
    }

    @PostMapping("/opcoes")
    public ResponseEntity<IaOpcoesResponseDTO> gerarOpcoes(
            @RequestBody IaReservaRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                iaReservaService.gerarOpcoes(dto, userDetails.getUsername())
        );
    }
}

