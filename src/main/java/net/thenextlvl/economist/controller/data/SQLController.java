package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public abstract class SQLController implements AutoCloseable, DataController {
    protected final @NonNull EconomistPlugin plugin;
    private final @NonNull Executor executor = new Executor();
    private final @NonNull Connection connection;

    public SQLController(@NonNull Connection connection, @NonNull EconomistPlugin plugin) throws SQLException {
        this.connection = connection;
        connection.setAutoCommit(false);
        this.plugin = plugin;
        startTransaction(executor -> {
            migrateDatabase(executor);
            setupDatabase(executor);
            return null;
        });
    }

    protected abstract void setupDatabase(Executor executor) throws SQLException;

    protected void migrateDatabase(Executor executor) throws SQLException {
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    protected <V> V startTransaction(@NonNull Transaction<V> transaction) throws SQLException {
        try {
            var call = transaction.execute(executor);
            connection.commit();
            return call;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    @FunctionalInterface
    protected interface Transaction<V> {
        @Nullable
        V execute(@NonNull Executor executor) throws SQLException;
    }

    protected class Executor {
        @SuppressWarnings("SqlSourceToSinkFlow")
        protected <T> T query(@NonNull String sql, @NonNull ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
            try (var statement = connection.prepareStatement(sql)) {
                for (var i = 0; i < parameters.length; i++)
                    statement.setObject(i + 1, parameters[i]);
                try (var resultSet = statement.executeQuery()) {
                    return mapper.apply(resultSet);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
            }
        }

        @SuppressWarnings("SqlSourceToSinkFlow")
        protected int[] batchUpdate(String sql, @Nullable Object[]... batches) throws SQLException {
            try (var statement = connection.prepareStatement(sql)) {
                for (var parameters : batches) {
                    for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                    statement.addBatch();
                }
                return statement.executeBatch();
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
            } catch (SQLException e) {
                throw new SQLException("Failed to execute SQL statement: " + sql, e);
            }
        }

        @SuppressWarnings("SqlSourceToSinkFlow")
        protected int[][] batchUpdates(@NonNull String[] statements, @Nullable Object[]... batches) throws SQLException {
            var results = new int[statements.length][];
            for (var index = 0; index < statements.length; index++) {
                var sql = statements[index];
                try (var statement = connection.prepareStatement(sql)) {
                    for (var parameters : batches) {
                        for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                        statement.addBatch();
                    }
                    var result = statement.executeBatch();
                    results[index] = result;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
                } catch (SQLException e) {
                    throw new SQLException("Failed to execute SQL statement: " + sql, e);
                }
            }
            return results;
        }

        @SuppressWarnings("SqlSourceToSinkFlow")
        protected int update(@NonNull String sql, Object... parameters) throws SQLException {
            try (var statement = connection.prepareStatement(sql)) {
                for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                return statement.executeUpdate();
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
            } catch (SQLException e) {
                throw new SQLException("Failed to execute SQL statement: " + sql, e);
            }
        }

        @SuppressWarnings("SqlSourceToSinkFlow")
        protected int[] updates(@NonNull String[] statements, Object... parameters) throws SQLException {
            var results = new int[statements.length];
            for (var index = 0; index < statements.length; index++) {
                var sql = statements[index];
                try (var statement = connection.prepareStatement(sql)) {
                    for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                    results[index] = statement.executeUpdate();
                } catch (SQLException e) {
                    throw new SQLException("Failed to execute SQL statement: " + sql, e);
                }
            }
            return results;
        }
    }

    @Contract(pure = true)
    protected static @NonNull String[] statements(@NonNull String file) {
        var stripped = statement(file)
                .replaceAll("(?s)\\s*/\\*.*?\\*/\\s*", "") // remove /* ... */ block comments
                .replaceAll("(?m)--.*$", ""); // remove -- line comments

        var statements = new ArrayList<String>();
        for (var statement : stripped.split(";")) {
            if (!statement.isBlank()) statements.add(statement);
        }
        return statements.toArray(new String[0]);
    }

    @Contract(pure = true)
    protected static @NonNull String statement(@NonNull String file) {
        try (var resource = SQLController.class.getClassLoader().getResourceAsStream(file)) {
            if (resource == null) throw new FileNotFoundException("Resource not found: " + file);
            try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    protected interface ThrowingFunction<T, R> {
        @Nullable
        R apply(@NonNull T t) throws SQLException;
    }
}
