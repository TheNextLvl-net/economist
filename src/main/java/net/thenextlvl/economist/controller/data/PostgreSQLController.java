package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.jspecify.annotations.NullMarked;

import java.sql.Connection;
import java.sql.SQLException;

@NullMarked
public class PostgreSQLController extends SQLController {
    public PostgreSQLController(Connection connection, EconomistPlugin plugin) throws SQLException {
        super(connection, plugin);
    }

    @Override
    protected void createAccountTable() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                  uuid TEXT NOT NULL,
                  balance DECIMAL(65, 20) NOT NULL,
                  world TEXT NULL
                )
                """);
        executeUpdate("CREATE UNIQUE INDEX unique_uuid_world ON accounts(uuid, world)");
        executeUpdate("CREATE UNIQUE INDEX unique_uuid_null_world ON accounts(uuid) WHERE world IS NULL");
    }
}
