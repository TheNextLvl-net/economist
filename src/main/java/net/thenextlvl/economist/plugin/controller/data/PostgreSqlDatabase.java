package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.core.databases.DefaultDatabase;
import de.chojo.sadu.core.updater.StatementSplitter;
import de.chojo.sadu.core.updater.UpdaterBuilder;
import de.chojo.sadu.updater.BaseSqlUpdaterBuilder;

final class PostgreSqlDatabase implements DefaultDatabase<PostgreSqlJdbcConfig, BaseSqlUpdaterBuilder<PostgreSqlJdbcConfig, ?>> {
    private static final PostgreSqlDatabase TYPE = new PostgreSqlDatabase();

    private PostgreSqlDatabase() {
    }

    static PostgreSqlDatabase get() {
        return TYPE;
    }

    @Override
    public String name() {
        return "postgresql";
    }

    @Override
    public String[] alias() {
        return new String[]{"postgres"};
    }

    @Override
    public PostgreSqlJdbcConfig jdbcBuilder() {
        return new PostgreSqlJdbcConfig();
    }

    @Override
    public String[] splitStatements(final String queries) {
        return StatementSplitter.split(queries);
    }

    @Override
    public UpdaterBuilder<PostgreSqlJdbcConfig, BaseSqlUpdaterBuilder<PostgreSqlJdbcConfig, ?>> newSqlUpdaterBuilder() {
        return new BaseSqlUpdaterBuilder<>(this);
    }
}
