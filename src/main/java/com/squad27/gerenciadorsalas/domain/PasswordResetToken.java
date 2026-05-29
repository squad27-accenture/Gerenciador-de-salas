package com.squad27.gerenciadorsalas.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private String email;
        private String codigo;
        @Column(name = "expiration_date")
        private LocalDateTime expiracao;
        private boolean usado;
    }

