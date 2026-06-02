package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.*;
import java.time.temporal.ChronoUnit;
import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.enums.DiaSemana;
import com.squad27.gerenciadorsalas.enums.StatusReserva;
import com.squad27.gerenciadorsalas.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadeService {

    private final DisponibilidadeSalaRepository disponibilidadeRepository;
    private final DataBloqueadaRepository dataBloqueadaRepository;
    private final SalaRepository salaRepository;
    private final ReservaRepository reservaRepository;
    private final AssentoRepository assentoRepository;


    public DisponibilidadeService(DisponibilidadeSalaRepository disponibilidadeRepository,
                                  DataBloqueadaRepository dataBloqueadaRepository,
                                  SalaRepository salaRepository, ReservaRepository reservaRepository, AssentoRepository assentoRepository) {
        this.disponibilidadeRepository = disponibilidadeRepository;
        this.dataBloqueadaRepository = dataBloqueadaRepository;
        this.salaRepository = salaRepository;
        this.reservaRepository = reservaRepository;
        this.assentoRepository = assentoRepository;
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

        long diasDeAntecedencia = ChronoUnit.DAYS.between(LocalDate.now(), dataReserva);
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
    public List<DisponibilidadePeriodoResponseDTO> consultarDisponibilidadePorPeriodo(
            Integer salaId, LocalDate dataInicio, LocalDate dataFim,
            LocalTime horarioInicio, LocalTime horarioFim) {

        if (dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de início deve ser anterior à data fim.");
        }
        if (dataFim.isAfter(dataInicio.plusMonths(3))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O período máximo de consulta é de 3 meses.");
        }

        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        List<Assento> todosAssentos = sala.getAssentos();
        int totalAssentos = todosAssentos.size();
        int inativos = (int) todosAssentos.stream().filter(a -> !Boolean.TRUE.equals(a.getAtivo())).count();

        List<DisponibilidadePeriodoResponseDTO> resultado = new ArrayList<>();
        LocalDate data = dataInicio;

        while (!data.isAfter(dataFim)) {
            DiaSemana diaSemana = converterDiaSemana(data);
            final LocalDate dataAtual = data;

            // verifica se há configuração para o dia
            var disponibilidadeOpt = disponibilidadeRepository.findBySalaIdAndDiaSemana(salaId, diaSemana);
            if (disponibilidadeOpt.isEmpty() || !disponibilidadeOpt.get().getAceitaReservas()) {
                resultado.add(new DisponibilidadePeriodoResponseDTO(
                        dataAtual, diaSemana.name(), "INDISPONIVEL",
                        "Sala não aceita reservas neste dia.", totalAssentos, 0, 0, inativos));
                data = data.plusDays(1);
                continue;
            }

            // verifica data bloqueada
            var bloqueio = dataBloqueadaRepository.findBySalaIdAndData(salaId, dataAtual);
            if (bloqueio.isPresent()) {
                resultado.add(new DisponibilidadePeriodoResponseDTO(
                        dataAtual, diaSemana.name(), "BLOQUEADA",
                        bloqueio.get().getMotivo(), totalAssentos, 0, 0, inativos));
                data = data.plusDays(1);
                continue;
            }

            // conta ocupados no horário
            List<Integer> ocupadas = reservaRepository.buscarPosicoesOcupadas(
                    salaId, dataAtual, horarioInicio, horarioFim, StatusReserva.CANCELADA);
            int ocupados = ocupadas.size();
            int ativos = totalAssentos - inativos;
            int livres = Math.max(0, ativos - ocupados);

            resultado.add(new DisponibilidadePeriodoResponseDTO(
                    dataAtual, diaSemana.name(), "DISPONIVEL",
                    null, totalAssentos, livres, ocupados, inativos));

            data = data.plusDays(1);
        }

        return resultado;
    }
    public List<AssentoStatusDTO> consultarStatusAssentos(
            Integer salaId, LocalDate data,
            LocalTime horarioInicio, LocalTime horarioFim) {

        salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        List<Integer> ocupadas = reservaRepository.buscarPosicoesOcupadas(
                salaId, data, horarioInicio, horarioFim, StatusReserva.CANCELADA);

        return assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .map(a -> {
                    String status;
                    if (!Boolean.TRUE.equals(a.getAtivo())) {
                        status = "INATIVO";
                    } else if (ocupadas.contains(a.getPosicao())) {
                        status = "OCUPADO";
                    } else {
                        status = "LIVRE";
                    }

                    return new AssentoStatusDTO(
                            a.getId(),
                            a.getPosicao(),
                            a.getTipoAssento() == null ? null : a.getTipoAssento().name(),
                            a.getCoordenadaX(),
                            a.getCoordenadaY(),
                            status,
                            a.getEquipamentos().stream().map(Enum::name).toList()
                    );
                })
                .toList();
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