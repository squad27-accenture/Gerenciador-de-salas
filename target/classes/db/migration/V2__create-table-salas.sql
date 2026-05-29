CREATE TABLE salas(
    id SERIAL PRIMARY KEY,
    nome VARCHAR(130) UNIQUE NOT NULL,
    capacidade INT NOT NULL,
    equipamentos_sala TEXT NOT NULL,
    status TEXT NOT NULL ,
    local TEXT NOT NULL,
    cidade TEXT NOT NULL,
    estado TEXT NOT NULL
)