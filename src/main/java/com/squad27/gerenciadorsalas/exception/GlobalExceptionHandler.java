package com.squad27.gerenciadorsalas.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("erro", ex.getReason()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String mensagem = "Valor inválido no corpo da requisição.";

        if (ex.getMessage() != null && ex.getMessage().contains("StatusSala")) {
            mensagem = "Status inválido. Valores aceitos: DISPONIVEL, INDISPONIVEL, MANUTENCAO.";
        }

        return ResponseEntity.status(400).body(Map.of("erro", mensagem));
    }
}
