package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import com.squad27.gerenciadorsalas.service.SalaService;
import com.squad27.gerenciadorsalas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/salas")
@RequiredArgsConstructor


public class SalaController {

    private final SalaService salaService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Void> cadastrarSala (@RequestBody SalaDTO salaDTO){

        Sala sala = Sala.builder()
                .numerosala(salaDTO.numerosala())
                .capacidade(salaDTO.capacidade())
                .build();

        salaService.cadastrarSala(sala);
        return ResponseEntity.ok().build();

    }

    @GetMapping
    public ResponseEntity<Sala> buscarSalaPorNumero (@RequestParam String numerosala){

        return ResponseEntity.ok(salaService.buscarSalaPorNumero(numerosala));
    }

    @DeleteMapping
    public ResponseEntity<Void>  deletarSalaPorNumero (@RequestParam String numerosala){

        salaService.deletarSalaPorNumero(numerosala);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> atualizarSala (@RequestParam Integer id , @RequestBody SalaDTO salaDTO){

        Sala sala = Sala.builder()
                .numerosala(salaDTO.numerosala())
                .capacidade(salaDTO.capacidade())
                .build();

        salaService.atualizarSalaPorId(id , sala);

        return ResponseEntity.ok().build();
    }


}
