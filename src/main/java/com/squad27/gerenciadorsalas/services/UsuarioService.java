package com.squad27.gerenciadorsalas.services;


import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;


    public List<Usuario> listarUsuarios(){

        return usuarioRepository.findAll();
    }

    public void deletarUsuarioPorId(Integer id){

        usuarioRepository.deleteById(id);

    }

    public void atualizarUsuarioPorId(Integer id , Usuario usuario){

        Usuario usuarioEntity = usuarioRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Usuario nao encontrado")
        );


        Usuario usuarioAtualizado = Usuario.builder()
                .id(usuarioEntity.getId())
                .email(usuario.getEmail() != null ? usuario.getEmail() : usuarioEntity.getEmail())
                .senha(usuario.getSenha() != null ? usuario.getSenha() : usuarioEntity.getSenha())
                .username(usuarioEntity.getUsername())
                .role(usuario.getRole() != null ? usuario.getRole() : usuarioEntity.getRole())
                .build();

        usuarioRepository.saveAndFlush(usuarioAtualizado);


    }


}
