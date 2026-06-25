package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.ConviteGrupo;
import com.squad27.gerenciadorsalas.domain.Grupo;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.*;
import com.squad27.gerenciadorsalas.enums.Role;
import com.squad27.gerenciadorsalas.enums.StatusConviteGrupo;
import com.squad27.gerenciadorsalas.repositories.ConviteGrupoRepository;
import com.squad27.gerenciadorsalas.repositories.GrupoRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConviteGrupoRepository conviteGrupoRepository;

    public GrupoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository,
            ConviteGrupoRepository conviteGrupoRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
        this.conviteGrupoRepository = conviteGrupoRepository;
    }

    public GrupoResponseDTO criar(GrupoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);

        if (!isAdmin(usuarioLogado) && !isTechLeader(usuarioLogado)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode criar grupos.");
        }

        validarNome(dto.nome());

        Usuario lider;

        if (isAdmin(usuarioLogado) && dto.liderId() != null) {
            lider = buscarUsuarioPorId(dto.liderId());
        } else {
            lider = usuarioLogado;
        }

        List<Usuario> usuarios = new ArrayList<>();

        if (isAdmin(usuarioLogado) && dto.usuarioIds() != null && !dto.usuarioIds().isEmpty()) {
            usuarios.addAll(buscarUsuariosValidos(dto.usuarioIds()));
        }

        if (usuarios.stream().noneMatch(u -> u.getId().equals(lider.getId()))) {
            usuarios.add(lider);
        }

        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome().trim());
        grupo.setDescricao(dto.descricao());
        grupo.setAtivo(true);
        grupo.setLider(lider);
        grupo.setUsuarios(usuarios);

        return toResponse(grupoRepository.save(grupo), usuarioLogado);
    }

    public List<GrupoResponseDTO> listar(String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);

        List<Grupo> grupos;

        if (isAdmin(usuarioLogado)) {
            grupos = grupoRepository.findAllByAtivoTrue();
        } else {
            grupos = grupoRepository.buscarMeusGrupos(emailUsuario);
        }

        return grupos.stream()
                .map(grupo -> toResponse(grupo, usuarioLogado))
                .toList();
    }

    public GrupoResponseDTO buscarPorId(Integer id, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);
        Grupo grupo = buscarGrupoAtivo(id);

        validarPodeVisualizar(grupo, usuarioLogado);

        return toResponse(grupo, usuarioLogado);
    }

    public GrupoResponseDTO editar(Integer id, GrupoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);
        Grupo grupo = buscarGrupoAtivo(id);

        validarPodeGerenciarGrupo(grupo, usuarioLogado);

        if (dto.nome() != null && !dto.nome().isBlank()) {
            grupo.setNome(dto.nome().trim());
        }

        if (dto.descricao() != null) {
            grupo.setDescricao(dto.descricao());
        }

        if (isAdmin(usuarioLogado) && dto.liderId() != null) {
            Usuario novoLider = buscarUsuarioPorId(dto.liderId());
            grupo.setLider(novoLider);

            if (grupo.getUsuarios().stream().noneMatch(u -> u.getId().equals(novoLider.getId()))) {
                grupo.getUsuarios().add(novoLider);
            }
        }

        if (isAdmin(usuarioLogado) && dto.usuarioIds() != null) {
            grupo.setUsuarios(buscarUsuariosValidos(dto.usuarioIds()));

            if (grupo.getLider() != null &&
                    grupo.getUsuarios().stream().noneMatch(u -> u.getId().equals(grupo.getLider().getId()))) {
                grupo.getUsuarios().add(grupo.getLider());
            }
        }

        return toResponse(grupoRepository.save(grupo), usuarioLogado);
    }

    public void deletar(Integer id, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);
        Grupo grupo = buscarGrupoAtivo(id);

        validarPodeGerenciarGrupo(grupo, usuarioLogado);

        grupo.setAtivo(false);
        grupoRepository.save(grupo);
    }

    public ConviteGrupoResponseDTO convidar(Integer grupoId, ConviteGrupoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);
        Grupo grupo = buscarGrupoAtivo(grupoId);

        validarPodeGerenciarGrupo(grupo, usuarioLogado);

        if (dto.email() == null || dto.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o e-mail do convidado.");
        }

        String emailConvidado = dto.email().trim().toLowerCase();

        boolean jaEstaNoGrupo = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(emailConvidado));

        if (jaEstaNoGrupo) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já faz parte do grupo.");
        }

        boolean convitePendente = conviteGrupoRepository.existsByGrupoIdAndEmailConvidadoIgnoreCaseAndStatus(
                grupoId,
                emailConvidado,
                StatusConviteGrupo.PENDENTE
        );

        if (convitePendente) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe convite pendente para esse e-mail.");
        }

        ConviteGrupo convite = new ConviteGrupo();
        convite.setGrupo(grupo);
        convite.setEmailConvidado(emailConvidado);
        convite.setConvidadoPor(usuarioLogado);
        convite.setStatus(StatusConviteGrupo.PENDENTE);
        convite.setCriadoEm(LocalDateTime.now());

        return new ConviteGrupoResponseDTO(conviteGrupoRepository.save(convite));
    }

    public List<ConviteGrupoResponseDTO> meusConvites(String emailUsuario) {
        return conviteGrupoRepository
                .findAllByEmailConvidadoIgnoreCaseAndStatus(emailUsuario, StatusConviteGrupo.PENDENTE)
                .stream()
                .map(ConviteGrupoResponseDTO::new)
                .toList();
    }

    public ConviteGrupoResponseDTO aceitarConvite(Integer conviteId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioLogado(emailUsuario);

        ConviteGrupo convite = conviteGrupoRepository.findByIdAndStatus(
                conviteId,
                StatusConviteGrupo.PENDENTE
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Convite não encontrado ou já respondido."
        ));

        if (!convite.getEmailConvidado().equalsIgnoreCase(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse convite não pertence a você.");
        }

        Grupo grupo = convite.getGrupo();

        boolean jaEstaNoGrupo = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioLogado.getId()));

        if (!jaEstaNoGrupo) {
            grupo.getUsuarios().add(usuarioLogado);
            grupoRepository.save(grupo);
        }

        convite.setStatus(StatusConviteGrupo.ACEITO);
        convite.setRespondidoEm(LocalDateTime.now());

        return new ConviteGrupoResponseDTO(conviteGrupoRepository.save(convite));
    }

    public ConviteGrupoResponseDTO recusarConvite(Integer conviteId, String emailUsuario) {
        ConviteGrupo convite = conviteGrupoRepository.findByIdAndStatus(
                conviteId,
                StatusConviteGrupo.PENDENTE
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Convite não encontrado ou já respondido."
        ));

        if (!convite.getEmailConvidado().equalsIgnoreCase(emailUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse convite não pertence a você.");
        }

        convite.setStatus(StatusConviteGrupo.RECUSADO);
        convite.setRespondidoEm(LocalDateTime.now());

        return new ConviteGrupoResponseDTO(conviteGrupoRepository.save(convite));
    }

    private GrupoResponseDTO toResponse(Grupo grupo, Usuario usuarioLogado) {
        List<ConviteGrupoResponseDTO> convites = List.of();

        if (isAdmin(usuarioLogado) || isLider(grupo, usuarioLogado)) {
            convites = conviteGrupoRepository
                    .findAllByGrupoIdAndStatus(grupo.getId(), StatusConviteGrupo.PENDENTE)
                    .stream()
                    .map(ConviteGrupoResponseDTO::new)
                    .toList();
        }

        return new GrupoResponseDTO(grupo, convites);
    }

    private void validarPodeVisualizar(Grupo grupo, Usuario usuario) {
        if (isAdmin(usuario) || isLider(grupo, usuario) || isMembro(grupo, usuario)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não pode visualizar este grupo.");
    }

    private void validarPodeGerenciarGrupo(Grupo grupo, Usuario usuario) {
        if (isAdmin(usuario) || isLider(grupo, usuario)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o líder do grupo ou admin pode alterar esse grupo.");
    }

    private boolean isAdmin(Usuario usuario) {
        return usuario.getRole() == Role.ADMIN;
    }

    private boolean isTechLeader(Usuario usuario) {
        return usuario.getRole() == Role.TECHLEADER;
    }

    private boolean isLider(Grupo grupo, Usuario usuario) {
        return grupo.getLider() != null &&
                grupo.getLider().getId().equals(usuario.getId());
    }

    private boolean isMembro(Grupo grupo, Usuario usuario) {
        return grupo.getUsuarios() != null &&
                grupo.getUsuarios().stream().anyMatch(u -> u.getId().equals(usuario.getId()));
    }

    private Usuario buscarUsuarioLogado(String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário logado não encontrado."));
    }

    private Usuario buscarUsuarioPorId(Integer id) {
        return usuarioRepository.findByIdAndDeletadoFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + id));
    }

    private Grupo buscarGrupoAtivo(Integer id) {
        return grupoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado ou inativo."));
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do grupo é obrigatório.");
        }
    }

    private List<Usuario> buscarUsuariosValidos(List<Integer> usuarioIds) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            return new ArrayList<>();
        }

        return usuarioIds.stream()
                .distinct()
                .map(this::buscarUsuarioPorId)
                .toList();
    }
}