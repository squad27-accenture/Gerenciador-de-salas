package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.dto.ConfirmarReservaOpcaoDTO;
import com.squad27.gerenciadorsalas.dto.OcupacaoResponseDTO;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.dto.ReservaResponseDTO;
import com.squad27.gerenciadorsalas.services.ReservaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    @Autowired
    private ReservaService reservaService;

    /*
     * RESERVA INDIVIDUAL MANUAL
     *
     * Fluxo:
     * - usuário escolhe sala no front
     * - usuário escolhe assento no mapa
     * - front manda salaId + posicaoAssento + data + horário
     */
    @PostMapping
    public ResponseEntity<ReservaResponseDTO> realizarReservaIndividual(
            @RequestBody ReservaDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Reserva reserva = reservaService.ReservarAssento(dto, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ReservaResponseDTO(reserva));
    }

    /*
     * CONFIRMAR OPÇÃO GERADA PELA IA
     *
     * Fluxo:
     * - POST /api/v1/ia/opcoes retorna opções
     * - usuário escolhe uma opção
     * - front manda salaId + grupoId + posicoesAssentos
     */
    @PostMapping("/confirmar-opcao")
    public ResponseEntity<List<ReservaResponseDTO>> confirmarOpcaoIa(
            @RequestBody ConfirmarReservaOpcaoDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Reserva> reservas = reservaService.confirmarOpcao(dto, userDetails.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservas.stream().map(ReservaResponseDTO::new).toList());
    }

    /*
     * CANCELAR RESERVA INDIVIDUAL
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelarReserva(
            @PathVariable Integer id,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Reserva reserva = reservaService.cancelarReserva(id, userDetails.getUsername(), motivo);

        return ResponseEntity.ok(new ReservaResponseDTO(reserva));
    }

    /*
     * CANCELAR RESERVA EM GRUPO
     */
    @PutMapping("/grupo/{codigoGrupo}/cancelar")
    public ResponseEntity<List<ReservaResponseDTO>> cancelarReservaGrupo(
            @PathVariable String codigoGrupo,
            @RequestParam(required = false) String motivo,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<Reserva> reservas = reservaService.cancelarReservaGrupo(
                codigoGrupo,
                userDetails.getUsername(),
                motivo
        );

        return ResponseEntity.ok(
                reservas.stream().map(ReservaResponseDTO::new).toList()
        );
    }

    /*
     * HISTÓRICO DE RESERVAS
     */
    @GetMapping("/historico")
    public ResponseEntity<List<ReservaResponseDTO>> historico(
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer salaId,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reservaService.buscarHistoricoDoUsuarioLogado(
                                usuarioId,
                                salaId,
                                dataInicio,
                                dataFim,
                                userDetails.getUsername()
                        )
                        .stream()
                        .map(ReservaResponseDTO::new)
                        .toList()
        );
    }

    /*
     * RELATÓRIO DE OCUPAÇÃO
     */
    @GetMapping("/ocupacao")
    public ResponseEntity<OcupacaoResponseDTO> relatorioOcupacao(
            @RequestParam Integer salaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim
    ) {
        return ResponseEntity.ok(
                reservaService.relatórioOcupacao(salaId, dataInicio, dataFim)
        );
    }
}