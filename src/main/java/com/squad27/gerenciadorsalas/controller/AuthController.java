package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.services.AuthorizationService;
import com.squad27.gerenciadorsalas.domain.Usuarios;
import com.squad27.gerenciadorsalas.dto.AuthorizationDTO;
import com.squad27.gerenciadorsalas.dto.LoginResponse;
import com.squad27.gerenciadorsalas.dto.RegisterDTO;
import com.squad27.gerenciadorsalas.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthorizationService authorizationService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody AuthorizationDTO data){

        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = tokenService.generateToken((Usuarios) auth.getPrincipal());

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {

            return ResponseEntity.status(401).body(Map.of("Erro" , "Email ou senha incorretos."));
        }catch (org.springframework.security.core.userdetails.UsernameNotFoundException e){

            return ResponseEntity.status(401).body(Map.of("Erro" , "Usuario não encontrado"));
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<Usuarios> cadastro(@RequestBody RegisterDTO registerDTO){
       Usuarios newusuario = authorizationService.cadastro(registerDTO);
       return ResponseEntity.ok().build();
    }
}
