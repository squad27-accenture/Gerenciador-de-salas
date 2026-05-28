package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.services.SalaService;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas/")

public class SalaController {

    @Autowired
    SalaService salaService;

    @PostMapping("CadastrarSala")
    public ResponseEntity<Sala> cadastrarSala(@RequestBody SalaDTO salaDTO){

        Sala salaSalva = salaService.cadastrarsala(salaDTO);
        return ResponseEntity.ok(salaSalva);


    }

    @GetMapping("ListarSala")
    public List<Sala> listarSalas(){
        return salaService.listarsalas();
    }



    @DeleteMapping("DeletarSala")
    public ResponseEntity<String> deletarSalaPorId(@RequestParam Integer id){

        salaService.deletarSalaPorId(id);

        return ResponseEntity.ok("Sala deletada com SUCESSO!");
    }

    @PutMapping("AtualizarSala")
    public ResponseEntity<String> atualizarSalaPorId(@RequestParam Integer id , @RequestBody SalaDTO salaDTO){

        Sala sala = Sala.builder()
                .nome(salaDTO.nome())
                .capacidade(salaDTO.capacidade())
                .local(salaDTO.local())
                .status(salaDTO.statusSala())
                .build();

        salaService.atualizarSalaPorId(id , sala);

        return ResponseEntity.ok("Sala atualizada com SUCESSO!");

    }
}


