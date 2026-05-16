package com.squad27.gerenciadorsalas.controller;


import com.squad27.gerenciadorsalas.domain.Sala;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.UsuarioDTO;
import com.squad27.gerenciadorsalas.services.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios(){

        var usuarios = usuarioService.listarUsuarios();

        return ResponseEntity.ok(usuarios);
    }

    @DeleteMapping
    public ResponseEntity<String> deletarUsuarioPorId(@RequestParam Integer id){

        usuarioService.deletarUsuarioPorId(id);

        return ResponseEntity.ok("USUARIO DELETADO!");
    }

    @PutMapping
    public ResponseEntity<String> atualizarUsuarioPorId(@RequestParam Integer id , @RequestBody UsuarioDTO usuarioDTO){

        Usuario usuario = Usuario.builder()
                .email(usuarioDTO.email())
                .senha(usuarioDTO.senha())
                .username(usuarioDTO.username())
                .role(usuarioDTO.role())
                .build();

        usuarioService.atualizarUsuarioPorId(id , usuario);

        return ResponseEntity.ok("USUARIO ATUALIZADO");


    }

}
