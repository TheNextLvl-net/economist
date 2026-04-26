package net.thenextlvl.economist.plugin.controller.data;

import de.chojo.sadu.core.jdbc.JdbcConfig;

final class PostgreSqlJdbcConfig extends JdbcConfig<PostgreSqlJdbcConfig> {
    @Override
    protected String defaultDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    protected String driver() {
        return "postgresql";
    }

    @Override
    protected String baseUrl() {
        return "jdbc:postgresql://";
    }
}
