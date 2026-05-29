CREATE TABLE password_reset_tokens (
                                       id BIGSERIAL PRIMARY KEY,
                                       token VARCHAR(255) NOT NULL,
                                       usuario_id BIGINT NOT NULL,
                                       expiration_date TIMESTAMP NOT NULL,

                                       CONSTRAINT fk_usuario
                                           FOREIGN KEY (usuario_id)
                                               REFERENCES usuarios(id)
                                               ON DELETE CASCADE
);