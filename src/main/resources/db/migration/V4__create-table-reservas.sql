CREATE TABLE reservas
(
    id SERIAL PRIMARY KEY,
    horario_inicio time not null ,
    horario_fim time not null ,
    data_reserva date NOT NULL,
    posicao INT NOT NULL,
    Sala_id INT NOT NULL,
    usuario_id INT,
    status_reserva TEXT NOT NULL ,
    codigo_grupo VARCHAR(100),

    CONSTRAINT fk_sala_reservas
    FOREIGN KEY (sala_id)
    REFERENCES salas(id),

    CONSTRAINT fk_usuarios_reservas
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
)