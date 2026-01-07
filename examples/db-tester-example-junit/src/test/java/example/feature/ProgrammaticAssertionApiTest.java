package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
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
 * <p>Key programmatic API features available in {@link DatabaseAssertion}:
 *
 * <ul>
 *   <li>{@link DatabaseAssertion#assertEqualsByQuery} - Compare expected data against SQL query
 *       results
 *   <li>{@link DatabaseAssertion#assertEquals} - Compare two datasets or tables directly
 *   <li>{@link DatabaseAssertion#assertEqualsIgnoreColumns} - Compare datasets ignoring specific
 *       columns
 * </ul>
 *
 * <p>Programmatic assertions are useful for custom SQL queries, dynamic column filtering, mid-test
 * state verification, or comparing multiple dataset sources.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class ProgrammaticAssertionApiTest {

  /** Logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ProgrammaticAssertionApiTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ProgrammaticAssertionApiTest instance. */
  public ProgrammaticAssertionApiTest() {}

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
   * Demonstrates basic programmatic assertion without annotations.
   *
   * <p>Shows direct use of {@link DatabaseAssertion} assertion APIs for custom validation scenarios
   * where annotation-based testing is insufficient.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
   *   <li>Execution: Inserts (3,Value3,300,NULL)
   *   <li>Expectation: Verifies all three records including NULL COLUMN3
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @DataSet
  @ExpectedDataSet
  void shouldDemonstrateBasicProgrammaticAPI() throws Exception {
    logger.info("Running programmatic API demonstration");

    executeSql(
        "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, NULL)");

    logger.info("Programmatic API demonstration completed - uses standard @Expectation validation");
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
  @DataSet
  void shouldValidateUsingMultipleQueries() throws Exception {
    logger.info("Running multiple query validation test");

    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Value3', 300)");
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Value4', 400)");

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
   * Demonstrates {@link DatabaseAssertion#assertEqualsByQuery} for comparing SQL query results
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
  @DataSet
  void shouldValidateQueryResultsUsingAssertEqualsByQuery() throws Exception {
    logger.info("Running assertEqualsByQuery demonstration");

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

    // Use DatabaseAssertion.assertEqualsByQuery to validate query results
    DatabaseAssertion.assertEqualsByQuery(
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
  @DataSet
  void shouldIgnoreSpecificColumnsUsingAssertEqualsIgnoreColumns() throws Exception {
    logger.info("Running assertEqualsIgnoreColumns demonstration");

    // Insert a row where COLUMN3 has a non-deterministic value
    executeSql(
        "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, 'RandomExtra')");

    // Build expected data ignoring COLUMN3 (which might be auto-generated or volatile)
    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("COLUMN1");
    final var columnNumber = new ColumnName("COLUMN2");
    final var columnExtra = new ColumnName("COLUMN3");

    final var row1 =
        Row.of(
            Map.of(
                columnId, new CellValue(1),
                columnValue, new CellValue("Value1"),
                columnNumber, new CellValue(100),
                columnExtra, new CellValue("Extra1")));
    final var row2 =
        Row.of(
            Map.of(
                columnId, new CellValue(2),
                columnValue, new CellValue("Value2"),
                columnNumber, new CellValue(200),
                columnExtra, new CellValue("Extra2")));
    final var row3 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(3),
                columnValue,
                new CellValue("Value3"),
                columnNumber,
                new CellValue(300),
                columnExtra,
                new CellValue("IGNORED"))); // This value won't be compared

    final var expectedTable =
        Table.of(
            new TableName("TABLE1"),
            List.of(columnId, columnValue, columnNumber, columnExtra),
            List.of(row1, row2, row3));

    // Build actual table from query
    final var actualRow1 =
        Row.of(
            Map.of(
                columnId, new CellValue(1),
                columnValue, new CellValue("Value1"),
                columnNumber, new CellValue(100),
                columnExtra, new CellValue("Extra1")));
    final var actualRow2 =
        Row.of(
            Map.of(
                columnId, new CellValue(2),
                columnValue, new CellValue("Value2"),
                columnNumber, new CellValue(200),
                columnExtra, new CellValue("Extra2")));
    final var actualRow3 =
        Row.of(
            Map.of(
                columnId,
                new CellValue(3),
                columnValue,
                new CellValue("Value3"),
                columnNumber,
                new CellValue(300),
                columnExtra,
                new CellValue("RandomExtra"))); // Different value but will be ignored

    final var actualTable =
        Table.of(
            new TableName("TABLE1"),
            List.of(columnId, columnValue, columnNumber, columnExtra),
            List.of(actualRow1, actualRow2, actualRow3));

    // Compare tables while ignoring COLUMN3
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
  @DataSet
  void shouldCompareTablesDirectlyUsingAssertEquals() throws Exception {
    logger.info("Running assertEquals demonstration");

    // Build expected table
    final var columnId = new ColumnName("ID");
    final var columnValue = new ColumnName("COLUMN1");
    final var columnNumber = new ColumnName("COLUMN2");
    final var columnExtra = new ColumnName("COLUMN3");

    final var row1 =
        Row.of(
            Map.of(
                columnId, new CellValue(1),
                columnValue, new CellValue("Value1"),
                columnNumber, new CellValue(100),
                columnExtra, new CellValue("Extra1")));
    final var row2 =
        Row.of(
            Map.of(
                columnId, new CellValue(2),
                columnValue, new CellValue("Value2"),
                columnNumber, new CellValue(200),
                columnExtra, new CellValue("Extra2")));

    final var expectedTable =
        Table.of(
            new TableName("TABLE1"),
            List.of(columnId, columnValue, columnNumber, columnExtra),
            List.of(row1, row2));

    // Build actual table (simulating what would be read from the database)
    final var actualTable =
        Table.of(
            new TableName("TABLE1"),
            List.of(columnId, columnValue, columnNumber, columnExtra),
            List.of(row1, row2)); // Same data as expected

    // Direct table comparison
    DatabaseAssertion.assertEquals(expectedTable, actualTable);

    logger.info("assertEquals validation completed - tables match exactly");
  }

  /**
   * Demonstrates using {@link DatabaseAssertion#assertEqualsByQuery} with TableSet for multi-table
   * scenarios.
   *
   * <p>This test shows how to use TableSet-based assertions when working with expected data that
   * contains multiple tables. The query results are compared against a specific table within the
   * expected dataset.
   *
   * @throws Exception if test fails
   */
  @Test
  @DataSet
  void shouldValidateUsingTableSetBasedAssertEqualsByQuery() throws Exception {
    logger.info("Running TableSet-based assertEqualsByQuery demonstration");

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
    DatabaseAssertion.assertEqualsByQuery(
        expectedTableSet, dataSource, "TABLE1", "SELECT ID, COLUMN1 FROM TABLE1 ORDER BY ID");

    logger.info("TableSet-based assertEqualsByQuery validation completed");
  }
}
