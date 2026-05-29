ALTER TABLE password_reset_tokens
    ADD COLUMN email VARCHAR(255) NOT NULL;