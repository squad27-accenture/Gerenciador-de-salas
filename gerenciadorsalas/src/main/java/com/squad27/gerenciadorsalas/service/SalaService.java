package com.squad27.gerenciadorsalas.service;

import com.squad27.gerenciadorsalas.domain.Role;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repository.SalaRepository;
import org.springframework.stereotype.Service;

@Service
public class SalaService {

    private final SalaRepository repository;

    public SalaService(SalaRepository repository) {
        this.repository = repository;
    }

    public void cadastrarSala(Sala sala){


        if (sala.getNumerosala() == null || sala.getNumerosala().isEmpty() ||
                sala.getCapacidade() <= 0) {
            throw new RuntimeException("Preencha todos os campos obrigatórios");
        }

        Usuario usuario = new Usuario();  // ← precisa estar ANTES do if
        usuario.setRole(Role.ADMIN);

        if (usuario.getRole() != Role.ADMIN) {
            throw new RuntimeException("Apenas administradores podem cadastrar salas") ;
        }

        if (repository.existsByNumerosala(sala.getNumerosala())){
            throw new RuntimeException("Sala já cadastrada!");
        }



        repository.saveAndFlush(sala);
    }

    public Sala buscarSalaPorNumero(String numerosala){

        return repository.findByNumerosala(numerosala).orElseThrow(
                () -> new RuntimeException("Sala nao encontrada!")
        );

    }

    public void deletarSalaPorNumero(String numerosala){

        repository.deleteByNumerosala(numerosala);
    }

    public void atualizarSalaPorId(Integer id , Sala sala){

        Sala salaEntity = repository.findById(id).orElseThrow(
                () -> new RuntimeException("Id nao encontrado")
        );

        Sala salaAtualizada = Sala.builder()
                .id(salaEntity.getId())
                .numerosala(sala.getNumerosala() != null ? sala.getNumerosala() : salaEntity.getNumerosala())
                .capacidade(sala.getCapacidade() > 0 ? sala.getCapacidade() : salaEntity.getCapacidade())
                .build();

        repository.saveAndFlush(salaAtualizada);
    }
}
