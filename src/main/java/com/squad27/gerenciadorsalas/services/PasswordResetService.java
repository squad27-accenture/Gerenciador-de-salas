package com.squad27.gerenciadorsalas.services;


import com.squad27.gerenciadorsalas.domain.PasswordResetToken;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repositories.PasswordResetTokenRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacaoEmailService notificacaoEmailService;


    public void solicitarRecuperacao(String email) {
        usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));


        String codigo = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken token = PasswordResetToken.builder()
                .email(email)
                .codigo(codigo)
                .expiracao(LocalDateTime.now().plusMinutes(15)) // expira em 15min
                .usado(false)
                .build();

        tokenRepository.save(token);
        enviarEmail(email, codigo);
    }


    public void redefinirSenha(String email, String codigo, String novaSenha) {
        PasswordResetToken token = tokenRepository.findByEmailAndCodigo(email, codigo)
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        if (token.isUsado()) {
            throw new RuntimeException("Código já utilizado");
        }
        if (token.getExpiracao().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código expirado");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);


        token.setUsado(true);
        tokenRepository.save(token);
    }



    private void enviarEmail(String email, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();

        notificacaoEmailService.enviarCodigoRecuperacao(email, codigo);
    }
}

