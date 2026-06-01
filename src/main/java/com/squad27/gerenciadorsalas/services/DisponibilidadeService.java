package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.*;
import com.squad27.gerenciadorsalas.dto.DataBloqueadaDTO;
import com.squad27.gerenciadorsalas.dto.DataBloqueadaResponseDTO;
import com.squad27.gerenciadorsalas.dto.DisponibilidadeDTO;
import com.squad27.gerenciadorsalas.dto.DisponibilidadeResponseDTO;
import com.squad27.gerenciadorsalas.enums.DiaSemana;
import com.squad27.gerenciadorsalas.repositories.DataBloqueadaRepository;
import com.squad27.gerenciadorsalas.repositories.DisponibilidadeSalaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeSalaRepository disponibilidadeRepository;
    private final DataBloqueadaRepository dataBloqueadaRepository;
    private final SalaRepository salaRepository;


    public DisponibilidadeService(DisponibilidadeSalaRepository disponibilidadeRepository,
                                  DataBloqueadaRepository dataBloqueadaRepository,
                                  SalaRepository salaRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.dataBloqueadaRepository = dataBloqueadaRepository;
        this.salaRepository = salaRepository;
    }

    public List<DisponibilidadeResponseDTO> configurarDisponibilidade(Integer salaId, DisponibilidadeDTO dto) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        List<DiaSemana> dias = dto.diasSemana() != null && !dto.diasSemana().isEmpty()
                ? dto.diasSemana()
                : List.of(DiaSemana.values()); // se não informar, configura todos os dias

        return dias.stream().map(dia -> {
            DisponibilidadeSala disponibilidade = disponibilidadeRepository
                    .findBySalaIdAndDiaSemana(salaId, dia)
                    .orElse(new DisponibilidadeSala());

            disponibilidade.setSala(sala);
            disponibilidade.setDiaSemana(dia);
            disponibilidade.setAceitaReservas(dto.aceitaReservas());
            disponibilidade.setHorarioAbertura(dto.horarioAbertura());
            disponibilidade.setHorarioFechamento(dto.horarioFechamento());
            disponibilidade.setAntecedenciaMinimaDias(dto.antecedenciaMinimaDias());

            return new DisponibilidadeResponseDTO(disponibilidadeRepository.save(disponibilidade));
        }).toList();
    }

    public List<DisponibilidadeResponseDTO> listarDisponibilidade(Integer salaId) {
        return disponibilidadeRepository.findBySalaId(salaId)
                .stream()
                .map(DisponibilidadeResponseDTO::new)
                .toList();
    }

    public DataBloqueadaResponseDTO bloquearData(Integer salaId, DataBloqueadaDTO dto) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada"));

        if (dataBloqueadaRepository.findBySalaIdAndData(salaId, dto.data()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa data já está bloqueada para essa sala");
        }

        DataBloqueada dataBloqueada = new DataBloqueada();
        dataBloqueada.setSala(sala);
        dataBloqueada.setData(dto.data());
        dataBloqueada.setMotivo(dto.motivo());

        return new DataBloqueadaResponseDTO(dataBloqueadaRepository.save(dataBloqueada));
    }

    public List<DataBloqueadaResponseDTO> listarDatasBloqueadas(Integer salaId) {
        return dataBloqueadaRepository.findBySalaId(salaId)
                .stream()
                .map(DataBloqueadaResponseDTO::new)
                .toList();
    }

    public void desbloquearData(Integer salaId, LocalDate data) {
        DataBloqueada dataBloqueada = dataBloqueadaRepository.findBySalaIdAndData(salaId, data)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Data bloqueada não encontrada"));
        dataBloqueadaRepository.delete(dataBloqueada);
    }

    public void validarDisponibilidade(Integer salaId, LocalDate dataReserva,
                                       LocalTime horarioInicio, LocalTime horarioFim) {
        DiaSemana diaSemana = converterDiaSemana(dataReserva);

        DisponibilidadeSala disponibilidade = disponibilidadeRepository
                .findBySalaIdAndDiaSemana(salaId, diaSemana)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A sala não possui configuração de disponibilidade para " + diaSemana));

        if (!disponibilidade.getAceitaReservas()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A sala não aceita reservas no dia: " + diaSemana);
        }

        dataBloqueadaRepository.findBySalaIdAndData(salaId, dataReserva).ifPresent(d -> {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A sala está bloqueada nessa data. Motivo: " + d.getMotivo());
        });

        long diasDeAntecedencia = LocalDate.now().until(dataReserva).getDays();
        if (diasDeAntecedencia < disponibilidade.getAntecedenciaMinimaDias()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A reserva deve ser feita com pelo menos " +
                            disponibilidade.getAntecedenciaMinimaDias() + " dia(s) de antecedência");
        }

        if (disponibilidade.getHorarioAbertura() != null && disponibilidade.getHorarioFechamento() != null) {
            if (horarioInicio.isBefore(disponibilidade.getHorarioAbertura()) ||
                    horarioFim.isAfter(disponibilidade.getHorarioFechamento())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "A reserva deve estar dentro do horário de funcionamento: " +
                                disponibilidade.getHorarioAbertura() + " às " +
                                disponibilidade.getHorarioFechamento());
            }
        }
    }

    private DiaSemana converterDiaSemana(LocalDate data) {
        return switch (data.getDayOfWeek()) {
            case MONDAY -> DiaSemana.SEGUNDA;
            case TUESDAY -> DiaSemana.TERCA;
            case WEDNESDAY -> DiaSemana.QUARTA;
            case THURSDAY -> DiaSemana.QUINTA;
            case FRIDAY -> DiaSemana.SEXTA;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}