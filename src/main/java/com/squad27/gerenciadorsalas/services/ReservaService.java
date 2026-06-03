package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.OcupacaoResponseDTO;
import com.squad27.gerenciadorsalas.enums.CriterioProximidade;
import com.squad27.gerenciadorsalas.enums.Role;
import com.squad27.gerenciadorsalas.enums.StatusReserva;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.dto.ReservaGrupoDTO;
import com.squad27.gerenciadorsalas.enums.TipoAssento;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.ReservaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final AssentoRepository assentoRepository;
    private final NotificacaoEmailService notificacaoEmailService;
    private final DisponibilidadeService disponibilidadeService;
    private final AlocacaoService alocacaoService;
    private final AuditoriaService auditoriaService;
    private final TipoAssentoService tipoAssentoService;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            SalaRepository salaRepository,
            AssentoRepository assentoRepository, NotificacaoEmailService notificacaoEmailService, DisponibilidadeService disponibilidadeService, AlocacaoService alocacaoService, AuditoriaService auditoriaService, TipoAssentoService tipoAssentoService
    ) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.assentoRepository = assentoRepository;
        this.notificacaoEmailService = notificacaoEmailService;
        this.disponibilidadeService = disponibilidadeService;
        this.alocacaoService = alocacaoService;
        this.auditoriaService = auditoriaService;
        this.tipoAssentoService = tipoAssentoService;
    }

    public Reserva ReservarAssento(ReservaDTO dto, String emailUsuario) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());
        disponibilidadeService.validarDisponibilidade(
                dto.salaId(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim()
        );

        List<Integer> posicoesOcupadas = reservaRepository.buscarPosicoesOcupadas(
                dto.salaId(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim(), StatusReserva.CANCELADA
        );
        List<Assento> assentosLivres = assentoRepository.findBySalaIdOrderByPosicao(dto.salaId())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .filter(a -> !posicoesOcupadas.contains(a.getPosicao()))
                .toList();

        // Monta lista de tipos preferidos (pessoa única)
        List<List<String>> tiposPorPessoa = new ArrayList<>();
        tiposPorPessoa.add(dto.tiposPreferidosPessoa1() != null ? dto.tiposPreferidosPessoa1() : List.of());
        validarTiposAssento(tiposPorPessoa);

        List<Assento> alocados;
        try {
            alocados = alocacaoService.alocar(
                    assentosLivres, tiposPorPessoa,
                    dto.criterioProximidade() != null ? dto.criterioProximidade() : CriterioProximidade.NENHUM,
                    sala.getRaioProximidade()
            );
        } catch (ResponseStatusException ex) {
            notificacaoEmailService.enviarRejeicaoReserva(
                    usuario.getEmail(), usuario.getUsername(),
                    sala.getNome(), dto.dataReserva().toString(),
                    dto.horarioInicio().toString(), dto.horarioFim().toString(),
                    ex.getReason()
            );
            throw ex;
        }

        Assento assentoEscolhido = alocados.get(0);
        validarConflito(dto.salaId(), assentoEscolhido.getPosicao(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim());

        Reserva reserva = new Reserva();
        reserva.setHorarioInicio(dto.horarioInicio());
        reserva.setHorarioFim(dto.horarioFim());
        reserva.setDataReserva(dto.dataReserva());
        reserva.setSala(sala);
        reserva.setUsuario(usuario);
        reserva.setStatusReserva(StatusReserva.CONFIRMADA);
        reserva.setPosicaoAssento(assentoEscolhido.getPosicao());

        Reserva salva = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "CRIACAO", "RESERVA", String.valueOf(salva.getId()),
                emailUsuario,
                "Reserva criada para sala " + sala.getNome() +
                        " em " + dto.dataReserva() +
                        " das " + dto.horarioInicio() + " às " + dto.horarioFim() +
                        " — assento " + assentoEscolhido.getPosicao()
        );

        notificacaoEmailService.enviarConfirmacaoReserva(
                usuario.getEmail(), usuario.getUsername(),
                sala.getNome(), dto.dataReserva().toString(),
                dto.horarioInicio().toString(), dto.horarioFim().toString(),
                assentoEscolhido.getPosicao()
        );

        return salva;
    }

    public List<Reserva> reservaGrupo(ReservaGrupoDTO dto, String emailUsuario) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());
        disponibilidadeService.validarDisponibilidade(
                dto.salaId(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim()
        );

        // Busca assentos livres
        List<Integer> posicoesOcupadas = reservaRepository.buscarPosicoesOcupadas(
                dto.salaId(), dto.dataReserva(),
                dto.horarioInicio(), dto.horarioFim(), StatusReserva.CANCELADA
        );
        List<Assento> assentosLivres = assentoRepository.findBySalaIdOrderByPosicao(dto.salaId())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .filter(a -> !posicoesOcupadas.contains(a.getPosicao()))
                .toList();

        List<List<String>> tiposPorPessoa = dto.tiposPreferidosPorPessoa() != null
                ? dto.tiposPreferidosPorPessoa()
                : List.of();
        validarTiposAssento(tiposPorPessoa);

        List<Assento> alocados;
        try {
            alocados = alocacaoService.alocar(
                    assentosLivres, tiposPorPessoa,
                    dto.criterioProximidade() != null ? dto.criterioProximidade() : CriterioProximidade.NENHUM,
                    sala.getRaioProximidade()
            );
        } catch (ResponseStatusException ex) {
            notificacaoEmailService.enviarRejeicaoReserva(
                    usuario.getEmail(), usuario.getUsername(),
                    sala.getNome(), dto.dataReserva().toString(),
                    dto.horarioInicio().toString(), dto.horarioFim().toString(),
                    ex.getReason()
            );
            throw ex;
        }

        String codigoGrupo = UUID.randomUUID().toString();
        List<Reserva> reservas = new ArrayList<>();

        for (Assento assento : alocados) {
            validarConflito(dto.salaId(), assento.getPosicao(), dto.dataReserva(),
                    dto.horarioInicio(), dto.horarioFim());

            Reserva reserva = new Reserva();
            reserva.setHorarioInicio(dto.horarioInicio());
            reserva.setHorarioFim(dto.horarioFim());
            reserva.setDataReserva(dto.dataReserva());
            reserva.setSala(sala);
            reserva.setUsuario(usuario);
            reserva.setStatusReserva(StatusReserva.CONFIRMADA);
            reserva.setPosicaoAssento(assento.getPosicao());
            reserva.setCodigoGrupo(codigoGrupo);
            reservas.add(reserva);
        }

        List<Reserva> salvas = reservaRepository.saveAll(reservas);
        auditoriaService.registrar(
                "CRIACAO", "RESERVA_GRUPO", codigoGrupo,
                emailUsuario,
                "Reserva em grupo criada para sala " + sala.getNome() +
                        " em " + dto.dataReserva() +
                        " das " + dto.horarioInicio() + " às " + dto.horarioFim() +
                        " — assentos " + alocados.stream().map(a -> String.valueOf(a.getPosicao())).toList()
        );

        notificacaoEmailService.enviarConfirmacaoReservaGrupo(
                usuario.getEmail(), usuario.getUsername(),
                sala.getNome(), dto.dataReserva().toString(),
                dto.horarioInicio().toString(), dto.horarioFim().toString(),
                alocados.stream().map(Assento::getPosicao).toList()
        );

        return salvas;
    }

    public Reserva cancelarReserva(Integer reservaId, String emailUsuario, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        Usuario solicitante = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        boolean isDono = reserva.getUsuario().getEmail().equals(emailUsuario);
        boolean isGestor = solicitante.getRole() == Role.ADMIN || solicitante.getRole() == Role.TECHLEADER;

        if (!isDono && !isGestor) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para cancelar esta reserva.");
        }

        reserva.setStatusReserva(StatusReserva.CANCELADA);
        reserva.setMotivoCancelamento(motivo);

        Reserva salva = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "CANCELAMENTO", "RESERVA", String.valueOf(salva.getId()),
                emailUsuario,
                "Reserva cancelada. Motivo: " + (motivo != null ? motivo : "não informado")
        );

        notificacaoEmailService.enviarCancelamentoReserva(
                reserva.getUsuario().getEmail(),
                reserva.getUsuario().getUsername(),
                reserva.getSala().getNome(),
                reserva.getDataReserva().toString(),
                reserva.getHorarioInicio().toString(),
                reserva.getHorarioFim().toString(),
                reserva.getPosicaoAssento()
        );

        return salva;
    }

    public List<Reserva> cancelarReservaGrupo(String codigoGrupo, String emailUsuario, String motivo) {
        List<Reserva> reservas = reservaRepository.findByCodigoGrupo(codigoGrupo);

        if (reservas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva em grupo não encontrada.");
        }

        Usuario solicitante = usuarioRepository.findByEmail(emailUsuario).orElseThrow();
        boolean isGestor = solicitante.getRole() == Role.ADMIN || solicitante.getRole() == Role.TECHLEADER;

        for (Reserva reserva : reservas) {
            boolean isDono = reserva.getUsuario().getEmail().equals(emailUsuario);
            if (!isDono && !isGestor) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para cancelar esta reserva.");
            }
            reserva.setStatusReserva(StatusReserva.CANCELADA);
            reserva.setMotivoCancelamento(motivo);
        }

        List<Reserva> salvas = reservaRepository.saveAll(reservas);

        auditoriaService.registrar(
                "CANCELAMENTO", "RESERVA_GRUPO", codigoGrupo,
                emailUsuario,
                "Reserva em grupo cancelada. Motivo: " + (motivo != null ? motivo : "não informado")
        );

        Reserva primeira = salvas.get(0);
        List<Integer> posicoes = salvas.stream().map(Reserva::getPosicaoAssento).toList();

        notificacaoEmailService.enviarCancelamentoReservaGrupo(
                primeira.getUsuario().getEmail(),
                primeira.getUsuario().getUsername(),
                primeira.getSala().getNome(),
                primeira.getDataReserva().toString(),
                primeira.getHorarioInicio().toString(),
                primeira.getHorarioFim().toString(),
                posicoes
        );

        return salvas;
    }

    public List<Integer> buscarAssentosOcupados(
            Integer salaId, LocalDate dataReserva,
            LocalTime horarioInicio, LocalTime horarioFim
    ) {
        return reservaRepository.buscarPosicoesOcupadas(
                salaId, dataReserva, horarioInicio, horarioFim, StatusReserva.CANCELADA
        );
    }



    private void validarHorarios(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Horário de início e fim são obrigatórios.");
        }
        if (!inicio.isBefore(fim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O horário inicial precisa ser antes do horário final.");
        }
    }

    private void validarDataReserva(LocalDate dataReserva) {
        if (dataReserva == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A data da reserva é obrigatória.");
        }
        if (dataReserva.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Não é possível realizar reservas para datas passadas.");
        }
    }

    private void validarConflito(Integer salaId, Integer posicao, LocalDate data,
                                 LocalTime inicio, LocalTime fim) {
        boolean ocupado = reservaRepository.existeConflitoDeHorario(
                salaId, posicao, data, inicio, fim, StatusReserva.CANCELADA);
        if (ocupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Assento " + posicao + " já está reservado nesse horário.");
        }
    }

    public List<Reserva> buscarHistorico(Integer usuarioId, Integer salaId,
                                         LocalDate dataInicio, LocalDate dataFim) {
        log.info("Buscando histórico: usuarioId={} salaId={} dataInicio={} dataFim={}",
                usuarioId, salaId, dataInicio, dataFim);
        return reservaRepository.buscarHistorico(usuarioId, salaId, dataInicio, dataFim);
    }

    public OcupacaoResponseDTO relatórioOcupacao(Integer salaId, LocalDate dataInicio, LocalDate dataFim) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        long totalReservas = reservaRepository.contarReservasPorSalaEPeriodo(
                salaId, dataInicio, dataFim, StatusReserva.CANCELADA
        );

        int totalAssentos = sala.getAssentos().size();
        long diasNoPeriodo = dataInicio.datesUntil(dataFim.plusDays(1)).count();
        long capacidadeTotal = (long) totalAssentos * diasNoPeriodo;

        double taxa = capacidadeTotal == 0 ? 0.0
                : Math.round(((double) totalReservas / capacidadeTotal) * 10000.0) / 100.0;

        return new OcupacaoResponseDTO(
                salaId,
                sala.getNome(),
                dataInicio,
                dataFim,
                (int) totalReservas,
                totalAssentos,
                taxa
        );
    }
    private void validarTiposAssento(List<List<String>> tiposPorPessoa) {
        for (int i = 0; i < tiposPorPessoa.size(); i++) {
            List<String> tipos = tiposPorPessoa.get(i);
            if (tipos == null) continue;
            for (String tipo : tipos) {
                if (tipo != null && !tipo.isBlank()) {
                    tipoAssentoService.validarNome(tipo); // lança 400 se inválido/inativo
                }
            }
        }
    }
}