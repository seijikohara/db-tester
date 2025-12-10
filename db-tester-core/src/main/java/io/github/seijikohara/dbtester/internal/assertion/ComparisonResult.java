package io.github.seijikohara.dbtester.internal.assertion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.github.seijikohara.dbtester.api.domain.ColumnMetadata;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Collects and formats comparison failures for datasets and tables.
 *
 * <p>This class accumulates all comparison failures rather than failing on the first mismatch,
 * enabling users to see all issues at once. The output consists of a human-readable summary line
 * followed by valid YAML details that can be parsed by standard YAML libraries.
 *
 * <p>Example output format:
 *
 * <pre>{@code
 * Assertion failed: 3 differences in USERS, ORDERS
 * summary:
 *   status: FAILED
 *   total_differences: 3
 * tables:
 *   USERS:
 *     differences:
 *       - path: row_count
 *         expected: 3
 *         actual: 2
 *   ORDERS:
 *     differences:
 *       - path: "row[0].STATUS"
 *         expected: COMPLETED
 *         actual: PENDING
 *         column:
 *           type: VARCHAR
 *           nullable: true
 *       - path: "row[1].AMOUNT"
 *         expected: 100.00
 *         actual: 99.99
 *         column:
 *           type: "DECIMAL(10,2)"
 * }</pre>
 */
public final class ComparisonResult {

  /** YAML mapper configured for readable output. */
  private static final ObjectMapper YAML_MAPPER = createYamlMapper();

  /** Map of table names to their table results. */
  private final Map<String, TableResult> tableResults;

  /** Total count of all differences found. */
  private int totalDifferences;

  /** Creates a new empty comparison result. */
  public ComparisonResult() {
    this.tableResults = new LinkedHashMap<>();
    this.totalDifferences = 0;
  }

