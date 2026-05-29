package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.StatusReserva;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.dto.ReservaGrupoDTO;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.ReservaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final AssentoRepository assentoRepository;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            SalaRepository salaRepository,
            AssentoRepository assentoRepository
    ) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.assentoRepository = assentoRepository;
    }

    public Reserva ReservarAssento (ReservaDTO dto, String emailUsuario){
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (dto.horarioInicio() == null || dto.horarioFim() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Horário de início e fim são obrigatórios."
            );
        }

        if (!dto.horarioInicio().isBefore(dto.horarioFim())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário inicial precisa ser antes do horário final."
            );
        }

        assentoRepository.findBySalaIdAndPosicao(dto.salaId(), dto.posicaoAssento())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assento não encontrado nessa sala."
                ));

        boolean ocupado = reservaRepository.existeConflitoDeHorario(
                dto.salaId(),
                dto.posicaoAssento(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim(),
                StatusReserva.CANCELADA
        );

        if (ocupado){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esse assento já está reservado nesse horário."
            );
        }

        Reserva reserva = new Reserva();
        reserva.setHorarioInicio(dto.horarioInicio());
        reserva.setHorarioFim(dto.horarioFim());
        reserva.setDataReserva(dto.dataReserva());
        reserva.setSala(sala);
        reserva.setUsuario(usuario);
        reserva.setStatusReserva(StatusReserva.EmANDAMENTO);
        reserva.setPosicaoAssento(dto.posicaoAssento());

        return reservaRepository.save(reserva);
    }

    public List<Reserva> reservaGrupo(ReservaGrupoDTO dto, String emailUsuario) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        String codigoGrupo = UUID.randomUUID().toString();

        List<Reserva> reservas = new ArrayList<>();

        for (Integer posicao : dto.posicoesAssentos()) {
            Reserva reserva = new Reserva();

            reserva.setHorarioInicio(dto.horarioInicio());
            reserva.setHorarioFim(dto.horarioFim());
            reserva.setDataReserva(dto.dataReserva());
            reserva.setSala(sala);
            reserva.setUsuario(usuario);
            reserva.setStatusReserva(StatusReserva.EmANDAMENTO);
            reserva.setPosicaoAssento(posicao);
            reserva.setCodigoGrupo(codigoGrupo);

            reservas.add(reserva);
        }

        return reservaRepository.saveAll(reservas);
    }

    public Reserva cancelarReserva(Integer reservaId, String emailUsuario) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        if (!reserva.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("Você não pode cancelar uma reserva de outro usuário");
        }

        reserva.setStatusReserva(StatusReserva.CANCELADA);

        return reservaRepository.save(reserva);
    }

    public List<Reserva> cancelarReservaGrupo(String codigoGrupo, String emailUsuario) {
        List<Reserva> reservas = reservaRepository.findByCodigoGrupo(codigoGrupo);

        if (reservas.isEmpty()) {
            throw new RuntimeException("Reserva em grupo não encontrada");
        }

        for (Reserva reserva : reservas) {
            if (!reserva.getUsuario().getEmail().equals(emailUsuario)) {
                throw new RuntimeException("Você não pode cancelar uma reserva de outro usuário");
            }

            reserva.setStatusReserva(StatusReserva.CANCELADA);
        }

        return reservaRepository.saveAll(reservas);
    }

    public List<Integer> buscarAssentosOcupados(
            Integer salaId,
            LocalDate dataReserva,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        return reservaRepository.buscarPosicoesOcupadas(
                salaId,
                dataReserva,
                horarioInicio,
                horarioFim,
                StatusReserva.CANCELADA
        );
    }
}
