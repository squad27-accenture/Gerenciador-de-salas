package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import com.squad27.gerenciadorsalas.services.SalaService;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/salas/")

public class SalaController {


    @Autowired
    SalaService salaService;

    @Autowired
    ReservaService reservaService;

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

    @GetMapping("{id}/assentos")
    public ResponseEntity<List<AssentoReponseDTO>> listarAssentosDaSala(@PathVariable Integer id) {
        List<AssentoReponseDTO> assentos = salaService.listarAssentosDaSala(id);
        return ResponseEntity.ok(assentos);
    }

    @GetMapping("ocupados")
    public ResponseEntity<List<Integer>> buscarAssentosOcupados(
            @RequestParam("salaId") Integer salaId,
            @RequestParam("dataReserva") LocalDate dataReserva,
            @RequestParam("horarioInicio") LocalTime horarioInicio,
            @RequestParam("horarioFim") LocalTime horarioFim

    ) {
        List<Integer> ocupados = reservaService.buscarAssentosOcupados(
                salaId,
                dataReserva,
                horarioInicio,
                horarioFim
        );

        return ResponseEntity.ok(ocupados);

    }


}


