package net.thenextlvl.economist.plugin.controller.data;

import net.thenextlvl.economist.plugin.EconomistPlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgreSQLController extends SQLController {
    public PostgreSQLController(final DataSource dataSource, final EconomistPlugin plugin) throws SQLException {
        super(dataSource, plugin, DatabaseDialect.POSTGRESQL, PostgreSqlDatabase.get());
    }

    public PostgreSQLController(final Connection connection, final EconomistPlugin plugin) throws SQLException {
        this(new SingleConnectionDataSource(connection), plugin);
    }
}
