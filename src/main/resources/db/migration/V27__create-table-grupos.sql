CREATE TABLE grupos (
                        id SERIAL PRIMARY KEY,
                        nome VARCHAR(120) NOT NULL,
                        descricao VARCHAR(500),
                        ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE grupo_usuarios (
                                grupo_id INT NOT NULL,
                                usuario_id INT NOT NULL,

                                PRIMARY KEY (grupo_id, usuario_id),

                                FOREIGN KEY (grupo_id) REFERENCES grupos(id),
                                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
);