package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.TipoAssentoCustom;
import com.squad27.gerenciadorsalas.dto.TipoAssentoRequestDTO;
import com.squad27.gerenciadorsalas.dto.TipoAssentoResponseDTO;
import com.squad27.gerenciadorsalas.repositories.TipoAssentoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TipoAssentoService {

    private final TipoAssentoRepository repo;

    public TipoAssentoService(TipoAssentoRepository repo) {
        this.repo = repo;
    }

    public TipoAssentoResponseDTO criar(TipoAssentoRequestDTO dto) {
        String nome = dto.nome().trim().toUpperCase();
        if (repo.existsByNomeIgnoreCase(nome))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tipo já existe: " + nome);
        TipoAssentoCustom t = new TipoAssentoCustom();
        t.setNome(nome);
        t.setAtivo(true);
        return new TipoAssentoResponseDTO(repo.save(t));
    }

    public List<TipoAssentoResponseDTO> listar() {
        return repo.findAll().stream().map(TipoAssentoResponseDTO::new).toList();
    }

    public TipoAssentoResponseDTO inativar(Integer id) {
        TipoAssentoCustom t = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo não encontrado"));
        t.setAtivo(false);
        return new TipoAssentoResponseDTO(repo.save(t));
    }

    public TipoAssentoResponseDTO reativar(Integer id) {
        TipoAssentoCustom t = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo não encontrado"));
        t.setAtivo(true);
        return new TipoAssentoResponseDTO(repo.save(t));
    }

    public void validarNome(String nome) {
        TipoAssentoCustom t = repo.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Tipo de assento inválido: " + nome));
        if (!t.getAtivo())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de assento inativo: " + nome);
    }
}