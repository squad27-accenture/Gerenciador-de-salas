CREATE TABLE assentos(
    id SERIAL PRIMARY KEY,
    equipamento_assento TEXT,
    posicao INT NOT NULL,
    Sala_id INT NOT NULL,

    CONSTRAINT fk_sala_assento
        FOREIGN KEY (sala_id)
        REFERENCES salas(id)
)