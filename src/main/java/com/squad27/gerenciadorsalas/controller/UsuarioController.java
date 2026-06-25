package com.squad27.gerenciadorsalas.controller;


import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.AtualizarTipoFuncionarioDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioListagemDTO;
import com.squad27.gerenciadorsalas.dto.UsuarioResponseDTO;
import com.squad27.gerenciadorsalas.services.UsuarioService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping({"", "/listarUsuarios"})
    public ResponseEntity<List<UsuarioListagemDTO>> listarUsuarios() {
        var usuarios = usuarioService.listarUsuarios()
                .stream()
                .map(UsuarioListagemDTO::new)
                .toList();

        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/meuPerfil")
    public ResponseEntity<UsuarioResponseDTO> verMeuPerfil() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        var usuario = usuarioService.buscarMeuPerfilPorEmail(email);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarUsuarioPorId(@PathVariable Integer id){
        usuarioService.deletarUsuarioPorId(id);
        return ResponseEntity.ok("Usuário deletado com sucesso.");
    }

    @DeleteMapping("/deletarConta")
    public ResponseEntity<String> deletarConta() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailAutenticado = authentication.getName();

        System.out.println("EMAIL AUTENTICADO: [" + emailAutenticado + "]");

        usuarioService.deletarUsuarioAutenticado(emailAutenticado);

        return ResponseEntity.ok("CONTA DELETADA");
    }



    @PutMapping
    public ResponseEntity<String> atualizarUsuarioPorId(@RequestBody UsuarioDTO usuarioDTO){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usernameAutenticado = authentication.getName();

        usuarioService.atualizarUsuarioAutenticado(usernameAutenticado, usuarioDTO);

        return ResponseEntity.ok("USUARIO ATUALIZADO");


    }

    @PutMapping("/me/tipo-funcionario")
    public ResponseEntity<UsuarioListagemDTO> atualizarMeuTipoFuncionario(
            @RequestBody AtualizarTipoFuncionarioDTO dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Usuario usuario = usuarioService.atualizarTipoFuncionario(
                userDetails.getUsername(),
                dto.tipoFuncionario()
        );

        return ResponseEntity.ok(new UsuarioListagemDTO(usuario));
    }

}
