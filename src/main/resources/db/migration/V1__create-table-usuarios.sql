CREATE TABLE usuarios(
                        id SERIAL PRIMARY KEY,
                        email TEXT NOT NULL UNIQUE,
                        senha TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE,
                        role TEXT NOT NULL
);