package com.squad27.gerenciadorsalas.services;


import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.UsuarioDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioResponseDTO;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    public List<Usuario> listarUsuarios(){
        return usuarioRepository.findAllByDeletadoFalse();
    }

    public UsuarioResponseDTO buscarMeuPerfilPorId(Integer id){
        Usuario usuario = usuarioRepository.findByIdAndDeletadoFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

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

    @Transactional
    public void deletarUsuarioPorId(Integer id){
        if (!usuarioRepository.findByIdAndDeletadoFalse(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        usuarioRepository.softDeleteById(id);
    }

    @Transactional
    public void deletarUsuarioAutenticado(String email) {
        usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
        usuarioRepository.softDeleteByEmail(email);
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