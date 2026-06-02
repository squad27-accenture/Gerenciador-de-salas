CREATE TABLE auditoria (
                           id          SERIAL PRIMARY KEY,
                           operacao    VARCHAR(50)  NOT NULL,
                           entidade    VARCHAR(50)  NOT NULL,
                           entidade_id VARCHAR(50),
                           usuario     VARCHAR(255),
                           detalhes    TEXT,
                           criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);