package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Assento;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.dto.AssentoReponseDTO;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import com.squad27.gerenciadorsalas.repositories.AssentoRepository;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SalaService {

    @Autowired
    private SalaRepository repository;

    @Autowired
    private AssentoRepository assentoRepository;

    public Sala cadastrarsala(SalaDTO salaDTO){
        Sala sala = new Sala();
        String nome = salaDTO.nome().trim();

        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma sala com esse nome."
            );
        }

        if (salaDTO.nome()  ==  null || salaDTO.nome().isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O nome da sala é obrigatorio.");
            }
        if (salaDTO.capacidade() <=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A capacidade da sala deve ser maior que 0.");
        }

        if (salaDTO.local() == null || salaDTO.local().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST ,
                    "O local é obrigatorio");
        }
        if (salaDTO.statusSala() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O status da sala é obrigatorio");
        }
        if (salaDTO.capacidade() > 0){
            for(int c = 1; c <= salaDTO.capacidade(); c++){
                Assento assento = new Assento();
                assento.setPosicao(c);

                sala.adicionarasseto(assento);
            }
        }
        sala.setNome(nome);
        sala.setCapacidade(salaDTO.capacidade());
        sala.setStatus(salaDTO.statusSala());
        sala.setLocal(salaDTO.local());
        sala.setCidade(salaDTO.cidade());
        sala.setEstado(salaDTO.estado());
        sala.setEquipamentosSala(salaDTO.equipamentosSala());
        return repository.save(sala);
    }

    public List<Sala> listarsalas(){

        return repository.findAll();
    }

    public void deletarSalaPorId(Integer id){

        repository.deleteById(id);

    }

    public void deletarSalaPorNome(String nome){

        repository.deleteByNome(nome);
    }

    public void atualizarSalaPorId (Integer id , Sala sala){

        Sala salaEntity = repository.findById(id).orElseThrow(
                ()-> new RuntimeException("Id nao encontrado!")
        );

        Sala salaAtualizado = Sala.builder()
                .nome(sala.getNome() != null ? sala.getNome() : salaEntity.getNome())
                .capacidade(sala.getCapacidade() != null ? sala.getCapacidade() : salaEntity.getCapacidade())
                .status(sala.getStatus() != null ? sala.getStatus() : salaEntity.getStatus())
                .id(salaEntity.getId())
                .local(sala.getLocal() != null ? sala.getLocal() : salaEntity.getLocal())
                .build();

        repository.saveAndFlush(salaAtualizado);
    }

    public List<AssentoReponseDTO> listarAssentosDaSala(Integer salaId) {
        return assentoRepository.findBySalaIdOrderByPosicao(salaId)
                .stream()
                .map(assento -> new AssentoReponseDTO(
                        assento.getId(),
                        assento.getPosicao(),
                        assento.getEquipamentoAssento() == null ? null : assento.getEquipamentoAssento().name()
                ))
                .toList();
    }
}
