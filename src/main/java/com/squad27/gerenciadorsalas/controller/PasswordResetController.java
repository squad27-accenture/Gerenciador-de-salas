package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.RedefinirSenhaDTO;
import com.squad27.gerenciadorsalas.dto.SolicitarRecuperacaoDTO;
import com.squad27.gerenciadorsalas.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/recuperar-senha")
    public ResponseEntity<String> solicitarRecuperacao(@RequestBody SolicitarRecuperacaoDTO dto) {
        passwordResetService.solicitarRecuperacao(dto.email());
        return ResponseEntity.ok("Código enviado para o email");
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<String> redefinirSenha(@RequestBody RedefinirSenhaDTO dto) {
        passwordResetService.redefinirSenha(dto.email(), dto.codigo(), dto.novaSenha());
        return ResponseEntity.ok("Senha redefinida com sucesso");
    }
}
