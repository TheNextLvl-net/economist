package net.thenextlvl.economist.plugin.controller.data;

import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

final class DriverManagerDataSource implements DataSource {
    private final String jdbcUrl;
    private final @Nullable String username;
    private final @Nullable String password;

    DriverManagerDataSource(final String jdbcUrl) {
        this(jdbcUrl, null, null);
    }

    DriverManagerDataSource(final String jdbcUrl, @Nullable final String username, @Nullable final String password) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return username == null ? DriverManager.getConnection(jdbcUrl) : DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("DriverManager does not expose a parent logger");
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) return iface.cast(this);
        throw new SQLException("Not a wrapper for " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return iface.isInstance(this);
    }
}
