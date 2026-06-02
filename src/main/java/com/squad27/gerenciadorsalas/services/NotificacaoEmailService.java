package com.squad27.gerenciadorsalas.services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String emailRemetente;

    public void enviarConfirmacaoReserva(String emailDestino, String nomeUsuario,
                                         String nomeSala, String data,
                                         String horarioInicio, String horarioFim,
                                         int posicao) {
        String assunto = "Reserva confirmada — " + nomeSala;
        String corpo = String.format("""
                Olá, %s!
                
                Sua reserva foi confirmada com sucesso.
                
                📍 Sala: %s
                📅 Data: %s
                🕐 Horário: %s às %s
                💺 Assento: %d
                
                Caso precise cancelar, acesse o sistema e cancele com antecedência.
                
                Atenciosamente,
                Sistema de Gerenciamento de Salas
                """, nomeUsuario, nomeSala, data, horarioInicio, horarioFim, posicao);

        enviar(emailDestino, assunto, corpo);
    }

    public void enviarCancelamentoReserva(String emailDestino, String nomeUsuario,
                                          String nomeSala, String data,
                                          String horarioInicio, String horarioFim,
                                          int posicao) {
        String assunto = "Reserva cancelada — " + nomeSala;
        String corpo = String.format("""
                Olá, %s!
                
                Sua reserva foi cancelada.
                
                📍 Sala: %s
                📅 Data: %s
                🕐 Horário: %s às %s
                💺 Assento: %d
                
                Se o cancelamento não foi feito por você, entre em contato com o suporte.
                
                Atenciosamente,
                Sistema de Gerenciamento de Salas
                """, nomeUsuario, nomeSala, data, horarioInicio, horarioFim, posicao);

        enviar(emailDestino, assunto, corpo);
    }

    private void enviar(String emailDestino, String assunto, String corpo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailRemetente);
        message.setTo(emailDestino);
        message.setSubject(assunto);
        message.setText(corpo);
        mailSender.send(message);
    }
    public void enviarCodigoRecuperacao(String emailDestino, String codigo) {
        String assunto = "Recuperação de senha";
        String corpo = String.format("""
            Seu código de recuperação é: %s
            
            Ele expira em 15 minutos.
            
            Se você não solicitou a recuperação, ignore este e-mail.
            
            Atenciosamente,
            Sistema de Gerenciamento de Salas
            """, codigo);

        enviar(emailDestino, assunto, corpo);
    }

    public void enviarConfirmacaoReservaGrupo(String emailDestino, String nomeUsuario,
                                              String nomeSala, String data,
                                              String horarioInicio, String horarioFim,
                                              List<Integer> posicoes) {
        String assunto = "Reserva em grupo confirmada — " + nomeSala;

        String listaAssentos = posicoes.stream()
                .map(p -> "• Assento " + p)
                .collect(java.util.stream.Collectors.joining("\n"));

        String corpo = String.format("""
            Olá, %s!
            
            Sua reserva em grupo foi confirmada com sucesso.
            
            📍 Sala: %s
            📅 Data: %s
            🕐 Horário: %s às %s
            💺 Assentos reservados:
            %s
            
            Caso precise cancelar, acesse o sistema e cancele com antecedência.
            
            Atenciosamente,
            Sistema de Gerenciamento de Salas
            """, nomeUsuario, nomeSala, data, horarioInicio, horarioFim, listaAssentos);

        enviar(emailDestino, assunto, corpo);
    }

    public void enviarCancelamentoReservaGrupo(String emailDestino, String nomeUsuario,
                                               String nomeSala, String data,
                                               String horarioInicio, String horarioFim,
                                               List<Integer> posicoes) {
        String assunto = "Reserva em grupo cancelada — " + nomeSala;

        String listaAssentos = posicoes.stream()
                .map(p -> "• Assento " + p)
                .collect(java.util.stream.Collectors.joining("\n"));

        String corpo = String.format("""
            Olá, %s!
            
            Sua reserva em grupo foi cancelada.
            
            📍 Sala: %s
            📅 Data: %s
            🕐 Horário: %s às %s
            💺 Assentos cancelados:
            %s
            
            Se o cancelamento não foi feito por você, entre em contato com o suporte.
            
            Atenciosamente,
            Sistema de Gerenciamento de Salas
            """, nomeUsuario, nomeSala, data, horarioInicio, horarioFim, listaAssentos);

        enviar(emailDestino, assunto, corpo);
    }

    public void enviarRejeicaoReserva(String emailDestino, String nomeUsuario,
                                      String nomeSala, String data,
                                      String horarioInicio, String horarioFim,
                                      String motivo) {
        String assunto = "Reserva não confirmada — " + nomeSala;
        String corpo = String.format("""
            Olá, %s!
            
            Infelizmente sua reserva não pôde ser confirmada.
            
            📍 Sala: %s
            📅 Data: %s
            🕐 Horário: %s às %s
            
            ❌ Motivo: %s
            
            Tente novamente com outras datas ou horários disponíveis.
            
            Atenciosamente,
            Sistema de Gerenciamento de Salas
            """, nomeUsuario, nomeSala, data, horarioInicio, horarioFim, motivo);

        enviar(emailDestino, assunto, corpo);
    }
}


