CREATE TABLE sala_equipamentos (
                                   sala_id INT NOT NULL,
                                   equipamento VARCHAR(100) NOT NULL,

                                   CONSTRAINT fk_sala_equipamentos
                                       FOREIGN KEY (sala_id)
                                           REFERENCES salas(id)
                                           ON DELETE CASCADE
);