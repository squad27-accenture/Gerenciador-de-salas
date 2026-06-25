package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.services.ReservaService;
import com.squad27.gerenciadorsalas.services.SalaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/salas")

public class SalaController {


    @Autowired
    SalaService salaService;

    @Autowired
    ReservaService reservaService;

    @PostMapping
    public ResponseEntity<SalaResponseDTO> cadastrarSala(
            @RequestBody SalaDTO salaDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Sala salaSalva = salaService.cadastrarsala(salaDTO, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(new SalaResponseDTO(
                salaSalva.getId(),
                salaSalva.getNome(),
                salaSalva.getCapacidade(),
                salaSalva.getLocal(),
                salaSalva.getCidade(),
                salaSalva.getEstado(),
                salaSalva.getAndar(),
                salaSalva.getBloco()
        ));
    }

    @GetMapping
    public ResponseEntity<List<SalaResponseDTO>> listarSalas(){
        List<SalaResponseDTO> salas = salaService.listarSalas();
        return ResponseEntity.ok(salas);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSalaPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails){
        salaService.deletarSalaPorId(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<SalaResponseDTO> buscarSalaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(salaService.buscarSalaPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarSalaPorId(
            @PathVariable Integer id,
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

    @PostMapping("{id}/layout/upload")
    public ResponseEntity<SalaResponseDTO> uploadLayout(
            @PathVariable Integer id,
            @RequestParam("imagem") MultipartFile imagem,
            @AuthenticationPrincipal UserDetails userDetails) {

        SalaResponseDTO response = salaService.uploadLayout(id, imagem, userDetails.getUsername());
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("{id}/layout-preview")
    public ResponseEntity<LayoutPreviewDTO> layoutPreview(@PathVariable Integer id) {
        return ResponseEntity.ok(salaService.layoutPreview(id));
    }

    @PutMapping("{id}/layout")
    public ResponseEntity<SalaResponseDTO> aprovarLayout(
            @PathVariable Integer id,
            @RequestBody AprovarLayoutDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        SalaResponseDTO response = salaService.aprovarLayout(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("{id}/layout/resultado")
    public ResponseEntity<Void> receberResultadoAgente(
            @PathVariable Integer id,
            @RequestBody AgentLayoutResultDTO dto) {

        salaService.receberResultadoAgente(dto);
        return ResponseEntity.noContent().build();
    }


}