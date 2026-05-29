package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates both annotation-based and programmatic database validation approaches.
 *
 * <p>This test class illustrates two complementary validation strategies:
 *
 * <ul>
 *   <li><strong>Annotation-based validation</strong> using {@code @ExpectedDataSet} - suitable for
 *       standard table comparisons with convention-based expected data
 *   <li><strong>Programmatic validation</strong> using custom SQL queries - provides flexibility
 *       for complex scenarios where annotation-based testing is insufficient
 * </ul>
 *
 * <p>Key programmatic API features:
 *
 * <ul>
 *   <li>{@link DatabaseQueryAssertion#assertEqualsByQuery} - Compare expected data against SQL
 *       query results
 *   <li>{@link DatabaseAssertion#assertEquals} - Compare two datasets or tables directly
 *   <li>{@link DatabaseAssertion#assertEqualsIgnoreColumns} - Compare datasets ignoring specific
 *       columns
 * </ul>
 *
 * <p>Programmatic assertions are useful for custom SQL queries, dynamic column filtering, mid-test
 * state verification, or comparing multiple dataset sources.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ProgrammaticAssertionApiTest")
final class ProgrammaticAssertionApiTest {

  /** Logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ProgrammaticAssertionApiTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ProgrammaticAssertionApiTest instance. */
  ProgrammaticAssertionApiTest() {}

  /**
   * Sets up H2 in-memory database and schema.
   *
   * @param context extension context
   * @throws Exception if setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up database for ProgrammaticAssertionApiTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ProgrammaticAssertionApiTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ProgrammaticAssertionApiTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  /**
   * Executes SQL script from classpath.
   *
   * @param dataSource target DataSource
   * @param scriptPath classpath resource path
   * @throws Exception if execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(
                ProgrammaticAssertionApiTest.class.getClassLoader().getResource(scriptPath))
            .orElseThrow(
                () -> new IllegalStateException(String.format("Script not found: %s", scriptPath)));

    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement();
        final var inputStream = resource.openStream()) {
      final var sql = new String(inputStream.readAllBytes(), UTF_8);
      Arrays.stream(sql.split(";"))
          .map(String::trim)
          .filter(Predicate.not(String::isEmpty))
          .forEach(
              trimmed -> {
                try {
                  statement.execute(trimmed);
                } catch (final SQLException e) {
                  throw new RuntimeException(
                      String.format("Failed to execute SQL: %s", trimmed), e);
                }
              });
    }
  }

  /**
   * Executes a SQL statement against the test database.
   *
   * @param sql the SQL statement to execute
   */
  private static void executeSql(final String sql) {
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    } catch (final SQLException e) {
      throw new RuntimeException(String.format("Failed to execute SQL: %s", sql), e);
    }
  }

  /**
   * Fetches the current database state as a {@link Table} by running a query and projecting the
   * supplied columns.
   *
   * <p>This helper shows how callers obtain an actual {@link Table} from the live database when
   * comparing it against an expected {@link Table} via {@link DatabaseAssertion#assertEquals} or
   * {@link DatabaseAssertion#assertEqualsIgnoreColumns}. The query result is wrapped in a {@link
   * Table} keyed by the requested column names so that comparison aligns column-by-column.
   *
   * @param tableName the logical table name attached to the result
   * @param sqlQuery the SQL query that returns the actual rows
   * @param columnNames the columns to project, in the order they appear in the query
   * @return a {@link Table} containing the rows returned by the query
   */
  private static Table fetchTable(
      final String tableName, final String sqlQuery, final List<String> columnNames) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    try (final var connection = dataSource.getConnection();
        final var statement = connection.prepareStatement(sqlQuery);
        final var resultSet = statement.executeQuery()) {
      final var rows = new ArrayList<Row>();
      while (resultSet.next()) {
        final Map<ColumnName, CellValue> rowValues = new LinkedHashMap<>();
        for (var i = 0; i < columns.size(); i++) {
          final var raw = resultSet.getObject(i + 1);
          rowValues.put(columns.get(i), raw == null ? CellValue.NULL : new CellValue(raw));
        }
        rows.add(Row.of(rowValues));
      }
      return Table.of(new TableName(tableName), columns, List.copyOf(rows));
    } catch (final SQLException e) {
      throw new RuntimeException(String.format("Failed to fetch table: %s", sqlQuery), e);
    }
  }

  /**
   * Demonstrates basic programmatic assertion without an {@code @ExpectedDataSet} annotation.
   *
   * <p>The expected table is built in code, the actual table is fetched from the database, and
   * verification is performed by {@link DatabaseAssertion#assertEquals}. This proves that
   * programmatic validation runs against the real database state rather than the framework's
   * annotation-driven mechanism.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
   *   <li>Execution: Inserts (3,Value3,300,NULL)
   *   <li>Expectation: Builds an expected table in code, fetches the actual table from the
   *       database, and compares with {@link DatabaseAssertion#assertEquals}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should demonstrate basic programmatic API for database validation")
  @DataSet
  void shouldDemonstrateBasicProgrammaticAPI() {
    // Given
    logger.info("Running programmatic API demonstration");

    // When
    executeSql(
        "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, NULL)");

    // Then
    final var expected =
        Table.ofValues(
            "TABLE1",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"),
            List.of(
                List.of(1, "Value1", 100, "Extra1"),
                List.of(2, "Value2", 200, "Extra2"),
                Arrays.asList(3, "Value3", 300, null)));
    final var actual =
        fetchTable(
            "TABLE1",
            "SELECT ID, COLUMN1, COLUMN2, COLUMN3 FROM TABLE1 ORDER BY ID",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"));

    DatabaseAssertion.assertEquals(expected, actual);
    logger.info("Programmatic API demonstration completed - DatabaseAssertion.assertEquals invoked");
  }

  /**
   * Demonstrates programmatic custom SQL query validation.
   *
   * <p>This test shows validation using direct SQL queries instead of relying on
   * {@code @ExpectedDataSet} annotation. Programmatic assertions provide flexibility for custom
   * validation scenarios.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
   *   <li>Execution: Inserts (3,Value3,300,NULL) and (4,Value4,400,NULL)
   *   <li>Expectation: Validates using SQL queries to verify row count and specific records
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate database state using multiple SQL queries")
  @DataSet
  void shouldValidateUsingMultipleQueries() throws Exception {
    // Given
    logger.info("Running multiple query validation test");

    // When
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Value3', 300)");
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Value4', 400)");

    // Then
    // Programmatic validation using SQL queries
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {

      // Verify total row count
      try (final var resultSet = statement.executeQuery("SELECT COUNT(*) FROM TABLE1")) {
        resultSet.next();
        final var count = resultSet.getInt(1);
        if (count != 4) {
          throw new AssertionError(String.format("Expected 4 rows in TABLE1 but found %d", count));
        }
      }

      // Verify newly inserted records exist with correct values
      try (final var resultSet =
          statement.executeQuery(
              "SELECT COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (3, 4) ORDER BY ID")) {
        // Verify row 3
        if (!resultSet.next()) {
          throw new AssertionError("Expected row with ID=3 but not found");
        }
        if (!"Value3".equals(resultSet.getString("COLUMN1"))
            || resultSet.getInt("COLUMN2") != 300) {
          throw new AssertionError(
              String.format(
                  "Expected row 3 (Value3, 300) but found (%s, %d)",
                  resultSet.getString("COLUMN1"), resultSet.getInt("COLUMN2")));
        }

        // Verify row 4
        if (!resultSet.next()) {
          throw new AssertionError("Expected row with ID=4 but not found");
        }
        if (!"Value4".equals(resultSet.getString("COLUMN1"))
            || resultSet.getInt("COLUMN2") != 400) {
          throw new AssertionError(
              String.format(
                  "Expected row 4 (Value4, 400) but found (%s, %d)",
                  resultSet.getString("COLUMN1"), resultSet.getInt("COLUMN2")));
        }
      }
    }

    logger.info("Multiple query validation completed");
  }

  /**
   * Demonstrates {@link DatabaseQueryAssertion#assertEqualsByQuery} for comparing SQL query results
   * against expected data.
   *
   * <p>This test shows how to use the programmatic API to validate query results against
   * programmatically constructed expected data. This is useful when:
   *
   * <ul>
   *   <li>Validating complex queries with joins, aggregations, or filters
   *   <li>Comparing subset of data returned by specific queries
   *   <li>Testing views or stored procedure results
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate query results using assertEqualsByQuery")
  @DataSet
  void shouldValidateQueryResultsUsingAssertEqualsByQuery() throws Exception {
    // Given
    logger.info("Running assertEqualsByQuery demonstration");

    // When & Then
    // Build expected table programmatically
    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("COLUMN1");
    final var columnNumber = new ColumnName("COLUMN2");

    final var row1 =
        Row.of(
            Map.of(
                columnId, new CellValue(1),
                columnValue, new CellValue("Value1"),
                columnNumber, new CellValue(100)));
    final var row2 =
        Row.of(
            Map.of(
                columnId, new CellValue(2),
                columnValue, new CellValue("Value2"),
                columnNumber, new CellValue(200)));

    final var expectedTable =
        Table.of(
            new TableName("QUERY_RESULT"),
            List.of(columnId, columnValue, columnNumber),
            List.of(row1, row2));

    // Use DatabaseQueryAssertion.assertEqualsByQuery to validate query results
    DatabaseQueryAssertion.assertEqualsByQuery(
        expectedTable,
        dataSource,
        "QUERY_RESULT",
        "SELECT ID, COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (1, 2) ORDER BY ID");

    logger.info("assertEqualsByQuery validation completed");
  }

  /**
   * Demonstrates {@link DatabaseAssertion#assertEqualsIgnoreColumns} for comparing tables while
   * excluding specific columns.
   *
   * <p>This is useful when certain columns contain auto-generated or non-deterministic values that
   * should be excluded from comparison, such as:
   *
   * <ul>
   *   <li>Auto-generated primary keys
   *   <li>Timestamp columns (created_at, updated_at)
   *   <li>Version or sequence numbers
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should ignore specific columns using assertEqualsIgnoreColumns")
  @DataSet
  void shouldIgnoreSpecificColumnsUsingAssertEqualsIgnoreColumns() {
    // Given
    logger.info("Running assertEqualsIgnoreColumns demonstration");

    // When
    // Insert a row whose COLUMN3 value is intentionally different from the placeholder used in
    // the expected table. The exclusion of COLUMN3 must allow the assertion to succeed.
    executeSql(
        "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)"
            + " VALUES (3, 'Value3', 300, 'RandomExtra')");

    // Build expected data with deliberately wrong COLUMN3 placeholders to prove the ignore
    // mechanism is the only reason the assertion succeeds.
    final var expectedTable =
        Table.ofValues(
            "TABLE1",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"),
            List.of(
                List.of(1, "Value1", 100, "PLACEHOLDER_1"),
                List.of(2, "Value2", 200, "PLACEHOLDER_2"),
                List.of(3, "Value3", 300, "PLACEHOLDER_3")));

    // Fetch the actual table from the database.
    final var actualTable =
        fetchTable(
            "TABLE1",
            "SELECT ID, COLUMN1, COLUMN2, COLUMN3 FROM TABLE1 ORDER BY ID",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"));

    // Then
    DatabaseAssertion.assertEqualsIgnoreColumns(expectedTable, actualTable, "COLUMN3");

    logger.info("assertEqualsIgnoreColumns validation completed - COLUMN3 was ignored");
  }

  /**
   * Demonstrates {@link DatabaseAssertion#assertEquals} for direct dataset comparison.
   *
   * <p>This test shows how to compare two complete datasets directly. This is useful for:
   *
   * <ul>
   *   <li>Comparing entire table snapshots before and after operations
   *   <li>Validating data migration results
   *   <li>Testing data transformation logic
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should compare tables directly using assertEquals")
  @DataSet
  void shouldCompareTablesDirectlyUsingAssertEquals() {
    // Given
    logger.info("Running assertEquals demonstration");

    // When
    // Build the expected table from the known preparation contents.
    final var expectedTable =
        Table.ofValues(
            "TABLE1",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"),
            List.of(
                List.of(1, "Value1", 100, "Extra1"),
                List.of(2, "Value2", 200, "Extra2")));

    // Fetch the actual table from the database so the comparison reflects real database state.
    final var actualTable =
        fetchTable(
            "TABLE1",
            "SELECT ID, COLUMN1, COLUMN2, COLUMN3 FROM TABLE1 ORDER BY ID",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3"));

    // Then
    DatabaseAssertion.assertEquals(expectedTable, actualTable);

    logger.info("assertEquals validation completed - tables match exactly");
  }

  /**
   * Demonstrates concise table construction using {@link Table#ofValues}.
   *
   * <p>This test shows the convenience factory method that eliminates boilerplate from {@code
   * ColumnName} and {@code CellValue} wrapping. Compare with {@link
   * #shouldValidateQueryResultsUsingAssertEqualsByQuery()} which uses the verbose API.
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should demonstrate concise table construction using Table.ofValues")
  @DataSet
  void shouldDemonstrateConciseTableConstructionUsingOfValues() throws Exception {
    // Given
    logger.info("Running Table.ofValues demonstration");

    // When & Then
    // Build expected table concisely using ofValues
    final var expectedTable =
        Table.ofValues(
            "QUERY_RESULT",
            List.of("ID", "COLUMN1", "COLUMN2"),
            List.of(List.of(1, "Value1", 100), List.of(2, "Value2", 200)));

    // Use DatabaseQueryAssertion.assertEqualsByQuery to validate query results
    DatabaseQueryAssertion.assertEqualsByQuery(
        expectedTable,
        dataSource,
        "QUERY_RESULT",
        "SELECT ID, COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (1, 2) ORDER BY ID");

    logger.info("Table.ofValues demonstration completed");
  }

  /**
   * Demonstrates using {@link DatabaseQueryAssertion#assertEqualsByQuery} with TableSet for
   * multi-table scenarios.
   *
   * <p>This test shows how to use TableSet-based assertions when working with expected data that
   * contains multiple tables. The query results are compared against a specific table within the
   * expected dataset.
   *
   * @throws Exception if test fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate using TableSet-based assertEqualsByQuery for multi-table scenarios")
  @DataSet
  void shouldValidateUsingTableSetBasedAssertEqualsByQuery() throws Exception {
    // Given
    logger.info("Running TableSet-based assertEqualsByQuery demonstration");

    // When & Then
    // Build expected table
    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("COLUMN1");

    final var row1 =
        Row.of(
            Map.of(
                columnId, new CellValue(1),
                columnValue, new CellValue("Value1")));
    final var row2 =
        Row.of(
            Map.of(
                columnId, new CellValue(2),
                columnValue, new CellValue("Value2")));

    final var expectedTable =
        Table.of(new TableName("TABLE1"), List.of(columnId, columnValue), List.of(row1, row2));

    // Wrap table in a TableSet
    final var expectedTableSet = TableSet.of(expectedTable);

    // Use TableSet-based assertEqualsByQuery
    DatabaseQueryAssertion.assertEqualsByQuery(
        expectedTableSet, dataSource, "TABLE1", "SELECT ID, COLUMN1 FROM TABLE1 ORDER BY ID");

    logger.info("TableSet-based assertEqualsByQuery validation completed");
  }
}
