
ALTER TABLE assentos DROP COLUMN IF EXISTS equipamento_assento;


ALTER TABLE assentos
    ADD COLUMN tipo_assento VARCHAR(50),
    ADD COLUMN coordenada_x DOUBLE PRECISION,
    ADD COLUMN coordenada_y DOUBLE PRECISION,
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;


CREATE TABLE assento_equipamentos (
                                      assento_id INT NOT NULL,
                                      equipamento VARCHAR(50) NOT NULL,

                                      CONSTRAINT fk_assento_equipamento
                                          FOREIGN KEY (assento_id) REFERENCES assentos(id)
);