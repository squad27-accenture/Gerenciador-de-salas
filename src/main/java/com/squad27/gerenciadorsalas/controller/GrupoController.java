package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.dto.ConviteGrupoRequestDTO;
import com.squad27.gerenciadorsalas.dto.GrupoRequestDTO;
import com.squad27.gerenciadorsalas.services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/grupos")
public class GrupoController {

    private final GrupoService grupoService;

    public GrupoController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody GrupoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.criar(dto, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.listar(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.buscarPorId(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editar(
            @PathVariable Integer id,
            @RequestBody GrupoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.editar(id, dto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        grupoService.deletar(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/convites")
    public ResponseEntity<?> convidar(
            @PathVariable Integer id,
            @RequestBody ConviteGrupoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grupoService.convidar(id, dto, userDetails.getUsername()));
    }

    @GetMapping("/convites/me")
    public ResponseEntity<?> meusConvites(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.meusConvites(userDetails.getUsername()));
    }

    @PostMapping("/convites/{id}/aceitar")
    public ResponseEntity<?> aceitarConvite(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.aceitarConvite(id, userDetails.getUsername()));
    }

    @PostMapping("/convites/{id}/recusar")
    public ResponseEntity<?> recusarConvite(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.recusarConvite(id, userDetails.getUsername()));
    }
}