package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Salas;
import com.squad27.gerenciadorsalas.dto.SalasDTO;
import com.squad27.gerenciadorsalas.repositories.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SalasService {

    @Autowired
    private SalaRepository repository;

    public Salas cadastrarsala(SalasDTO salaDTO){
        Salas sala = new Salas();

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

        sala.setNome(salaDTO.nome());
        sala.setCapacidade(salaDTO.capacidade());
        sala.setStatus(salaDTO.statusSala());
        sala.setLocal(salaDTO.local());
        return repository.save(sala);
    }

    public List<Salas> listarsalas(){

        return repository.findAll();
    }
}
