package com.squad27.gerenciadorsalas.services;

import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.dto.RegisterDTO;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    private UsuarioRepository Repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        return Repository.findByEmail(email).orElseThrow();
    }

    public Usuario cadastro(RegisterDTO registerDTO) {
        if (Repository.existsByEmail(registerDTO.email())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uma conta com esse email ja foi criado!"
            );
        }

        if (registerDTO.tipoFuncionario() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O tipo de funcionário é obrigatório."
            );
        }

        String senhaEncriptada = new BCryptPasswordEncoder().encode(registerDTO.senha());

        Usuario novousuario = new Usuario(
                registerDTO.email(),
                senhaEncriptada,
                registerDTO.role(),
                registerDTO.username(),
                registerDTO.tipoFuncionario()
        );

        return Repository.save(novousuario);
    }

}
