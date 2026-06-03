package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
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
    private final TipoAssentoService tipoAssentoService;

    public AssentoService(AssentoRepository assentoRepository, SalaRepository salaRepository, TipoAssentoService tipoAssentoService) {
        this.assentoRepository = assentoRepository;
        this.salaRepository = salaRepository;
        this.tipoAssentoService = tipoAssentoService;
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
        if (dto.tipoAssento() != null) {
            tipoAssentoService.validarNome(dto.tipoAssento());
            assento.setTipoAssento(dto.tipoAssento().trim().toUpperCase());
        }
        assento.setCoordenadaX(dto.coordenadaX());
        assento.setCoordenadaY(dto.coordenadaY());
        assento.setTipoCadeira(dto.tipoCadeira());
        assento.setTipoMesa(dto.tipoMesa());
        assento.setAtivo(true);
        assento.setEquipamentos(dto.equipamentos() != null ? dto.equipamentos() : List.of());

        return assentoRepository.save(assento);
    }

    public List<AssentoReponseDTO> listarAssentos(Integer salaId) {
        salaRepository.findById(salaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sala não encontrada."));
        return assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .map(a -> new AssentoReponseDTO(
                        a.getId(),
                        a.getPosicao(),
                        a.getTipoAssento(),
                        a.getCoordenadaX(),
                        a.getCoordenadaY(),
                        a.getTipoCadeira(),
                        a.getTipoMesa(),
                        a.getAtivo(),
                        a.getEquipamentos().stream().map(Enum::name).toList()
                ))
                .toList();
    }

    public AssentoReponseDTO buscarPorPosicao(Integer salaId, Integer posicao) {
        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assento na posição " + posicao + " não encontrado na sala."));
        return new AssentoReponseDTO(
                assento.getId(),
                assento.getPosicao(),
                assento.getTipoAssento(),
                assento.getCoordenadaX(),
                assento.getCoordenadaY(),
                assento.getTipoCadeira(),
                assento.getTipoMesa(),
                assento.getAtivo(),
                assento.getEquipamentos().stream().map(Enum::name).toList()
        );
    }

    public Assento atualizarAssento(Integer salaId, Integer posicao, AssentoRequestDTO dto) {
        Assento assento = assentoRepository.findBySalaIdAndPosicao(salaId, posicao)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assento na posição " + posicao + " não encontrado na sala."));

        if (dto.tipoAssento() != null) {
            tipoAssentoService.validarNome(dto.tipoAssento());
            assento.setTipoAssento(dto.tipoAssento().trim().toUpperCase());
        }
        if (dto.coordenadaX() != null)   assento.setCoordenadaX(dto.coordenadaX());
        if (dto.coordenadaY() != null)   assento.setCoordenadaY(dto.coordenadaY());
        if (dto.tipoCadeira() != null)   assento.setTipoCadeira(dto.tipoCadeira());
        if (dto.tipoMesa() != null)      assento.setTipoMesa(dto.tipoMesa());
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