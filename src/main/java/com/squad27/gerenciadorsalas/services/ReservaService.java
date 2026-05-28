package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.StatusReserva;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.repositories.ReservaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SalaRepository salaRepository;

    public Reserva ReservarAssento (ReservaDTO dto, String emailUsuario){
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        Reserva reserva = new Reserva();
        reserva.setHorarioInicio(dto.horarioInicio());
        reserva.setHorarioFim(dto.horarioFim());
        reserva.setDataReserva(dto.dataReserva());
        reserva.setSala(sala);
        reserva.setUsuario(usuario);
        reserva.setStatusReserva(StatusReserva.EmANDAMENTO);
        reserva.setPosicaoassento(dto.posicaoAssento());

        return reservaRepository.save(reserva);
    }
}
