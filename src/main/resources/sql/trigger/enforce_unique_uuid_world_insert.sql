CREATE TRIGGER IF NOT EXISTS enforce_unique_uuid_world
    BEFORE INSERT
    ON accounts
    FOR EACH ROW
    WHEN NEW.world IS NULL
BEGIN
    SELECT RAISE(ABORT, 'Cannot insert another row with NULL world for the same uuid')
    WHERE EXISTS (SELECT 1
                  FROM accounts
                  WHERE uuid = NEW.uuid AND world IS NULL);
END;