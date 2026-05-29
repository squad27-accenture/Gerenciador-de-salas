package com.squad27.gerenciadorsalas.services;


import com.squad27.gerenciadorsalas.domain.PasswordResetToken;
import com.squad27.gerenciadorsalas.domain.Usuario;
import com.squad27.gerenciadorsalas.repositories.PasswordResetTokenRepository;
import com.squad27.gerenciadorsalas.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
    private final JavaMailSender mailSender;

    // PASSO 1 — gera e envia o código
    public void solicitarRecuperacao(String email) {
        usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email não encontrado"));

        // Gera código de 6 dígitos
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

    // PASSO 2 — valida o código e redefine a senha
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

        // Marca o token como usado
        token.setUsado(true);
        tokenRepository.save(token);
    }

    private void enviarEmail(String email, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bernardomendesoficial@gmail.com"); // <- email que você verificou no SendGrid
        message.setTo(email);
        message.setSubject("Recuperação de senha");
        message.setText("Seu código de recuperação é: " + codigo + "\nEle expira em 15 minutos.");

        System.out.println(">>> Enviando email para: " + email); // <-- adicione isso
        mailSender.send(message);
        System.out.println(">>> Email enviado com sucesso");

    }
}

