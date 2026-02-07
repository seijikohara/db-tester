package io.github.seijikohara.dbtester.internal.jdbc.type;

import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Type handler for UUID values.
 *
 * <p>This handler supports UUID columns in PostgreSQL and other databases that support UUID types.
 * UUIDs are read as objects and written using setObject with Types.OTHER.
 *
 * <p>For databases that store UUIDs as strings (VARCHAR), this handler will convert between UUID
 * objects and their string representations.
 */
public final class UuidTypeHandler implements TypeHandler<UUID> {

  /** SQL types supported by this handler. */
  private static final List<Integer> SUPPORTED_SQL_TYPES = List.of(Types.OTHER);

  /** Databases with native UUID support. */
  private static final List<String> SUPPORTED_DATABASES = List.of("PostgreSQL", "H2");

  /** Creates a new UUID type handler. */
  public UuidTypeHandler() {}

  @Override
  public Class<UUID> getJavaType() {
    return UUID.class;
  }

  @Override
  public List<Integer> getSqlTypes() {
    return SUPPORTED_SQL_TYPES;
  }

  @Override
  public List<String> getSupportedDatabases() {
    return SUPPORTED_DATABASES;
  }

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  @SuppressWarnings("NullAway")
  public @Nullable UUID read(final ResultSet resultSet, final int columnIndex) throws SQLException {
    final var value = resultSet.getObject(columnIndex);
    if (value == null || resultSet.wasNull()) {
      return null;
    }

    if (value instanceof UUID uuid) {
      return uuid;
    }

    // Handle string representation
    if (value instanceof String strValue) {
      return UUID.fromString(strValue);
    }

    throw new SQLException(
        String.format("Cannot convert value of type %s to UUID", value.getClass().getName()));
  }

  @Override
  public void write(
      final PreparedStatement preparedStatement, final int parameterIndex, final UUID value)
      throws SQLException {
    preparedStatement.setObject(parameterIndex, value, Types.OTHER);
  }

  @Override
  public String format(final UUID value) {
    return value.toString();
  }

  @Override
  public UUID parse(final String value) {
    try {
      return UUID.fromString(value.trim());
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(String.format("Invalid UUID format: %s", value), e);
    }
  }
}
