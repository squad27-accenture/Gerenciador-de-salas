package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Grupo;
import com.squad27.gerenciadorsalas.domain.Reserva;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.ConfirmarReservaOpcaoDTO;
import com.squad27.gerenciadorsalas.dto.OcupacaoResponseDTO;
import com.squad27.gerenciadorsalas.dto.ReservaDTO;
import com.squad27.gerenciadorsalas.enums.Role;
import com.squad27.gerenciadorsalas.enums.StatusReserva;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.GrupoRepository;
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
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaRepository salaRepository;
    private final AssentoRepository assentoRepository;
    private final GrupoRepository grupoRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final AuditoriaService auditoriaService;

    public ReservaService(
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            SalaRepository salaRepository,
            AssentoRepository assentoRepository,
            GrupoRepository grupoRepository,
            DisponibilidadeService disponibilidadeService,
            AuditoriaService auditoriaService
    ) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.salaRepository = salaRepository;
        this.assentoRepository = assentoRepository;
        this.grupoRepository = grupoRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.auditoriaService = auditoriaService;
    }

    /*
     * RESERVA INDIVIDUAL MANUAL
     *
     * Fluxo:
     * - front mostra a sala
     * - usuário escolhe um assento
     * - backend só valida e salva
     */
    public Reserva ReservarAssento(ReservaDTO dto, String emailUsuario) {
        validarPedidoIndividual(dto);

        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        Sala sala = buscarSala(dto.salaId());

        Assento assento = buscarAssentoAtivoDaSala(
                dto.salaId(),
                dto.posicaoAssento()
        );

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());

        disponibilidadeService.validarDisponibilidade(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

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

        Reserva salva = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "CRIACAO",
                "RESERVA_INDIVIDUAL",
                String.valueOf(salva.getId()),
                emailUsuario,
                "Reserva individual criada para sala " + sala.getNome() +
                        " em " + dto.dataReserva() +
                        " das " + dto.horarioInicio() +
                        " às " + dto.horarioFim() +
                        " — assento " + assento.getPosicao()
        );

        return salva;
    }

    /*
     * CONFIRMAR OPÇÃO DA IA
     *
     * Se vier grupoId:
     * - usa os usuários do grupo
     * - exige uma posição para cada usuário
     *
     * Se não vier grupoId:
     * - aceita somente uma posição
     * - reserva para o usuário logado
     */
    public List<Reserva> confirmarOpcao(ConfirmarReservaOpcaoDTO dto, String emailUsuario) {
        validarPedidoConfirmacaoOpcao(dto);

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);
        Sala sala = buscarSala(dto.salaId());

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());

        disponibilidadeService.validarDisponibilidade(
                dto.salaId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim()
        );

        List<Usuario> usuariosDaReserva = buscarUsuariosDaConfirmacao(dto, usuarioLogado);

        if (dto.posicoesAssentos().size() != usuariosDaReserva.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A quantidade de assentos precisa ser igual à quantidade de usuários da reserva."
            );
        }

        List<Reserva> reservas = new ArrayList<>();

        String codigoGrupo = usuariosDaReserva.size() > 1
                ? UUID.randomUUID().toString()
                : null;

        for (int i = 0; i < dto.posicoesAssentos().size(); i++) {
            Integer posicao = dto.posicoesAssentos().get(i);
            Usuario usuarioDaPosicao = usuariosDaReserva.get(i);

            Assento assento = buscarAssentoAtivoDaSala(dto.salaId(), posicao);

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
            reserva.setUsuario(usuarioDaPosicao);
            reserva.setStatusReserva(StatusReserva.CONFIRMADA);
            reserva.setPosicaoAssento(assento.getPosicao());
            reserva.setCodigoGrupo(codigoGrupo);

            reservas.add(reserva);
        }

        List<Reserva> salvas = reservaRepository.saveAll(reservas);

        auditoriaService.registrar(
                "CRIACAO",
                codigoGrupo == null ? "RESERVA_INDIVIDUAL" : "RESERVA_GRUPO_IA",
                codigoGrupo == null ? String.valueOf(salvas.get(0).getId()) : codigoGrupo,
                emailUsuario,
                "Reserva confirmada a partir de opção da IA. Sala " +
                        sala.getNome() +
                        " em " + dto.dataReserva() +
                        " das " + dto.horarioInicio() +
                        " às " + dto.horarioFim() +
                        " — assentos " + dto.posicoesAssentos()
        );

        return salvas;
    }

    public Reserva cancelarReserva(Integer reservaId, String emailUsuario, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reserva não encontrada."
                ));

        Usuario solicitante = buscarUsuarioPorEmail(emailUsuario);

        boolean isDono = reserva.getUsuario().getEmail().equals(emailUsuario);
        boolean isGestor = isGestor(solicitante);

        if (!isDono && !isGestor) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para cancelar esta reserva."
            );
        }

        if (reserva.getStatusReserva() == StatusReserva.CANCELADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta reserva já está cancelada."
            );
        }

        reserva.setStatusReserva(StatusReserva.CANCELADA);
        reserva.setMotivoCancelamento(motivo);

        Reserva salva = reservaRepository.save(reserva);

        auditoriaService.registrar(
                "CANCELAMENTO",
                "RESERVA",
                String.valueOf(salva.getId()),
                emailUsuario,
                "Reserva cancelada. Motivo: " + (motivo != null ? motivo : "não informado")
        );

        return salva;
    }

    public List<Reserva> cancelarReservaGrupo(String codigoGrupo, String emailUsuario, String motivo) {
        if (codigoGrupo == null || codigoGrupo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Código do grupo é obrigatório."
            );
        }

        List<Reserva> reservas = reservaRepository.findByCodigoGrupo(codigoGrupo);

        if (reservas.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Reserva em grupo não encontrada."
            );
        }

        Usuario solicitante = buscarUsuarioPorEmail(emailUsuario);

        boolean gestor = isGestor(solicitante);
        boolean participante = reservas.stream()
                .anyMatch(r -> r.getUsuario().getEmail().equals(emailUsuario));

        if (!gestor && !participante) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para cancelar esta reserva em grupo."
            );
        }

        for (Reserva reserva : reservas) {
            reserva.setStatusReserva(StatusReserva.CANCELADA);
            reserva.setMotivoCancelamento(motivo);
        }

        List<Reserva> salvas = reservaRepository.saveAll(reservas);

        auditoriaService.registrar(
                "CANCELAMENTO",
                "RESERVA_GRUPO",
                codigoGrupo,
                emailUsuario,
                "Reserva em grupo cancelada. Motivo: " + (motivo != null ? motivo : "não informado")
        );

        return salvas;
    }

    public List<Integer> buscarAssentosOcupados(
            Integer salaId,
            LocalDate dataReserva,
            LocalTime horarioInicio,
            LocalTime horarioFim
    ) {
        validarHorarios(horarioInicio, horarioFim);
        validarDataReserva(dataReserva);

        return reservaRepository.buscarPosicoesOcupadas(
                salaId,
                dataReserva,
                horarioInicio,
                horarioFim,
                StatusReserva.CANCELADA
        );
    }

    public List<Reserva> buscarHistorico(
            Integer usuarioId,
            Integer salaId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        log.info(
                "Buscando histórico: usuarioId={} salaId={} dataInicio={} dataFim={}",
                usuarioId,
                salaId,
                dataInicio,
                dataFim
        );

        return reservaRepository.buscarHistorico(usuarioId, salaId, dataInicio, dataFim);
    }

    public OcupacaoResponseDTO relatórioOcupacao(
            Integer salaId,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        Sala sala = buscarSala(salaId);

        if (dataInicio == null || dataFim == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataInicio e dataFim são obrigatórias."
            );
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataInicio precisa ser menor ou igual à dataFim."
            );
        }

        long totalReservas = reservaRepository.contarReservasPorSalaEPeriodo(
                salaId,
                dataInicio,
                dataFim,
                StatusReserva.CANCELADA
        );

        int totalAssentos = sala.getAssentos() != null
                ? sala.getAssentos().size()
                : 0;

        long diasNoPeriodo = dataInicio.datesUntil(dataFim.plusDays(1)).count();
        long capacidadeTotal = (long) totalAssentos * diasNoPeriodo;

        double taxa = capacidadeTotal == 0
                ? 0.0
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

    private void validarPedidoIndividual(ReservaDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dados da reserva são obrigatórios."
            );
        }

        if (dto.salaId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "salaId é obrigatório."
            );
        }

        if (dto.posicaoAssento() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "posicaoAssento é obrigatório."
            );
        }

        if (dto.dataReserva() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataReserva é obrigatória."
            );
        }

        if (dto.horarioInicio() == null || dto.horarioFim() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "horarioInicio e horarioFim são obrigatórios."
            );
        }
    }

    private void validarPedidoConfirmacaoOpcao(ConfirmarReservaOpcaoDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dados da confirmação são obrigatórios."
            );
        }

        if (dto.salaId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "salaId é obrigatório."
            );
        }

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

        if (dto.dataReserva() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataReserva é obrigatória."
            );
        }

        if (dto.horarioInicio() == null || dto.horarioFim() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "horarioInicio e horarioFim são obrigatórios."
            );
        }
    }

    private List<Usuario> buscarUsuariosDaConfirmacao(
            ConfirmarReservaOpcaoDTO dto,
            Usuario usuarioLogado
    ) {
        if (dto.grupoId() == null) {
            if (dto.posicoesAssentos().size() > 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Para confirmar mais de um assento, informe grupoId."
                );
            }

            return List.of(usuarioLogado);
        }

        Grupo grupo = grupoRepository.findByIdAndAtivoTrue(dto.grupoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Grupo não encontrado ou inativo."
                ));

        List<Usuario> usuarios = grupo.getUsuarios() == null
                ? List.of()
                : grupo.getUsuarios()
                  .stream()
                  .filter(u -> !Boolean.TRUE.equals(u.getDeletado()))
                  .toList();

        if (usuarios.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Grupo não possui usuários ativos."
            );
        }

        return usuarios;
    }

    private Usuario buscarUsuarioPorEmail(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));
    }

    private Sala buscarSala(Integer salaId) {
        if (salaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "salaId é obrigatório."
            );
        }

        return salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sala não encontrada."
                ));
    }

    private Assento buscarAssentoAtivoDaSala(Integer salaId, Integer posicao) {
        if (posicao == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Posição do assento é obrigatória."
            );
        }

        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assento " + posicao + " não encontrado nesta sala."
                ));

        if (!Boolean.TRUE.equals(assento.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assento " + posicao + " está inativo."
            );
        }

        return assento;
    }

    private void validarHorarios(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Horário de início e fim são obrigatórios."
            );
        }

        if (!inicio.isBefore(fim)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário inicial precisa ser antes do horário final."
            );
        }
    }

    private void validarDataReserva(LocalDate dataReserva) {
        if (dataReserva == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data da reserva é obrigatória."
            );
        }

        if (dataReserva.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não é possível realizar reservas para datas passadas."
            );
        }
    }

    private void validarConflito(
            Integer salaId,
            Integer posicao,
            LocalDate data,
            LocalTime inicio,
            LocalTime fim
    ) {
        boolean ocupado = reservaRepository.existeConflitoDeHorario(
                salaId,
                posicao,
                data,
                inicio,
                fim,
                StatusReserva.CANCELADA
        );

        if (ocupado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assento " + posicao + " já está reservado nesse horário."
            );
        }
    }

    private boolean isGestor(Usuario usuario) {
        return usuario.getRole() == Role.ADMIN || usuario.getRole() == Role.TECHLEADER;
    }

    public List<Reserva> buscarHistoricoDoUsuarioLogado(
            Integer usuarioId,
            Integer salaId,
            LocalDate dataInicio,
            LocalDate dataFim,
            String emailUsuario
    ) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        boolean admin = usuarioLogado.getRole() == Role.ADMIN;

        Integer filtroUsuarioId = admin
                ? usuarioId
                : usuarioLogado.getId();

        return reservaRepository.buscarHistorico(
                filtroUsuarioId,
                salaId,
                dataInicio,
                dataFim
        );
    }
}