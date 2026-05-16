package com.squad27.gerenciadorsalas.service;

import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repository.UsuarioRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;


    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public void salvarUsuario(Usuario usuario){

        if (repository.existsByEmail(usuario.getEmail())){
            throw new RuntimeException("Email já cadastrado!");
        }

        repository.saveAndFlush(usuario);
    }

    public Usuario buscarUsuarioPorEmail(String email){

        return repository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email nao encontrado!")
        );
    }

    public void deletarUsuarioPorEmail(String email){

        repository.deleteByEmail(email);
    }

    public void atualizarPorId(Integer id , Usuario usuario){

        Usuario usuarioEntity = repository.findById(id).orElseThrow(
                () -> new RuntimeException("Id nao encontrado")
        );

        Usuario usuarioAtualizado =Usuario.builder()
                .id(usuarioEntity.getId())
                .email(usuario.getEmail() != null ? usuario.getEmail() : usuarioEntity.getEmail())
                .senha(usuario.getSenha() != null ? usuario.getSenha() : usuarioEntity.getSenha())
                .role(usuario.getRole() != null ? usuario.getRole() : usuarioEntity.getRole())
                .build();

        repository.saveAndFlush(usuarioAtualizado);

    }

}
