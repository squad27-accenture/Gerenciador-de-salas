CREATE TABLE salas(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(130) NOT NULL,
    capacidade INT NOT NULL,
    status TEXT not null,
    local TEXT NOT NULL
)