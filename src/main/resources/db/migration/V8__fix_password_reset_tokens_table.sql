ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS expiracao TIMESTAMP;
ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS usado BOOLEAN DEFAULT FALSE;
ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(255);
ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS email VARCHAR(255);