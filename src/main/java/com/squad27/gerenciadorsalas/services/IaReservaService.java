package com.squad27.gerenciadorsalas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Grupo;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.enums.CriterioProximidade;
import com.squad27.gerenciadorsalas.enums.EquipamentosAssento;
import com.squad27.gerenciadorsalas.enums.StatusReserva;
import com.squad27.gerenciadorsalas.enums.TipoFuncionario;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.GrupoRepository;
import com.squad27.gerenciadorsalas.repositories.ReservaRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IaReservaService {

    private final SalaRepository salaRepository;
    private final AssentoRepository assentoRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final DisponibilidadeService disponibilidadeService;
    private final ObjectMapper objectMapper;

    @Value("${ia.python.base-url:http://localhost:8001}")
    private String iaBaseUrl;

    @Value("${ia.python.opcoes-path:/ia/opcoes}")
    private String iaOpcoesPath;

    @Value("${ia.python.timeout-seconds:30}")
    private long timeoutSeconds;

    public IaReservaService(
            SalaRepository salaRepository,
            AssentoRepository assentoRepository,
            ReservaRepository reservaRepository,
            UsuarioRepository usuarioRepository,
            GrupoRepository grupoRepository,
            DisponibilidadeService disponibilidadeService,
            ObjectMapper objectMapper
    ) {
        this.salaRepository = salaRepository;
        this.assentoRepository = assentoRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.grupoRepository = grupoRepository;
        this.disponibilidadeService = disponibilidadeService;
        this.objectMapper = objectMapper;
    }

    public IaOpcoesResponseDTO gerarOpcoes(IaReservaRequestDTO dto, String emailUsuario) {
        validarPedido(dto);

        List<Usuario> usuarios = buscarUsuarios(dto, emailUsuario);

        List<IaUsuarioDTO> usuariosPayload = usuarios.stream()
                .map(this::toIaUsuarioDTO)
                .toList();

        List<IaSalaDTO> salasPayload = montarSalasComAssentosLivres(dto, usuarios.size());

        if (salasPayload.isEmpty()) {
            return new IaOpcoesResponseDTO(
                    "Não encontrei salas com assentos livres suficientes para esse horário.",
                    dto,
                    List.of()
            );
        }

        String tipoFuncionarioPrincipal = descobrirTipoFuncionarioPrincipal(usuarios);

        IaPythonRequestDTO payload = new IaPythonRequestDTO(
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim(),

                usuarios.size(), // quantidadeAssentos
                usuarios.size(), // quantidadePessoas

                dto.grupoId(),
                null, // salaId
                null, // nomeSala

                tipoFuncionarioPrincipal,

                isProximidade(dto),
                criterio(dto),

                equipamentosObrigatoriosPorTipo(tipoFuncionarioPrincipal),
                equipamentosDesejaveisPorTipo(tipoFuncionarioPrincipal),
                tiposAssentoPreferidosPorTipo(tipoFuncionarioPrincipal),

                null, // cidade
                null, // estado

                5,     // limiteResultados
                false, // incluirIncompativeis
                80,    // compatibilidadeMinima
                true,  // usarIaGenerativa
                true, // incluirMapaAssentos

                usuariosPayload,
                salasPayload
        );

        IaOpcoesResponseDTO respostaPython = chamarPython(payload);

        return normalizarResposta(respostaPython, dto, salasPayload, usuarios);   }

    private void validarPedido(IaReservaRequestDTO dto) {
        if (dto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe os dados da reserva.");
        }

        validarHorarios(dto.horarioInicio(), dto.horarioFim());
        validarDataReserva(dto.dataReserva());
    }

    private void validarHorarios(LocalTime inicio, LocalTime fim) {
        if (inicio == null || fim == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário de início e fim são obrigatórios.");
        }

        if (!inicio.isBefore(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário inicial precisa ser antes do horário final.");
        }
    }

    private void validarDataReserva(LocalDate dataReserva) {
        if (dataReserva == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data da reserva é obrigatória.");
        }

        if (dataReserva.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível realizar reservas para datas passadas.");
        }
    }

    private List<Usuario> buscarUsuarios(IaReservaRequestDTO dto, String emailUsuario) {
        if (dto.grupoId() != null) {
            Grupo grupo = grupoRepository.findByIdAndAtivoTrue(dto.grupoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Grupo não encontrado ou inativo."
                    ));

            List<Usuario> usuariosDoGrupo = grupo.getUsuarios() == null
                    ? List.of()
                    : grupo.getUsuarios().stream()
                      .filter(u -> !Boolean.TRUE.equals(u.getDeletado()))
                      .toList();

            if (usuariosDoGrupo.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupo não possui usuários ativos.");
            }

            return usuariosDoGrupo;
        }

        if (dto.usuarioIds() != null && !dto.usuarioIds().isEmpty()) {
            List<Usuario> usuarios = dto.usuarioIds().stream()
                    .distinct()
                    .map(id -> usuarioRepository.findByIdAndDeletadoFalse(id)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Usuário não encontrado ou deletado: " + id
                            )))
                    .toList();

            if (usuarios.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe pelo menos um usuário.");
            }

            return usuarios;
        }

        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário logado não encontrado."
                ));

        return List.of(usuarioLogado);
    }

    private List<IaSalaDTO> montarSalasComAssentosLivres(IaReservaRequestDTO dto, int quantidadePessoas) {
        return salaRepository.findAllByDeletadoFalse()
                .stream()
                .map(sala -> montarSalaSeDisponivel(sala, dto, quantidadePessoas))
                .filter(Objects::nonNull)
                .toList();
    }

    private IaSalaDTO montarSalaSeDisponivel(Sala sala, IaReservaRequestDTO dto, int quantidadePessoas) {
        try {
            disponibilidadeService.validarDisponibilidade(
                    sala.getId(),
                    dto.dataReserva(),
                    dto.horarioInicio(),
                    dto.horarioFim()
            );
        } catch (Exception ex) {
            System.out.println("Sala ignorada por indisponibilidade: " + sala.getNome() + " | Motivo: " + ex.getMessage());
            return null;
        }

        List<Integer> posicoesOcupadas = reservaRepository.buscarPosicoesOcupadas(
                sala.getId(),
                dto.dataReserva(),
                dto.horarioInicio(),
                dto.horarioFim(),
                StatusReserva.CANCELADA
        );

        List<Assento> assentosLivres = assentoRepository.findBySalaIdOrderByPosicao(sala.getId())
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getAtivo()))
                .filter(a -> !posicoesOcupadas.contains(a.getPosicao()))
                .toList();

        if (assentosLivres.size() < quantidadePessoas) {
            System.out.println("Sala ignorada por falta de assentos: " + sala.getNome()
                    + " | livres=" + assentosLivres.size()
                    + " | precisa=" + quantidadePessoas);
            return null;
        }

        return new IaSalaDTO(
                sala.getId(),
                sala.getNome(),
                sala.getCapacidade(),
                montarLocalizacaoSala(sala),
                sala.getAndar(),
                sala.getBloco(),
                sala.getCidade(),
                sala.getEstado(),
                sala.getRaioProximidade() != null ? sala.getRaioProximidade() : 5.0,
                sala.getEquipamentosSala() != null
                        ? sala.getEquipamentosSala().stream().map(Enum::name).toList()
                        : List.of(),
                assentosLivres.stream().map(this::toIaAssentoDTO).toList()
        );
    }

    private IaUsuarioDTO toIaUsuarioDTO(Usuario usuario) {
        TipoFuncionario tipo = usuario.getTipoFuncionario() != null
                ? usuario.getTipoFuncionario()
                : TipoFuncionario.OUTRO;

        return new IaUsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                tipo.name()
        );
    }

    private IaAssentoDTO toIaAssentoDTO(Assento assento) {
        List<String> equipamentos = assento.getEquipamentos() != null
                ? assento.getEquipamentos().stream().map(Enum::name).toList()
                : List.of();

        return new IaAssentoDTO(
                assento.getId(),
                assento.getPosicao(),
                "A" + assento.getPosicao(),
                assento.getTipoAssento(),
                assento.getCoordenadaX(),
                assento.getCoordenadaY(),
                assento.getTipoCadeira(),
                assento.getTipoMesa(),
                temComputador(assento),
                temMonitor(assento),
                temTela4k(assento),
                equipamentos
        );
    }

    private Boolean temComputador(Assento assento) {
        return assento.getEquipamentos() != null && assento.getEquipamentos().stream().anyMatch(e ->
                e == EquipamentosAssento.COMPUTADOR_PC || e == EquipamentosAssento.COMPUTADOR_NOTEBOOK
        );
    }

    private Boolean temMonitor(Assento assento) {
        return assento.getEquipamentos() != null && assento.getEquipamentos().stream().anyMatch(e ->
                e == EquipamentosAssento.MONITOR || e == EquipamentosAssento.MONITOR_4K
        );
    }

    private Boolean temTela4k(Assento assento) {
        return assento.getEquipamentos() != null
                && assento.getEquipamentos().contains(EquipamentosAssento.MONITOR_4K);
    }

    private IaOpcoesResponseDTO chamarPython(IaPythonRequestDTO payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            System.out.println();
            System.out.println("===== PAYLOAD ENVIADO PARA PYTHON =====");
            System.out.println(json);
            System.out.println("=======================================");
            System.out.println();

            String baseUrl = iaBaseUrl.endsWith("/")
                    ? iaBaseUrl.substring(0, iaBaseUrl.length() - 1)
                    : iaBaseUrl;

            String path = iaOpcoesPath.startsWith("/")
                    ? iaOpcoesPath
                    : "/" + iaOpcoesPath;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println();
            System.out.println("===== STATUS PYTHON =====");
            System.out.println(response.statusCode());

            System.out.println("===== RESPOSTA BRUTA PYTHON =====");
            System.out.println(response.body());
            System.out.println("=================================");
            System.out.println();

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Python IA retornou erro " + response.statusCode() + ": " + response.body()
                );
            }

            return objectMapper.readValue(response.body(), IaOpcoesResponseDTO.class);

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Falha ao comunicar com a IA Python: " + ex.getMessage(),
                    ex
            );
        }
    }

    private IaOpcoesResponseDTO normalizarResposta(
            IaOpcoesResponseDTO respostaPython,
            IaReservaRequestDTO pedidoOriginal,
            List<IaSalaDTO> salasPayload,
            List<Usuario> usuarios
    ) {
        if (respostaPython == null || respostaPython.opcoes() == null) {
            return new IaOpcoesResponseDTO("IA não retornou opções.", pedidoOriginal, List.of());
        }

        Map<Integer, IaSalaDTO> salasPorId = salasPayload.stream()
                .collect(Collectors.toMap(IaSalaDTO::id, s -> s));

        List<IaOpcaoReservaDTO> opcoes = new ArrayList<>();
        int numero = 1;

        CriterioProximidade criterio = criterio(pedidoOriginal);

        for (IaOpcaoReservaDTO opcao : respostaPython.opcoes()) {
            if (opcao == null || opcao.salaId() == null) {
                continue;
            }

            IaSalaDTO sala = salasPorId.get(opcao.salaId());

            if (sala == null) {
                System.out.println("Opção ignorada: salaId não existe no payload enviado. salaId=" + opcao.salaId());
                continue;
            }

            List<AlocacaoIA> alocacao = montarMelhorAlocacaoParaSala(
                    sala,
                    usuarios,
                    criterio
            );

            if (alocacao.size() < usuarios.size()) {
                System.out.println("Opção ignorada: não consegui alocar todos os usuários na sala " + sala.nome());
                continue;
            }

            double compatibilidadeReal = calcularCompatibilidadeReal(alocacao, criterio);

            if (compatibilidadeReal < 80.0) {
                System.out.println("Opção ignorada: compatibilidade real menor que 80. sala="
                        + sala.nome() + " compatibilidade=" + compatibilidadeReal);
                continue;
            }

            List<IaAssentoEscolhidoDTO> assentosDetalhados = alocacao.stream()
                    .map(item -> toIaAssentoEscolhidoDTO(item.assento(), item.usuario()))
                    .toList();

            List<Integer> posicoes = alocacao.stream()
                    .map(item -> item.assento().posicao())
                    .toList();

            opcoes.add(new IaOpcaoReservaDTO(
                    numero++,
                    opcao.salaId(),
                    opcao.salaNome() != null ? opcao.salaNome() : sala.nome(),
                    compatibilidadeReal,
                    gerarMotivoReal(opcao, compatibilidadeReal, criterio),
                    assentosDetalhados,
                    posicoes
            ));

            if (opcoes.size() >= 5) {
                break;
            }
        }

        opcoes = opcoes.stream()
                .sorted(Comparator.comparing(IaOpcaoReservaDTO::compatibilidade).reversed())
                .toList();

        return new IaOpcoesResponseDTO(
                opcoes.isEmpty()
                        ? "A IA não encontrou opções com compatibilidade mínima de 80%."
                        : "Encontrei opções recomendadas pela IA. Escolha uma para confirmar.",
                pedidoOriginal,
                opcoes
        );
    }

    private List<AlocacaoIA> montarMelhorAlocacaoParaSala(
            IaSalaDTO sala,
            List<Usuario> usuarios,
            CriterioProximidade criterio
    ) {
        if (sala.assentosLivres() == null || sala.assentosLivres().isEmpty()) {
            return List.of();
        }

        if (usuarios == null || usuarios.isEmpty()) {
            return List.of();
        }

        List<Usuario> usuariosOrdenados = usuarios.stream()
                .sorted(Comparator.comparingInt(u -> prioridadeTipoFuncionario(u.getTipoFuncionario())))
                .toList();

        List<IaAssentoDTO> assentosBase = new ArrayList<>(sala.assentosLivres());

        if (criterio == CriterioProximidade.OBRIGATORIO ||
                criterio == CriterioProximidade.PREFERENCIAL) {

            List<IaAssentoDTO> blocoProximo = encontrarBlocoProximoIA(
                    sala.assentosLivres(),
                    usuariosOrdenados.size()
            );

            if (!blocoProximo.isEmpty()) {
                assentosBase = blocoProximo;
            }

            if (criterio == CriterioProximidade.OBRIGATORIO && blocoProximo.isEmpty()) {
                return List.of();
            }
        }

        List<AlocacaoIA> resultado = new ArrayList<>();
        Set<Integer> assentosUsados = new HashSet<>();

        for (Usuario usuario : usuariosOrdenados) {
            IaAssentoDTO melhor = escolherMelhorAssentoParaUsuario(
                    usuario,
                    assentosBase,
                    assentosUsados
            );

            if (melhor == null && assentosBase != sala.assentosLivres()) {
                melhor = escolherMelhorAssentoParaUsuario(
                        usuario,
                        sala.assentosLivres(),
                        assentosUsados
                );
            }

            if (melhor == null) {
                continue;
            }

            double score = pontuarAssentoParaUsuarioIA(usuario, melhor);

            assentosUsados.add(melhor.id());
            resultado.add(new AlocacaoIA(melhor, usuario, score));
        }

        return resultado;
    }

    private List<IaAssentoDTO> encontrarBlocoProximoIA(
            List<IaAssentoDTO> assentos,
            int quantidade
    ) {
        if (assentos == null || assentos.size() < quantidade) {
            return List.of();
        }

        List<IaAssentoDTO> ordenados = assentos.stream()
                .sorted(Comparator.comparingInt(this::posicaoSegura))
                .toList();

        List<IaAssentoDTO> melhorBloco = List.of();
        int menorDistancia = Integer.MAX_VALUE;

        for (int i = 0; i <= ordenados.size() - quantidade; i++) {
            List<IaAssentoDTO> bloco = ordenados.subList(i, i + quantidade);

            int primeira = posicaoSegura(bloco.get(0));
            int ultima = posicaoSegura(bloco.get(bloco.size() - 1));
            int distancia = ultima - primeira;

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhorBloco = new ArrayList<>(bloco);
            }
        }

        return melhorBloco;
    }

    private double pontuarAssentoParaUsuarioIA(Usuario usuario, IaAssentoDTO assento) {
        TipoFuncionario tipo = usuario.getTipoFuncionario();

        if (tipo == null) {
            tipo = TipoFuncionario.OUTRO;
        }

        List<String> equipamentos = equipamentosIA(assento);

        double pontos = 0.0;

        switch (tipo) {
            case PROGRAMADOR -> {
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 45;
                if (equipamentos.contains("COMPUTADOR_NOTEBOOK")) pontos += 35;
                if (equipamentos.contains("MONITOR")) pontos += 20;
                if (equipamentos.contains("MONITOR_4K")) pontos += 25;
                if (equipamentos.contains("TECLADO")) pontos += 8;
                if (equipamentos.contains("MOUSE")) pontos += 8;
                if (equipamentos.contains("PONTO_DE_REDE")) pontos += 7;

                if (!equipamentos.contains("COMPUTADOR_PC")
                        && !equipamentos.contains("COMPUTADOR_NOTEBOOK")) {
                    pontos = Math.min(pontos, 55);
                }
            }

            case DESIGNER -> {
                if (equipamentos.contains("MONITOR_4K")) pontos += 70;
                if (equipamentos.contains("MONITOR")) pontos += 20;
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 10;
                if (equipamentos.contains("COMPUTADOR_NOTEBOOK")) pontos += 10;
                if (equipamentos.contains("DOCKING_STATION")) pontos += 8;

                if (!equipamentos.contains("MONITOR_4K")) {
                    pontos = Math.min(pontos, 55);
                }
            }

            case QA -> {
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 45;
                if (equipamentos.contains("COMPUTADOR_NOTEBOOK")) pontos += 35;
                if (equipamentos.contains("MONITOR")) pontos += 25;
                if (equipamentos.contains("MONITOR_4K")) pontos += 25;
                if (equipamentos.contains("PONTO_DE_REDE")) pontos += 8;

                if (!equipamentos.contains("COMPUTADOR_PC")
                        && !equipamentos.contains("COMPUTADOR_NOTEBOOK")) {
                    pontos = Math.min(pontos, 60);
                }
            }

            case SUPORTE -> {
                if (equipamentos.contains("HEADSET")) pontos += 45;
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 30;
                if (equipamentos.contains("COMPUTADOR_NOTEBOOK")) pontos += 25;
                if (equipamentos.contains("RAMAL_TELEFONICO")) pontos += 15;
                if (equipamentos.contains("MONITOR")) pontos += 10;

                if (!equipamentos.contains("HEADSET")) {
                    pontos = Math.min(pontos, 60);
                }
            }

            case GESTOR -> {
                if (equipamentos.contains("WEBCAM")) pontos += 25;
                if (equipamentos.contains("MONITOR_4K")) pontos += 25;
                if (equipamentos.contains("MONITOR")) pontos += 20;
                if (equipamentos.contains("DOCKING_STATION")) pontos += 15;
                if (equipamentos.contains("COMPUTADOR_NOTEBOOK")) pontos += 10;
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 10;
            }

            case OUTRO -> {
                pontos += 60;
                if (equipamentos.contains("TOMADA_ELETRICA")) pontos += 10;
                if (equipamentos.contains("MONITOR")) pontos += 10;
                if (equipamentos.contains("COMPUTADOR_PC")) pontos += 10;
            }
        }

        String tipoAssento = assento.tipoAssento();

        if (tipoAssento != null) {
            if (tipo == TipoFuncionario.DESIGNER &&
                    (tipoAssento.equals("ESTACAO_DESIGN") || tipoAssento.equals("ESTACAO_EXECUTIVA"))) {
                pontos += 8;
            }

            if (tipo == TipoFuncionario.PROGRAMADOR &&
                    (tipoAssento.equals("ESTACAO_PADRAO") || tipoAssento.equals("ESTACAO_EXECUTIVA"))) {
                pontos += 8;
            }

            if (tipo == TipoFuncionario.GESTOR &&
                    (tipoAssento.equals("ESTACAO_EXECUTIVA") || tipoAssento.equals("SALA_REUNIAO_INDIVIDUAL"))) {
                pontos += 8;
            }
        }

        return Math.max(0.0, Math.min(100.0, pontos));
    }

    private IaAssentoDTO escolherMelhorAssentoParaUsuario(
            Usuario usuario,
            List<IaAssentoDTO> assentos,
            Set<Integer> assentosUsados
    ) {
        return assentos.stream()
                .filter(a -> a.id() != null)
                .filter(a -> !assentosUsados.contains(a.id()))
                .max(Comparator.comparingDouble(a -> pontuarAssentoParaUsuarioIA(usuario, a)))
                .orElse(null);
    }

    private double calcularCompatibilidadeReal(
            List<AlocacaoIA> alocacao,
            CriterioProximidade criterio
    ) {
        if (alocacao == null || alocacao.isEmpty()) {
            return 0.0;
        }

        if (criterio == CriterioProximidade.OBRIGATORIO && !validarProximidadeReal(alocacao)) {
            return 0.0;
        }

        double mediaEquipamentos = alocacao.stream()
                .mapToDouble(AlocacaoIA::score)
                .average()
                .orElse(0.0);

        double bonusProximidade = 0.0;

        if (criterio == CriterioProximidade.OBRIGATORIO) {
            bonusProximidade = 8.0;
        }

        if (criterio == CriterioProximidade.PREFERENCIAL) {
            bonusProximidade = calcularBonusProximidadeReal(alocacao);
        }

        double resultado = mediaEquipamentos + bonusProximidade;

        return Math.max(0.0, Math.min(100.0, Math.round(resultado * 10.0) / 10.0));
    }

    private boolean validarProximidadeReal(List<AlocacaoIA> alocacao) {
        if (alocacao.size() <= 1) {
            return true;
        }

        List<Integer> posicoes = alocacao.stream()
                .map(item -> posicaoSegura(item.assento()))
                .sorted()
                .toList();

        int primeira = posicoes.get(0);
        int ultima = posicoes.get(posicoes.size() - 1);

        int distancia = ultima - primeira;

        return distancia <= alocacao.size() * 2;
    }

    private double calcularBonusProximidadeReal(List<AlocacaoIA> alocacao) {
        if (alocacao.size() <= 1) {
            return 0.0;
        }

        List<Integer> posicoes = alocacao.stream()
                .map(item -> posicaoSegura(item.assento()))
                .sorted()
                .toList();

        int primeira = posicoes.get(0);
        int ultima = posicoes.get(posicoes.size() - 1);

        int distancia = ultima - primeira;

        if (distancia <= alocacao.size()) {
            return 8.0;
        }

        if (distancia <= alocacao.size() * 2) {
            return 4.0;
        }

        return 0.0;
    }

    private List<IaAssentoEscolhidoDTO> montarAssentosDetalhados(
            List<Integer> posicoes,
            IaSalaDTO sala,
            List<Usuario> usuarios
    ) {
        Map<Integer, IaAssentoDTO> assentosPorPosicao = sala.assentosLivres()
                .stream()
                .collect(Collectors.toMap(IaAssentoDTO::posicao, a -> a, (a, b) -> a));

        List<IaAssentoEscolhidoDTO> resultado = new ArrayList<>();

        for (int i = 0; i < posicoes.size(); i++) {
            Integer posicao = posicoes.get(i);
            IaAssentoDTO assento = assentosPorPosicao.get(posicao);

            if (assento == null) {
                continue;
            }

            Usuario usuario = i < usuarios.size() ? usuarios.get(i) : null;

            resultado.add(toIaAssentoEscolhidoDTO(assento, usuario));
        }

        return resultado;
    }

    private IaAssentoEscolhidoDTO toIaAssentoEscolhidoDTO(
            IaAssentoDTO assento,
            Usuario usuario
    ) {
        String tipoAssento = assento.tipoAssento() != null
                ? assento.tipoAssento()
                : "ESTACAO_PADRAO";

        List<String> equipamentos = assento.equipamentos() != null
                ? assento.equipamentos()
                : List.of();

        return new IaAssentoEscolhidoDTO(
                assento.id(),
                assento.posicao(),
                "A" + assento.posicao(),
                tipoAssento,
                equipamentos,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getUsername() : null,
                usuario != null ? usuario.getEmail() : null
        );
    }

    private boolean posicoesPertencemASala(List<Integer> posicoes, IaSalaDTO sala) {
        Set<Integer> posicoesLivresDaSala = sala.assentosLivres()
                .stream()
                .map(IaAssentoDTO::posicao)
                .collect(Collectors.toSet());

        return posicoes.stream().allMatch(posicoesLivresDaSala::contains);
    }

    private List<Integer> extrairPosicoes(IaOpcaoReservaDTO opcao, Map<Integer, IaAssentoDTO> assentosPorId) {
        if (opcao.posicoesAssentos() != null && !opcao.posicoesAssentos().isEmpty()) {
            return opcao.posicoesAssentos();
        }

        if (opcao.assentos() == null || opcao.assentos().isEmpty()) {
            return List.of();
        }

        List<Integer> posicoes = new ArrayList<>();

        for (IaAssentoEscolhidoDTO escolhido : opcao.assentos()) {
            if (escolhido == null) {
                continue;
            }

            if (escolhido.posicao() != null) {
                posicoes.add(escolhido.posicao());
                continue;
            }

            if (escolhido.assentoId() != null && assentosPorId.containsKey(escolhido.assentoId())) {
                posicoes.add(assentosPorId.get(escolhido.assentoId()).posicao());
            }
        }

        return posicoes;
    }

    private boolean isProximidade(IaReservaRequestDTO dto) {
        if (dto.proximidade() != null) {
            return dto.proximidade();
        }

        return criterio(dto) != CriterioProximidade.NENHUM;
    }

    private CriterioProximidade criterio(IaReservaRequestDTO dto) {
        return dto.criterioProximidade() != null
                ? dto.criterioProximidade()
                : CriterioProximidade.NENHUM;
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

        return partes.isEmpty() ? "Localização não informada" : String.join(", ", partes);
    }

    private String descobrirTipoFuncionarioPrincipal(List<Usuario> usuarios) {
        if (usuarios == null || usuarios.isEmpty()) {
            return TipoFuncionario.OUTRO.name();
        }

        Map<String, Long> contagem = usuarios.stream()
                .map(usuario -> usuario.getTipoFuncionario() != null
                        ? usuario.getTipoFuncionario().name()
                        : TipoFuncionario.OUTRO.name())
                .collect(Collectors.groupingBy(tipo -> tipo, Collectors.counting()));

        return contagem.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TipoFuncionario.OUTRO.name());
    }

    private List<String> equipamentosObrigatoriosPorTipo(String tipoFuncionario) {
        if (tipoFuncionario == null) {
            return List.of();
        }

        return switch (tipoFuncionario) {
            case "PROGRAMADOR", "DEV" -> List.of("COMPUTADOR_PC");
            case "DESIGNER", "DESIGN" -> List.of("MONITOR_4K");
            case "QA" -> List.of("COMPUTADOR_PC", "MONITOR");
            case "SUPORTE" -> List.of("COMPUTADOR_PC", "HEADSET");
            default -> List.of();
        };
    }

    private List<String> equipamentosDesejaveisPorTipo(String tipoFuncionario) {
        if (tipoFuncionario == null) {
            return List.of();
        }

        return switch (tipoFuncionario) {
            case "PROGRAMADOR", "DEV" -> List.of("MONITOR", "TECLADO", "MOUSE", "PONTO_DE_REDE");
            case "DESIGNER", "DESIGN" -> List.of("COMPUTADOR_PC", "MONITOR", "DOCKING_STATION");
            case "QA" -> List.of("WEBCAM", "PONTO_DE_REDE");
            case "SUPORTE" -> List.of("RAMAL_TELEFONICO", "MONITOR");
            case "GESTOR" -> List.of("MONITOR_4K", "WEBCAM", "VIDEO_CONFERENCIA");
            default -> List.of("TOMADA_ELETRICA");
        };
    }

    private List<String> equipamentosIA(IaAssentoDTO assento) {
        if (assento.equipamentos() == null) {
            return List.of();
        }

        return assento.equipamentos()
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
    }

    private int prioridadeTipoFuncionario(TipoFuncionario tipo) {
        if (tipo == null) return 99;

        return switch (tipo) {
            case DESIGNER -> 1;
            case PROGRAMADOR -> 2;
            case QA -> 3;
            case SUPORTE -> 4;
            case GESTOR -> 5;
            case OUTRO -> 99;
        };
    }

    private int posicaoSegura(IaAssentoDTO assento) {
        return assento.posicao() != null ? assento.posicao() : 999999;
    }

    private String gerarMotivoReal(
            IaOpcaoReservaDTO opcao,
            double compatibilidade,
            CriterioProximidade criterio
    ) {
        String proximidadeTexto = switch (criterio) {
            case OBRIGATORIO -> "com assentos obrigatoriamente próximos";
            case PREFERENCIAL -> "priorizando assentos próximos";
            case NENHUM -> "sem exigência de proximidade";
        };

        String motivoBase = opcao.motivo() != null
                ? opcao.motivo()
                : "Opção recomendada pela IA.";

        return motivoBase + " Compatibilidade recalculada pelo sistema: "
                + compatibilidade + "%, " + proximidadeTexto + ".";
    }

    private record AlocacaoIA(
            IaAssentoDTO assento,
            Usuario usuario,
            double score
    ) {
    }

    private List<String> tiposAssentoPreferidosPorTipo(String tipoFuncionario) {
        if (tipoFuncionario == null) {
            return List.of();
        }

        return switch (tipoFuncionario) {
            case "PROGRAMADOR", "DEV" -> List.of("ESTACAO_PADRAO", "ESTACAO_EXECUTIVA");
            case "DESIGNER", "DESIGN" -> List.of("ESTACAO_DESIGN", "ESTACAO_EXECUTIVA");
            case "QA" -> List.of("ESTACAO_PADRAO");
            case "SUPORTE" -> List.of("ESTACAO_PADRAO");
            case "GESTOR" -> List.of("ESTACAO_EXECUTIVA", "SALA_REUNIAO_INDIVIDUAL");
            default -> List.of("ESTACAO_PADRAO", "HOT_DESK");
        };
    }
}