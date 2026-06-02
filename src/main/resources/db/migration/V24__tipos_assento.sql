CREATE TABLE tipos_assento (
                               id   SERIAL PRIMARY KEY,
                               nome VARCHAR(50) NOT NULL UNIQUE,
                               ativo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tipos_assento (nome) VALUES
                                     ('ESTACAO_PADRAO'),
                                     ('ESTACAO_EXECUTIVA'),
                                     ('SALA_REUNIAO_INDIVIDUAL'),
                                     ('POSICAO_ACESSIVEL'),
                                     ('HOT_DESK');