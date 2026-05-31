package com.squad27.gerenciadorsalas.controller;


import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.UsuarioDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioResponseDTO;
import com.squad27.gerenciadorsalas.services.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/usuarios/")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("listarUsuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios(){

        var usuarios = usuarioService.listarUsuarios();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("meuPerfil")
    public ResponseEntity<UsuarioResponseDTO> verMeuPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        var usuario = usuarioService.buscarMeuPerfilPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("DeletarUsuario")
    public ResponseEntity<String> deletarUsuarioPorId(@RequestParam Integer id){

        usuarioService.deletarUsuarioPorId(id);

        return ResponseEntity.ok("USUARIO DELETADO!");
    }

    @DeleteMapping("deletarConta")
    public ResponseEntity<String> deletarConta() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailAutenticado = authentication.getName();

        System.out.println("EMAIL AUTENTICADO: [" + emailAutenticado + "]");

        usuarioService.deletarUsuarioAutenticado(emailAutenticado);

        return ResponseEntity.ok("CONTA DELETADA");
    }



    @PutMapping("atualizarConta")
    public ResponseEntity<String> atualizarUsuarioPorId(@RequestBody UsuarioDTO usuarioDTO){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usernameAutenticado = authentication.getName();

        usuarioService.atualizarUsuarioAutenticado(usernameAutenticado, usuarioDTO);

        return ResponseEntity.ok("USUARIO ATUALIZADO");


    }

}
