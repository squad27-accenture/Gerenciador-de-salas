package com.squad27.gerenciadorsalas.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            erros.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.status(400).body(erros);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("status", ex.getStatusCode().value());
        corpo.put("erro", ex.getReason());
        corpo.put("codigo", resolverCodigo(ex.getReason()));
        return ResponseEntity.status(ex.getStatusCode()).body(corpo);
    }

    private String resolverCodigo(String motivo) {
        if (motivo == null) return "ERRO_DESCONHECIDO";
        if (motivo.contains("já está reservado"))       return "ASSENTO_OCUPADO";
        if (motivo.contains("proximidade OBRIGATÓRIA")) return "PROXIMIDADE_INSUFICIENTE";
        if (motivo.contains("tipos preferidos"))        return "TIPO_ASSENTO_INDISPONIVEL";
        if (motivo.contains("assentos disponíveis"))    return "SEM_ASSENTOS_LIVRES";
        if (motivo.contains("disponível neste horário")) return "SALA_INDISPONIVEL_HORARIO";
        if (motivo.contains("data bloqueada"))          return "DATA_BLOQUEADA";
        if (motivo.contains("horário inicial"))         return "HORARIO_INVALIDO";
        return "REJEICAO_RESERVA";
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String mensagem = "Valor inválido no corpo da requisição.";

        if (ex.getMessage() != null && ex.getMessage().contains("StatusSala")) {
            mensagem = "Status inválido. Valores aceitos: DISPONIVEL, INDISPONIVEL, MANUTENCAO.";
        }

        return ResponseEntity.status(400).body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(404).body(Map.of("erro", ex.getMessage()));
    }
}
