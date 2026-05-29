CREATE TABLE refresh_tokens (
                                id SERIAL PRIMARY KEY,
                                usuario_id INTEGER NOT NULL UNIQUE,
                                token VARCHAR(255) NOT NULL UNIQUE,
                                expiracao TIMESTAMP NOT NULL,
                                revogado BOOLEAN NOT NULL DEFAULT FALSE,
                                CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);