  /**
   * Creates the YAML ObjectMapper with appropriate settings.
   *
   * @return configured ObjectMapper for YAML output
   */
  private static ObjectMapper createYamlMapper() {
    final var factory =
        YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .build();
    return new ObjectMapper(factory).setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * Adds a table count mismatch.
   *
   * @param expected the expected table count
   * @param actual the actual table count
   */
  public void addTableCountMismatch(final int expected, final int actual) {
    addDifference(
        "(dataset)",
        new Difference("table_count", String.valueOf(expected), String.valueOf(actual), null));
  }

  /**
   * Adds a missing table error.
   *
   * @param tableName the name of the missing table
   */
  public void addMissingTable(final String tableName) {
    addDifference(tableName, new Difference("table", "exists", "not found", null));
  }

  /**
   * Adds a row count mismatch for a table.
   *
   * @param tableName the table name
   * @param expected the expected row count
   * @param actual the actual row count
   */
  public void addRowCountMismatch(final String tableName, final int expected, final int actual) {
    addDifference(
        tableName,
        new Difference("row_count", String.valueOf(expected), String.valueOf(actual), null));
  }

  /**
   * Adds a cell value mismatch.
   *
   * @param tableName the table name
   * @param rowIndex the row index (0-based)
   * @param columnName the column name
   * @param expected the expected value
   * @param actual the actual value
   */
  public void addValueMismatch(
      final String tableName,
      final int rowIndex,
      final String columnName,
      final @Nullable Object expected,
      final @Nullable Object actual) {
    addValueMismatch(tableName, rowIndex, columnName, expected, actual, null);
  }

  /**
   * Adds a cell value mismatch with column metadata.
   *
   * @param tableName the table name
   * @param rowIndex the row index (0-based)
   * @param columnName the column name
   * @param expected the expected value
   * @param actual the actual value
   * @param metadata the column metadata (nullable)
   */
  public void addValueMismatch(
      final String tableName,
      final int rowIndex,
      final String columnName,
      final @Nullable Object expected,
      final @Nullable Object actual,
      final @Nullable ColumnMetadata metadata) {
    final var path = String.format("row[%d].%s", rowIndex, columnName);
    final var columnInfo = createColumnInfo(metadata);
    addDifference(
        tableName, new Difference(path, formatValue(expected), formatValue(actual), columnInfo));
  }

  /**
   * Creates column info from metadata.
   *
   * @param metadata the column metadata
   * @return the column info or null
   */
  private @Nullable ColumnInfo createColumnInfo(final @Nullable ColumnMetadata metadata) {
    return Optional.ofNullable(metadata)
        .map(
            m -> {
              final var typeStr = formatJdbcType(m);
              return new ColumnInfo(typeStr, m.nullable(), m.primaryKey());
            })
        .orElse(null);
  }

  /**
   * Formats JDBC type with precision/scale.
   *
   * @param metadata the column metadata
   * @return the formatted type string
   */
  private String formatJdbcType(final ColumnMetadata metadata) {
    final var type =
        Optional.ofNullable(metadata.jdbcType()).map(JDBCType::getName).orElse("UNKNOWN");
    if (metadata.precision() > 0) {
      if (metadata.scale() > 0) {
        return String.format("%s(%d,%d)", type, metadata.precision(), metadata.scale());
      }
      return String.format("%s(%d)", type, metadata.precision());
    }
    return type;
  }

  /**
   * Adds a column missing error.
   *
   * @param tableName the table name
   * @param rowIndex the row index (0-based)
   * @param columnName the column name that is missing
   */
  public void addMissingColumn(
      final String tableName, final int rowIndex, final String columnName) {
    final var path = String.format("row[%d].%s", rowIndex, columnName);
    addDifference(tableName, new Difference(path, "exists", "column not found", null));
  }

  /**
   * Adds a difference to the result.
   *
   * @param tableName the table name
   * @param difference the difference to add
   */
  private void addDifference(final String tableName, final Difference difference) {
    tableResults.computeIfAbsent(tableName, k -> new TableResult()).differences().add(difference);
    totalDifferences++;
  }

  /**
   * Returns whether any differences were found.
   *
   * @return true if there are differences
   */
  public boolean hasDifferences() {
    return totalDifferences > 0;
  }

  /**
   * Returns the total number of differences found.
   *
   * @return the difference count
   */
  public int getDifferenceCount() {
    return totalDifferences;
  }

  /**
   * Formats all differences into valid YAML.
   *
   * <p>The output is valid YAML that can be parsed by standard YAML libraries. The structure
   * includes:
   *
   * <ul>
   *   <li>Summary with status and total differences
   *   <li>Tables grouped with their differences
   *   <li>Column metadata when available
   * </ul>
   *
   * @return the formatted YAML message
   */
  public String formatMessage() {
    if (!hasDifferences()) {
      return "No differences found";
    }

    final var builder = new StringBuilder();

    // Human-readable summary line
    builder.append(formatSummaryLine()).append(System.lineSeparator());

    // YAML details
    final var output =
        new YamlOutput(new Summary("FAILED", totalDifferences), new LinkedHashMap<>(tableResults));

    try {
      builder.append(YAML_MAPPER.writeValueAsString(output));
    } catch (final JsonProcessingException e) {
      // Fallback to simple format if YAML serialization fails
      builder.append(formatFallbackYaml());
    }

    return builder.toString().trim();
  }

  /**
   * Formats a human-readable summary line.
   *
   * @return the summary line
   */
  private String formatSummaryLine() {
    final var tableNames = String.join(", ", tableResults.keySet());
    return String.format(
        "Assertion failed: %d %s in %s",
        totalDifferences, totalDifferences == 1 ? "difference" : "differences", tableNames);
  }

  /**
   * Formats a fallback YAML-like message when Jackson serialization fails.
   *
   * @return the fallback YAML content
   */
  private String formatFallbackYaml() {
    final var builder = new StringBuilder();
    builder.append(String.format("summary:%n"));
    builder.append(String.format("  status: FAILED%n"));
    builder.append(String.format("  total_differences: %d%n", totalDifferences));
    builder.append(String.format("tables:%n"));

    tableResults.forEach(
        (tableName, result) -> {
          builder.append(String.format("  %s:%n", tableName));
          builder.append(String.format("    differences:%n"));
          result
              .differences()
              .forEach(
                  diff -> {
                    builder.append(String.format("      - path: %s%n", diff.path()));
                    builder.append(String.format("        expected: %s%n", diff.expected()));
                    builder.append(String.format("        actual: %s%n", diff.actual()));
                  });
        });

    return builder.toString();
  }

  /**
   * Throws an AssertionError if there are any differences.
   *
   * @throws AssertionError if differences exist
   */
  public void assertNoDifferences() {
    if (hasDifferences()) {
      throw new AssertionError(formatMessage());
    }
  }

  /**
   * Formats a value for YAML output.
   *
   * <p>Nulls show as "null", other values use their toString representation. YAML handles quoting
   * automatically.
   *
   * @param value the value to format
   * @return the formatted string representation
   */
  private String formatValue(final @Nullable Object value) {
    if (value == null) {
      return "null";
    }
    return value.toString();
  }

  /**
   * Root YAML output structure.
   *
   * @param summary the summary section
   * @param tables the tables with differences
   */
  @JsonPropertyOrder({"summary", "tables"})
  private record YamlOutput(Summary summary, Map<String, TableResult> tables) {}

  /**
   * Summary section of the YAML output.
   *
   * @param status the assertion status (FAILED/PASSED)
   * @param totalDifferences the total number of differences
   */
  @JsonPropertyOrder({"status", "total_differences"})
  private record Summary(String status, @JsonProperty("total_differences") int totalDifferences) {}

  /**
   * Result for a single table.
   *
   * @param differences the list of differences
   */
  private record TableResult(List<Difference> differences) {
    /** Creates a new table result with an empty difference list. */
    TableResult() {
      this(new ArrayList<>());
    }
  }

  /**
   * Represents a single difference between expected and actual values.
   *
   * @param path the path to the differing element (e.g., "row[0].NAME")
   * @param expected the expected value
   * @param actual the actual value
   * @param column the column metadata (nullable)
   */
  @JsonPropertyOrder({"path", "expected", "actual", "column"})
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private record Difference(
      String path, String expected, String actual, @Nullable ColumnInfo column) {}

  /**
   * Column metadata for inclusion in difference output.
   *
   * @param type the JDBC type with precision/scale
   * @param nullable whether the column allows null
   * @param primaryKey whether the column is a primary key
   */
  @JsonPropertyOrder({"type", "nullable", "primary_key"})
  @JsonInclude(JsonInclude.Include.NON_DEFAULT)
  private record ColumnInfo(
      String type, boolean nullable, @JsonProperty("primary_key") boolean primaryKey) {}
}
