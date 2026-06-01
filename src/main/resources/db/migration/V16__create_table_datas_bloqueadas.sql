CREATE TABLE datas_bloqueadas (
                                  id SERIAL PRIMARY KEY,
                                  sala_id INTEGER NOT NULL REFERENCES salas(id),
                                  data DATE NOT NULL,
                                  motivo VARCHAR(255),
                                  UNIQUE (sala_id, data)
);