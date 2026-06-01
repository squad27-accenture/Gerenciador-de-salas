CREATE TABLE disponibilidade_sala (
                                      id SERIAL PRIMARY KEY,
                                      sala_id INTEGER NOT NULL REFERENCES salas(id),
                                      dia_semana VARCHAR(20) NOT NULL,
                                      aceita_reservas BOOLEAN NOT NULL DEFAULT true,
                                      horario_abertura TIME,
                                      horario_fechamento TIME,
                                      antecedencia_minima_dias INTEGER NOT NULL DEFAULT 0,
                                      UNIQUE (sala_id, dia_semana)
);