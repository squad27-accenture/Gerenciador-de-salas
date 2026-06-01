package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoRequestDTO;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssentoService {

    private final AssentoRepository assentoRepository;
    private final SalaRepository salaRepository;

    public AssentoService(AssentoRepository assentoRepository, SalaRepository salaRepository) {
        this.assentoRepository = assentoRepository;
        this.salaRepository = salaRepository;
    }

    public Assento criarAssento(Integer salaId, AssentoRequestDTO dto) {
        Sala sala = salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));

        int proximaPosicao = assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .mapToInt(Assento::getPosicao)
                .max()
                .orElse(0) + 1;

        Assento assento = new Assento();
        assento.setSala(sala);
        assento.setPosicao(proximaPosicao);
        assento.setTipoAssento(dto.tipoAssento());
        assento.setCoordenadaX(dto.coordenadaX());
        assento.setCoordenadaY(dto.coordenadaY());
        assento.setAtivo(true);
        assento.setEquipamentos(dto.equipamentos() != null ? dto.equipamentos() : List.of());

        return assentoRepository.save(assento);
    }

    public Assento atualizarAssento(Integer salaId, Integer posicao, AssentoRequestDTO dto) {
        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assento na posição " + posicao + " não encontrado na sala."));

        if (dto.tipoAssento() != null)   assento.setTipoAssento(dto.tipoAssento());
        if (dto.coordenadaX() != null)   assento.setCoordenadaX(dto.coordenadaX());
        if (dto.coordenadaY() != null)   assento.setCoordenadaY(dto.coordenadaY());
        if (dto.equipamentos() != null)  assento.setEquipamentos(dto.equipamentos());

        return assentoRepository.save(assento);
    }

    public Assento inativarAssento(Integer salaId, Integer posicao) {
        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assento na posição " + posicao + " não encontrado na sala."));

        if (!assento.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assento já está inativo.");
        }

        assento.setAtivo(false);
        return assentoRepository.save(assento);
    }

    public Assento reativarAssento(Integer salaId, Integer posicao) {
        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assento na posição " + posicao + " não encontrado na sala."));

        if (assento.getAtivo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Assento já está ativo.");
        }

        assento.setAtivo(true);
        return assentoRepository.save(assento);
    }
}