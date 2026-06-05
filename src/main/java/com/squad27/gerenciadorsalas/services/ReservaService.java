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
import com.squad27.gerenciadorsalas.dto.AssentoVisualDTO;
import com.squad27.gerenciadorsalas.dto.OpcaoReservaDTO;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.ReservaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.squad27.gerenciadorsalas.dto.AssentoOpcaoDTO;
import com.squad27.gerenciadorsalas.dto.ConfirmarReservaOpcaoDTO;
import com.squad27.gerenciadorsalas.dto.OpcoesReservaResponseDTO;
import com.squad27.gerenciadorsalas.dto.OpcaoReservaDTO;
import com.squad27.gerenciadorsalas.dto.OpcoesReservaResponseDTO;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
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
        tiposPorPessoa.add(normalizarTiposPessoa(dto.tiposPreferidosPessoa1(), 1));
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

        List<List<String>> tiposPorPessoa = montarTiposPorPessoa(
                dto.quantidadePessoas(),
                dto.tiposPreferidosPorPessoa()
        );
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

    private List<AssentoVisualDTO> montarAssentosDaSalaParaVisualizacao(
            List<Assento> todosAssentos,
            List<Assento> assentosLivres,
            List<Assento> assentosSugeridos
    ) {
        List<Integer> posicoesLivres = assentosLivres.stream()
                .map(Assento::getPosicao)
                .toList();

        List<Integer> posicoesSugeridas = assentosSugeridos.stream()
                .map(Assento::getPosicao)
                .toList();

        return todosAssentos.stream()
                .map(assento -> {
                    boolean sugerido = posicoesSugeridas.contains(assento.getPosicao());

                    String status;

                    if (sugerido) {
                        status = "SUGERIDO";
                    } else if (!Boolean.TRUE.equals(assento.getAtivo())) {
                        status = "INATIVO";
                    } else if (posicoesLivres.contains(assento.getPosicao())) {
                        status = "LIVRE";
                    } else {
                        status = "OCUPADO";
                    }

                    return toAssentoVisualDTO(assento, status, sugerido);
                })
                .toList();
    }

    private AssentoVisualDTO toAssentoVisualDTO(
            Assento assento,
            String status,
            Boolean sugerido
    ) {
        return new AssentoVisualDTO(
                assento.getId(),
                assento.getPosicao(),
                assento.getTipoAssento(),
                status,
                sugerido,
                assento.getTipoCadeira(),
                assento.getTipoMesa(),
                assento.getEquipamentos() != null
                        ? assento.getEquipamentos().stream().map(Enum::name).toList()
                        : List.of()
        );
    }

    private String montarLocalizacaoSala(Sala sala) {
        List<String> partes = new ArrayList<>();

        if (sala.getLocal() != null && !sala.getLocal().isBlank()) {
            partes.add(sala.getLocal());
        }

        if (sala.getBloco() != null && !sala.getBloco().isBlank()) {
            partes.add("Bloco " + sala.getBloco());
        }

        if (sala.getAndar() != null && !sala.getAndar().isBlank()) {
            partes.add("Andar " + sala.getAndar());
        }

        if (sala.getCidade() != null && !sala.getCidade().isBlank()) {
            String cidadeEstado = sala.getCidade();

            if (sala.getEstado() != null && !sala.getEstado().isBlank()) {
                cidadeEstado += "/" + sala.getEstado();
            }

            partes.add(cidadeEstado);
        }

        return partes.isEmpty()
                ? "Localização não informada"
                : String.join(", ", partes);
    }

    private String montarObservacaoOpcao(CriterioProximidade criterio) {
        if (criterio == CriterioProximidade.OBRIGATORIA) {
            return "Opção respeitando proximidade obrigatória.";
        }

        if (criterio == CriterioProximidade.PREFERENCIAL) {
            return "Opção priorizando os assentos mais próximos disponíveis.";
        }

        return "Opção com assentos livres compatíveis com o pedido.";
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

    public OpcoesReservaResponseDTO gerarOpcoesIndividual(ReservaDTO dto, String emailUsuario) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());

        disponibilidadeService.validarDisponibilidade(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<Assento> assentosLivres = buscarAssentosLivres(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<List<String>> tiposPorPessoa = new ArrayList<>();
        tiposPorPessoa.add(normalizarTiposPessoa(dto.tiposPreferidosPessoa1(), 1));
        validarTiposAssento(tiposPorPessoa);

        List<OpcaoReservaDTO> opcoes = montarOpcoes(
                sala,
                assentosLivres,
                tiposPorPessoa,
                dto.criterioProximidade(),
                3
        );

        if (opcoes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não encontrei opções disponíveis para essa reserva."
            );
        }

        return new OpcoesReservaResponseDTO(
                "Encontrei estas opções. Escolha uma para confirmar a reserva.",
                dto,
                opcoes
        );
    }

    public OpcoesReservaResponseDTO gerarOpcoesGrupo(ReservaGrupoDTO dto, String emailUsuario) {
        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());

        disponibilidadeService.validarDisponibilidade(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<Assento> assentosLivres = buscarAssentosLivres(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<List<String>> tiposPorPessoa = montarTiposPorPessoa(
                dto.quantidadePessoas(),
                dto.tiposPreferidosPorPessoa()
        );

        validarTiposAssento(tiposPorPessoa);

        List<OpcaoReservaDTO> opcoes = montarOpcoes(
                sala,
                assentosLivres,
                tiposPorPessoa,
                dto.criterioProximidade(),
                3
        );

        if (opcoes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não encontrei opções disponíveis para essa reserva em grupo."
            );
        }

        return new OpcoesReservaResponseDTO(
                "Encontrei estas opções. Escolha uma para confirmar a reserva.",
                dto,
                opcoes
        );
    }

    public List<Reserva> confirmarOpcao(ConfirmarReservaOpcaoDTO dto, String emailUsuario) {
        if (dto.posicoesAssentos() == null || dto.posicoesAssentos().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe ao menos uma posição para confirmar a reserva."
            );
        }

        if (dto.posicoesAssentos().stream().anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A lista de posições não pode conter valores nulos."
            );
        }

        if (dto.posicoesAssentos().stream().distinct().count() != dto.posicoesAssentos().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A lista de posições não pode conter assentos repetidos."
            );
        }

        Sala sala = salaRepository.findById(dto.salaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());

        disponibilidadeService.validarDisponibilidade(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<Assento> assentosDaSala = assentoRepository.findBySalaIdOrderByPosicao(dto.salaId());

        String codigoGrupo = dto.posicoesAssentos().size() > 1
                ? UUID.randomUUID().toString()
                : null;

        List<Reserva> reservas = new ArrayList<>();

        for (Integer posicao : dto.posicoesAssentos()) {
            Assento assento = assentosDaSala.stream()
                    .filter(a -> Objects.equals(a.getPosicao(), posicao))
                    .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Assento " + posicao + " não encontrado ou inativo nesta sala."
                    ));

            validarConflito(
                    dto.salaId(),
                    assento.getPosicao(),
                    dto.dataReserva(),
                    dto.horarioInicio(),
                    dto.horarioFim()
            );

            Reserva reserva = new Reserva();
            reserva.setHorarioInicio(dto.horarioInicio());
            reserva.setHorarioFim(dto.horarioFim());
            reserva.setDataReserva(dto.dataReserva());
            reserva.setSala(sala);
            reserva.setUsuario(usuario);
            reserva.setStatusReserva(StatusReserva.CONFIRMADA);
            reserva.setPosicaoAssento(assento.getPosicao());

            if (codigoGrupo != null) {
                reserva.setCodigoGrupo(codigoGrupo);
            }

            reservas.add(reserva);
        }

        List<Reserva> salvas = reservaRepository.saveAll(reservas);

        auditoriaService.registrar(
                "CRIACAO",
                codigoGrupo == null ? "RESERVA" : "RESERVA_GRUPO",
                codigoGrupo == null ? String.valueOf(salvas.get(0).getId()) : codigoGrupo,
                emailUsuario,
                "Reserva confirmada a partir de opção sugerida pela IA. Sala " +
                        sala.getNome() +
                        " em " + dto.dataReserva() +
                        " das " + dto.horarioInicio() +
                        " às " + dto.horarioFim() +
                        " — assentos " + dto.posicoesAssentos()
        );

        if (salvas.size() == 1) {
            notificacaoEmailService.enviarConfirmacaoReserva(
                    usuario.getEmail(),
                    usuario.getUsername(),
                    sala.getNome(),
                    dto.dataReserva().toString(),
                    dto.horarioInicio().toString(),
                    dto.horarioFim().toString(),
                    salvas.get(0).getPosicaoAssento()
            );
        } else {
            notificacaoEmailService.enviarConfirmacaoReservaGrupo(
                    usuario.getEmail(),
                    usuario.getUsername(),
                    sala.getNome(),
                    dto.dataReserva().toString(),
                    dto.horarioInicio().toString(),
                    dto.horarioFim().toString(),
                    salvas.stream().map(Reserva::getPosicaoAssento).toList()
            );
        }

        return salvas;
    }

    private List<OpcaoReservaDTO> montarOpcoes(
            Sala sala,
            List<Assento> assentosLivres,
            List<List<String>> tiposPorPessoa,
            CriterioProximidade criterio,
            int limite
    ) {
        List<OpcaoReservaDTO> opcoes = new ArrayList<>();
        List<Assento> candidatos = new ArrayList<>(assentosLivres);

        CriterioProximidade criterioFinal = criterio != null
                ? criterio
                : CriterioProximidade.NENHUM;

        double raio = sala.getRaioProximidade() != null
                ? sala.getRaioProximidade()
                : 5.0;

        while (opcoes.size() < limite) {
            List<Assento> alocados;

            try {
                alocados = alocacaoService.alocar(
                        candidatos,
                        tiposPorPessoa,
                        criterioFinal,
                        raio
                );
            } catch (ResponseStatusException ex) {
                break;
            }

            if (alocados == null || alocados.isEmpty()) {
                break;
            }

            List<Assento> todosAssentosDaSala =
                    assentoRepository.findBySalaIdOrderByPosicao(sala.getId());

            List<AssentoVisualDTO> assentosSugeridosVisual = alocados.stream()
                    .map(assento -> toAssentoVisualDTO(assento, "SUGERIDO", true))
                    .toList();

            List<AssentoVisualDTO> assentosDaSalaVisual = montarAssentosDaSalaParaVisualizacao(
                    todosAssentosDaSala,
                    assentosLivres,
                    alocados
            );

            opcoes.add(new OpcaoReservaDTO(
                    opcoes.size() + 1,

                    sala.getId(),
                    sala.getNome(),
                    montarLocalizacaoSala(sala),
                    sala.getAndar(),
                    sala.getBloco(),
                    sala.getCidade(),
                    sala.getEstado(),
                    sala.getEquipamentosSala() != null
                            ? sala.getEquipamentosSala().stream().map(Enum::name).toList()
                            : List.of(),

                    assentosSugeridosVisual,
                    assentosDaSalaVisual,

                    criterioFinal.name(),
                    montarObservacaoOpcao(criterioFinal)
            ));

            List<Integer> posicoesUsadas = alocados.stream()
                    .map(Assento::getPosicao)
                    .toList();

            candidatos = candidatos.stream()
                    .filter(a -> !posicoesUsadas.contains(a.getPosicao()))
                    .toList();

            if (candidatos.size() < tiposPorPessoa.size()) {
                break;
            }
        }

        return opcoes;
    }


    private AssentoOpcaoDTO toAssentoOpcaoDTO(Assento assento) {
        return new AssentoOpcaoDTO(
                assento.getId(),
                assento.getPosicao(),
                assento.getTipoAssento(),
                assento.getCoordenadaX(),
                assento.getCoordenadaY()
        );
    }

    private List<Assento> buscarAssentosLivres(
            Integer salaId,
            LocalDate dataReserva,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        List<Integer> posicoesOcupadas = reservaRepository.buscarPosicoesOcupadas(
                salaId,
                dataReserva,
                horarioInicio,
                horarioFim,
                StatusReserva.CANCELADA
        );

        return assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .filter(a -> !posicoesOcupadas.contains(a.getPosicao()))
                .toList();
    }

    private List<List<String>> montarTiposPorPessoa(
            Integer quantidadePessoas,
            List<List<String>> tiposRecebidos
    ) {
        int quantidadePorLista = tiposRecebidos != null ? tiposRecebidos.size() : 0;
        int quantidadeFinal = quantidadePessoas != null ? quantidadePessoas : quantidadePorLista;

        if (quantidadeFinal <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade de pessoas da reserva deve ser maior que zero."
            );
        }

        List<List<String>> resultado = new ArrayList<>();

        for (int i = 0; i < quantidadeFinal; i++) {
            List<String> tiposPessoa = tiposRecebidos != null && i < tiposRecebidos.size()
                    ? tiposRecebidos.get(i)
                    : List.of();

            resultado.add(normalizarTiposPessoa(tiposPessoa, i + 1));
        }

        return resultado;
    }

    private List<String> normalizarTiposPessoa(List<String> tipos, int numeroPessoa) {
        if (tipos == null) {
            return List.of();
        }

        List<String> normalizados = tipos.stream()
                .filter(Objects::nonNull)
                .map(this::normalizarTipoAssento)
                .filter(t -> !t.isBlank())
                .distinct()
                .toList();

        if (normalizados.size() > 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A pessoa " + numeroPessoa + " pode informar no máximo 3 tipos de posição."
            );
        }

        return normalizados;
    }

    private String normalizarTipoAssento(String valor) {
        if (valor == null) {
            return "";
        }

        String semAcento = Normalizer
                .normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return semAcento
                .trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}

