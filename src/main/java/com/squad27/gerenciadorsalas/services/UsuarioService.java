package com.squad27.gerenciadorsalas.services;


import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.UsuarioDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioResponseDTO;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    public List<Usuario> listarUsuarios(){

        return usuarioRepository.findAll();
    }

   public UsuarioResponseDTO buscarMeuPerfilPorId(Integer id){

       Usuario usuario = usuarioRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

       // Retorna apenas os dados seguros
       return new UsuarioResponseDTO(
               usuario.getId(),
               usuario.getUsername(),
               usuario.getEmail(),
               usuario.getRole()
       );
   }

    public UsuarioResponseDTO buscarMeuPerfilPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }

    public void deletarUsuarioPorId(Integer id){

        usuarioRepository.deleteById(id);

    }
    public void deletarUsuarioAutenticado(String email) {

        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        usuarioRepository.deleteById(usuarioEntity.getId());
    }

    public void atualizarUsuarioAutenticado(String username, UsuarioDTO usuarioDTO){

        Usuario usuarioEntity = usuarioRepository.findByUsername(username).orElseThrow(
                ()-> new RuntimeException("Usuario nao encontrado")
        );



        String senhaFinal = (usuarioDTO.senha() != null && !usuarioDTO.senha().isBlank())
                ? passwordEncoder.encode(usuarioDTO.senha())
                : usuarioEntity.getSenha();

        Usuario usuarioAtualizado = Usuario.builder()
                .id(usuarioEntity.getId())
                .email(usuarioDTO.email() != null ? usuarioDTO.email() : usuarioEntity.getEmail())
                .senha(senhaFinal)
                .username(usuarioEntity.getUsername())
                .role(usuarioEntity.getRole())
                .build();

        usuarioRepository.saveAndFlush(usuarioAtualizado);
    }


    }








