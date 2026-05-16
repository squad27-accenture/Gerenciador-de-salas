package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.services.SalasService;
import com.squad27.gerenciadorsalas.domain.Salas;
import com.squad27.gerenciadorsalas.dto.SalasDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")

public class SalaController {

    @Autowired
    SalasService salasService;

    @PostMapping
    public ResponseEntity<Salas> cadastrarSala(@RequestBody SalasDTO salaDTO){



        Salas salaSalva = salasService.cadastrarsala(salaDTO);
        return ResponseEntity.ok(salaSalva);


    }

    @GetMapping
    public List<Salas> listarSalas(){
        return salasService.listarsalas();
    }

}
