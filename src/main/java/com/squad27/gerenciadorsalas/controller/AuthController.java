package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.RefreshToken;
import com.squad27.gerenciadorsalas.services.AuthorizationService;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.AuthorizationDTO;
import com.squad27.gerenciadorsalas.dto.RegisterDTO;
import com.squad27.gerenciadorsalas.security.TokenService;
import com.squad27.gerenciadorsalas.services.RefreshTokenService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final AuthorizationService authorizationService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody AuthorizationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            Usuario usuario = (Usuario) auth.getPrincipal();

            var accessToken = tokenService.generateToken(usuario);
            var refreshToken = refreshTokenService.criarRefreshToken(usuario);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken.getToken()
            ));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("erro", "Email ou senha incorretos."));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("erro", "Usuário não encontrado"));
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<String> cadastro(@RequestBody @Valid RegisterDTO registerDTO) {
        try {
            authorizationService.cadastro(registerDTO);
            return ResponseEntity.ok("Cadastro realizado!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já cadastrado.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Recurso não encontrado.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

        @PostMapping("/refresh")
        public ResponseEntity<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
            try {
                String token = body.get("refreshToken");
                RefreshToken refreshToken = refreshTokenService.validar(token);
                String novoAccessToken = tokenService.generateToken(refreshToken.getUsuario());
                return ResponseEntity.ok(Map.of("accessToken", novoAccessToken));
            } catch (RuntimeException e) {
                return ResponseEntity.status(401).body(Map.of("erro", e.getMessage()));
            }
        }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("refreshToken");
            RefreshToken refreshToken = refreshTokenService.validar(token);
            refreshTokenService.revogar(refreshToken.getUsuario());
            return ResponseEntity.ok(Map.of("mensagem", "Logout realizado com sucesso"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("erro", e.getMessage()));
        }
    }
}