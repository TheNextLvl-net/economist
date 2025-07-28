CREATE TRIGGER IF NOT EXISTS enforce_unique_account_uuid_world_update
    BEFORE UPDATE
    ON accounts
    FOR EACH ROW
    WHEN NEW.world IS NULL
BEGIN
    SELECT RAISE(ABORT, 'Cannot update to a row with NULL world for the same uuid')
    WHERE EXISTS (SELECT 1 FROM accounts WHERE uuid = NEW.uuid AND world IS NULL AND rowid != OLD.rowid);
END;