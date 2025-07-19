package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

@NullMarked
public class PostgreSQLController extends SQLController {
    // todo: implement
    public PostgreSQLController(Connection connection, EconomistPlugin plugin) throws SQLException {
        super(connection, plugin);
    }

    @Override
    public int prune(Duration duration, @Nullable World world) {
        return 0;
    }

    @Override
    protected void setupDatabase() throws SQLException {
    }
}
