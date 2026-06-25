ALTER TABLE grupos
    ADD COLUMN IF NOT EXISTS lider_id INTEGER;

ALTER TABLE grupos
    ADD CONSTRAINT fk_grupos_lider
        FOREIGN KEY (lider_id) REFERENCES usuarios(id);

CREATE TABLE IF NOT EXISTS convites_grupo (
                                              id SERIAL PRIMARY KEY,
                                              grupo_id INTEGER NOT NULL REFERENCES grupos(id),
                                              email_convidado VARCHAR(255) NOT NULL,
                                              convidado_por_id INTEGER NOT NULL REFERENCES usuarios(id),
                                              status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
                                              criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              respondido_em TIMESTAMP
);