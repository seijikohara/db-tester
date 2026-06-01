package io.github.seijikohara.dbtester.internal.jdbc.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Type handler for JSON/JSONB values in PostgreSQL.
 *
 * <p>This handler reads and writes JSON data as Jackson {@link JsonNode} objects. PostgreSQL stores
 * JSON and JSONB columns as Types.OTHER, which this handler supports.
 *
 * <p>When reading, JSON strings from the database are parsed into JsonNode trees. When writing, the
 * JsonNode is serialized to a JSON string and set using setObject with Types.OTHER.
 */
public final class JsonTypeHandler implements TypeHandler<JsonNode> {

  /** SQL types supported by this handler. */
  private static final List<Integer> SUPPORTED_SQL_TYPES = List.of(Types.OTHER);

  /** Databases with native JSON support handled by this handler. */
  private static final List<String> SUPPORTED_DATABASES = List.of("PostgreSQL");

  /** Jackson ObjectMapper for JSON parsing and serialization. */
  private final ObjectMapper objectMapper;

  /** Creates a new JSON type handler with a default ObjectMapper. */
  public JsonTypeHandler() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Creates a new JSON type handler with a custom ObjectMapper.
   *
   * @param objectMapper the ObjectMapper to use
   */
  JsonTypeHandler(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Class<JsonNode> javaType() {
    return JsonNode.class;
  }

  @Override
  public List<Integer> sqlTypes() {
    return SUPPORTED_SQL_TYPES;
  }

  @Override
  public List<String> supportedDatabases() {
    return SUPPORTED_DATABASES;
  }

  @Override
  public int priority() {
    return 20; // Higher priority than UuidTypeHandler for PostgreSQL
  }

  @Override
  @SuppressWarnings("NullAway")
  public @Nullable JsonNode read(final ResultSet resultSet, final int columnIndex)
      throws SQLException {
    final var value = resultSet.getString(columnIndex);
    if (value == null || resultSet.wasNull()) {
      return null;
    }

    try {
      return objectMapper.readTree(value);
    } catch (final JsonProcessingException e) {
      throw new SQLException(String.format("Failed to parse JSON value: %s", value), e);
    }
  }

  @Override
  public void write(
      final PreparedStatement preparedStatement, final int parameterIndex, final JsonNode value)
      throws SQLException {
    try {
      final var jsonString = objectMapper.writeValueAsString(value);
      // PostgreSQL requires using setObject with Types.OTHER for JSON/JSONB columns
      preparedStatement.setObject(parameterIndex, jsonString, Types.OTHER);
    } catch (final JsonProcessingException e) {
      throw new SQLException("Failed to serialize JSON value", e);
    }
  }

  @Override
  public String format(final JsonNode value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (final JsonProcessingException e) {
      throw new IllegalStateException("Failed to format JSON value", e);
    }
  }

  @Override
  public JsonNode parse(final String value) {
    try {
      return objectMapper.readTree(value);
    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException(String.format("Invalid JSON format: %s", value), e);
    }
  }
}
