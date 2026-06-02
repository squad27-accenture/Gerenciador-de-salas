package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
import com.squad27.gerenciadorsalas.dto.SalaResponseDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import com.squad27.gerenciadorsalas.services.SalaService;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salas/")

public class SalaController {


    @Autowired
    SalaService salaService;

    @Autowired
    ReservaService reservaService;

    @PostMapping("CadastrarSala")
    public ResponseEntity<SalaResponseDTO> cadastrarSala(
            @RequestBody SalaDTO salaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Sala salaSalva = salaService.cadastrarsala(salaDTO, userDetails.getUsername());
        return ResponseEntity.ok(new SalaResponseDTO(
                salaSalva.getId(),
                salaSalva.getNome(),
                salaSalva.getCapacidade(),
                salaSalva.getLocal(),
                salaSalva.getCidade(),
                salaSalva.getEstado()
        ));
    }

    @GetMapping("ListarSala")
    public ResponseEntity<List<SalaResponseDTO>> listarSalas(){
        List<SalaResponseDTO> salas = salaService.listarSalas();
        return ResponseEntity.ok(salas);
    }

    @DeleteMapping("DeletarSala")
    public ResponseEntity<String> deletarSalaPorId(
            @RequestParam Integer id,
            @AuthenticationPrincipal UserDetails userDetails){
        salaService.deletarSalaPorId(id, userDetails.getUsername());
        return ResponseEntity.ok("Sala deletada com SUCESSO!");
    }

    @PutMapping("AtualizarSala")
    public ResponseEntity<String> atualizarSalaPorId(
            @RequestParam Integer id,
            @RequestBody SalaDTO salaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sala sala = Sala.builder()
                .nome(salaDTO.nome())
                .capacidade(salaDTO.capacidade())
                .local(salaDTO.local())
                .cidade(salaDTO.cidade())
                .estado(salaDTO.estado())
                .status(salaDTO.statusSala())
                .raioProximidade(salaDTO.raioProximidade())
                .build();

        salaService.atualizarSalaPorId(id, sala, userDetails.getUsername());
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