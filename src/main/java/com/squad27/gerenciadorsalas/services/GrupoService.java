package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Grupo;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.GrupoRequestDTO;
import com.squad27.gerenciadorsalas.dto.GrupoResponseDTO;
import com.squad27.gerenciadorsalas.repositories.GrupoRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    public GrupoService(
            GrupoRepository grupoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.grupoRepository = grupoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public GrupoResponseDTO criar(GrupoRequestDTO dto) {
        validarNome(dto.nome());

        List<Usuario> usuarios = buscarUsuariosValidos(dto.usuarioIds());

        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome().trim());
        grupo.setDescricao(dto.descricao());
        grupo.setAtivo(true);
        grupo.setUsuarios(usuarios);

        Grupo grupoSalvo = grupoRepository.save(grupo);

        return new GrupoResponseDTO(grupoSalvo);
    }

    public List<GrupoResponseDTO> listar() {
        return grupoRepository.findAllByAtivoTrue()
                .stream()
                .map(GrupoResponseDTO::new)
                .toList();
    }

    public GrupoResponseDTO buscarPorId(Integer id) {
        Grupo grupo = buscarGrupoAtivo(id);
        return new GrupoResponseDTO(grupo);
    }

    public GrupoResponseDTO editar(Integer id, GrupoRequestDTO dto) {
        Grupo grupo = buscarGrupoAtivo(id);

        if (dto.nome() != null && !dto.nome().isBlank()) {
            grupo.setNome(dto.nome().trim());
        }

        if (dto.descricao() != null) {
            grupo.setDescricao(dto.descricao());
        }

        if (dto.usuarioIds() != null) {
            List<Usuario> usuarios = buscarUsuariosValidos(dto.usuarioIds());
            grupo.setUsuarios(usuarios);
        }

        Grupo grupoSalvo = grupoRepository.save(grupo);

        return new GrupoResponseDTO(grupoSalvo);
    }

    public void deletar(Integer id) {
        Grupo grupo = buscarGrupoAtivo(id);

        grupo.setAtivo(false);

        grupoRepository.save(grupo);
    }

    private Grupo buscarGrupoAtivo(Integer id) {
        return grupoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Grupo não encontrado ou inativo."
                ));
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O nome do grupo é obrigatório."
            );
        }
    }

    private List<Usuario> buscarUsuariosValidos(List<Integer> usuarioIds) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe pelo menos um usuário para criar o grupo."
            );
        }

        return usuarioIds.stream()
                .distinct()
                .map(id -> usuarioRepository.findByIdAndDeletadoFalse(id)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuário não encontrado ou deletado: " + id
                        )))
                .toList();
    }
}