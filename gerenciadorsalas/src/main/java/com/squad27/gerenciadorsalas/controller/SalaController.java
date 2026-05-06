package com.squad27.gerenciadorsalas.controller;

import com.squad27.gerenciadorsalas.domain.Role;
import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.SalaDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/salas")

public class SalaController {


    // isso aq é so pra simular um "banco de  dados" ja que n temos ainda.
    private List<Sala> salas = new ArrayList<>();

    @PostMapping
    public String cadastrarSala(@RequestBody SalaDTO salaDTO){

        // uma simulação de usuário logado  como admin
        Usuario usuario = new Usuario();
        usuario.setRole(Role.ADMIN);


        if (usuario.getRole() != Role.ADMIN){
            return "Apenas administradores podem cadastrar salas";
        }

        if (salaDTO.nome()  ==  null || salaDTO.nome().isEmpty() || salaDTO.capacidade() <=0){
            return "Preencha todos os campos obrigatórios";
        }

        for (Sala s : salas){
            if (s.getNome().equalsIgnoreCase(salaDTO.nome())){
                return "Já existe uma sala com este nome";
            }
        }

        Sala sala =  new Sala();
        sala.setNome(salaDTO.nome());
        sala.setCapacidade(salaDTO.capacidade());

        salas.add(sala);

        return "Sala cadastrada com sucesso";
    }


}
