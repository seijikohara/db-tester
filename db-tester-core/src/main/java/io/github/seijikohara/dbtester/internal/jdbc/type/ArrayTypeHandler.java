package io.github.seijikohara.dbtester.internal.jdbc.type;

import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

/**
 * Type handler for SQL ARRAY values.
 *
 * <p>This handler reads and writes SQL arrays as Java Object arrays. It supports PostgreSQL and
 * other databases with native array types.
 *
 * <p>When reading, the JDBC Array is converted to a Java Object array. When writing, the Java array
 * is converted back to a JDBC Array using the connection's createArrayOf method.
 *
 * <p>For string serialization, arrays are formatted as comma-separated values enclosed in curly
 * braces (PostgreSQL array literal syntax): {@code {value1,value2,value3}}
 */
public final class ArrayTypeHandler implements TypeHandler<Object[]> {

  /** SQL types supported by this handler. */
  private static final List<Integer> SUPPORTED_SQL_TYPES = List.of(Types.ARRAY);

  /** Databases with native ARRAY support. */
  private static final List<String> SUPPORTED_DATABASES = List.of("PostgreSQL", "H2");

  /** Creates a new array type handler. */
  public ArrayTypeHandler() {}

  @Override
  @SuppressWarnings("unchecked")
  public Class<Object[]> getJavaType() {
    return Object[].class;
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
  public Object @Nullable [] read(final ResultSet resultSet, final int columnIndex)
      throws SQLException {
    final var sqlArray = resultSet.getArray(columnIndex);
    if (sqlArray == null || resultSet.wasNull()) {
      return null;
    }

    try {
      final var javaArray = sqlArray.getArray();
      if (javaArray instanceof Object[] objectArray) {
        return objectArray;
      }
      // Handle primitive arrays by boxing
      return boxPrimitiveArray(javaArray);
    } finally {
      sqlArray.free();
    }
  }

  @Override
  public void write(
      final PreparedStatement preparedStatement, final int parameterIndex, final Object[] value)
      throws SQLException {
    // Determine the SQL type name for the array elements
    final var typeName = inferTypeName(value);
    final var connection = preparedStatement.getConnection();
    final Array sqlArray = connection.createArrayOf(typeName, value);
    try {
      preparedStatement.setArray(parameterIndex, sqlArray);
    } catch (final SQLException e) {
      sqlArray.free();
      throw e;
    }
  }

  @Override
  public String format(final Object[] value) {
    // Format as PostgreSQL array literal: {value1,value2,value3}
    final var elements =
        Arrays.stream(value).map(this::formatElement).collect(Collectors.joining(","));
    return "{" + elements + "}";
  }

  @Override
  public Object[] parse(final String value) {
    final var trimmed = value.trim();

    // Handle PostgreSQL array literal syntax: {value1,value2,value3}
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
      final var content = trimmed.substring(1, trimmed.length() - 1);
      if (content.isEmpty()) {
        return new Object[0];
      }
      return parseArrayContent(content);
    }

    // Handle comma-separated values
    if (trimmed.contains(",")) {
      return Arrays.stream(trimmed.split(",")).map(String::trim).toArray(Object[]::new);
    }

    // Single element
    return new Object[] {trimmed};
  }

  /**
   * Converts a primitive array to an Object array.
   *
   * @param primitiveArray the primitive array
   * @return the boxed Object array
   */
  private Object[] boxPrimitiveArray(final Object primitiveArray) {
    if (primitiveArray instanceof int[] intArray) {
      return Arrays.stream(intArray).boxed().toArray();
    }
    if (primitiveArray instanceof long[] longArray) {
      return Arrays.stream(longArray).boxed().toArray();
    }
    if (primitiveArray instanceof double[] doubleArray) {
      return Arrays.stream(doubleArray).boxed().toArray();
    }
    if (primitiveArray instanceof boolean[] boolArray) {
      final var result = new Object[boolArray.length];
      for (var i = 0; i < boolArray.length; i++) {
        result[i] = boolArray[i];
      }
      return result;
    }
    // Fallback: wrap in single-element array
    return new Object[] {primitiveArray};
  }

  /**
   * Infers the SQL type name for array elements.
   *
   * @param array the array to inspect
   * @return the SQL type name
   */
  private String inferTypeName(final Object[] array) {
    if (array.length == 0) {
      return "varchar"; // Default for empty arrays
    }

    final var firstElement = array[0];
    if (firstElement == null) {
      return "varchar";
    }

    return switch (firstElement) {
      case Integer i -> "integer";
      case Long l -> "bigint";
      case Double d -> "double precision";
      case Float f -> "real";
      case Boolean b -> "boolean";
      case String s -> "varchar";
      default -> "varchar";
    };
  }

  /**
   * Formats a single array element for string output.
   *
   * @param element the element to format
   * @return the formatted string
   */
  private String formatElement(final @Nullable Object element) {
    if (element == null) {
      return "NULL";
    }
    if (element instanceof String strValue) {
      // Escape quotes and wrap in double quotes if contains special characters
      if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("{")) {
        return "\"" + strValue.replace("\"", "\\\"") + "\"";
      }
      return strValue;
    }
    return element.toString();
  }

  /**
   * Parses the content of a PostgreSQL array literal.
   *
   * @param content the array content without braces
   * @return the parsed array elements
   */
  private Object[] parseArrayContent(final String content) {
    // Simple parsing for non-nested arrays
    final var elements = new java.util.ArrayList<Object>();
    final var current = new StringBuilder();
    var inQuotes = false;
    var escaped = false;

    for (var i = 0; i < content.length(); i++) {
      final var c = content.charAt(i);

      if (escaped) {
        current.append(c);
        escaped = false;
        continue;
      }

      if (c == '\\') {
        escaped = true;
        continue;
      }

      if (c == '"') {
        inQuotes = !inQuotes;
        continue;
      }

      if (c == ',' && !inQuotes) {
        elements.add(parseElement(current.toString()));
        current.setLength(0);
        continue;
      }

      current.append(c);
    }

    // Add the last element
    if (!current.isEmpty()) {
      elements.add(parseElement(current.toString()));
    }

    return elements.toArray();
  }

  /**
   * Parses a single array element value.
   *
   * @param element the element string
   * @return the parsed value (null for "NULL", otherwise the string value)
   */
  @SuppressWarnings("NullAway")
  private @Nullable Object parseElement(final String element) {
    final var trimmed = element.trim();
    if ("NULL".equalsIgnoreCase(trimmed)) {
      return null;
    }
    return trimmed;
  }
}
