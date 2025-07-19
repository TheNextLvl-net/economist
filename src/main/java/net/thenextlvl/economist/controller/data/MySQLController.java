package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.jspecify.annotations.NullMarked;

import java.sql.DriverManager;
import java.sql.SQLException;

@NullMarked
public class MySQLController extends SQLController {
    public MySQLController(EconomistPlugin plugin) throws SQLException {
        super(DriverManager.getConnection(
                "jdbc:mysql://" + plugin.config.database.url,
                plugin.config.database.user,
                plugin.config.database.password
        ), plugin);
    }

    @Override
    protected void createAccountTable() throws SQLException {
        executeUpdate("""
                CREATE TABLE IF NOT EXISTS accounts (
                  uuid VARCHAR(36) NOT NULL,
                  balance DECIMAL(65, 20) NOT NULL,
                  world VARCHAR(255) NULL,
                  UNIQUE KEY unique_uuid_world (uuid, world)
                )
                """);
        executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS unique_uuid_null_world ON accounts(uuid) WHERE world IS NULL");
    }
}