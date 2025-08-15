package net.thenextlvl.economist.controller.data;

import net.thenextlvl.economist.EconomistPlugin;
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
    private final @NonNull Connection connection;

    public SQLController(@NonNull Connection connection, @NonNull EconomistPlugin plugin) throws SQLException {
        this.connection = connection;
        connection.setAutoCommit(false);
        this.plugin = plugin;
        migrateDatabase();
        setupDatabase();
    }

    protected abstract void setupDatabase() throws SQLException;

    protected void migrateDatabase() throws SQLException {
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected <T> T executeQuery(@NonNull String sql, @NonNull ThrowingFunction<ResultSet, T> mapper, Object... parameters) throws SQLException {
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
    protected int[] executeBatchUpdate(String sql, @Nullable Object[]... batches) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (var parameters : batches) {
                for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                statement.addBatch();
            }
            var result = statement.executeBatch();
            connection.commit();
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            connection.rollback();
            throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Failed to execute SQL statement: " + sql, e);
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int[][] executeBatchUpdates(@NonNull String[] statements, @Nullable Object[]... batches) throws SQLException {
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
                connection.rollback();
                throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Failed to execute SQL statement: " + sql, e);
            }
        }
        connection.commit();
        return results;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int executeUpdate(@NonNull String sql, Object... parameters) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
            var result = statement.executeUpdate();
            connection.commit();
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            connection.rollback();
            throw new SQLException("Invalid number of parameters for SQL statement: " + sql, e);
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Failed to execute SQL statement: " + sql, e);
        }
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    protected int[] executeUpdates(@NonNull String[] statements, Object... parameters) throws SQLException {
        var results = new int[statements.length];
        for (var index = 0; index < statements.length; index++) {
            var sql = statements[index];
            try (var statement = connection.prepareStatement(sql)) {
                for (var i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
                var result = statement.executeUpdate();
                results[index] = result;
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Failed to execute SQL statement: " + sql, e);
            }
        }
        connection.commit();
        return results;
    }

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
