package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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
 * Demonstrates database testing with SQL query result validation.
 *
 * <p>Each test prepares baseline data through {@link DataSet}, runs additional SQL that exercises
 * the feature under test, then verifies the result of a SQL query (rather than the full table
 * state) using {@link DatabaseQueryAssertion#assertEqualsByQuery}.
 *
 * <p>This test demonstrates four query patterns:
 *
 * <ul>
 *   <li>Filtering rows via a {@code WHERE} clause
 *   <li>Aggregating rows via {@code GROUP BY} with {@code SUM} and {@code COUNT}
 *   <li>Joining two tables via {@code INNER JOIN}
 *   <li>Restricting rows to a date range via {@code BETWEEN}
 * </ul>
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("CustomQueryValidationTest")
final class CustomQueryValidationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(CustomQueryValidationTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates CustomQueryValidationTest instance. */
  CustomQueryValidationTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for CustomQueryValidationTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/CustomQueryValidationTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:CustomQueryValidationTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  /**
   * Executes a SQL script from the classpath.
   *
   * @param dataSource the DataSource to execute against
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(
                CustomQueryValidationTest.class.getClassLoader().getResource(scriptPath))
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
   * Builds an in-memory Table for use as an expected query result.
   *
   * @param tableName the logical table name attached to the query result
   * @param columnNames the column names in order
   * @param rowValues each inner list represents the cell values of one row, in column order
   * @return a Table representing the expected query result
   */
  private static Table createTable(
      final String tableName,
      final List<String> columnNames,
      final List<List<Object>> rowValues) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rows =
        rowValues.stream()
            .map(
                values -> {
                  final Map<ColumnName, CellValue> row = new LinkedHashMap<>();
                  IntStream.range(0, Math.min(columns.size(), values.size()))
                      .forEach(i -> row.put(columns.get(i), new CellValue(values.get(i))));
                  return Row.of(row);
                })
            .toList();
    return Table.of(new TableName(tableName), columns, rows);
  }

  /**
   * Verifies that a {@code WHERE} filter returns only the matching rows after an insert.
   *
   * <p>The query restricts {@code TABLE1} to rows whose {@code COLUMN4} value equals {@code 'East'}.
   * The expected result contains the pre-loaded East row plus the newly inserted East row, in
   * primary-key order.
   */
  @Test
  @Tag("normal")
  @DisplayName("should return only East-region rows when WHERE filter is applied")
  @DataSet
  void shouldReturnOnlyEastRegionRows_whenWhereFilterApplied() {
    // Given
    logger.info("Inserting a new East-region sale");
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 3, '2024-01-25', 350.00, 'East')
        """);

    final var expected =
        createTable(
            "TABLE1",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3", "COLUMN4"),
            List.of(
                List.of(2, 2, Date.valueOf("2024-01-15"), new BigDecimal("300.00"), "East"),
                List.of(4, 3, Date.valueOf("2024-01-25"), new BigDecimal("350.00"), "East")));

    // When & Then
    DatabaseQueryAssertion.assertEqualsByQuery(
        expected,
        dataSource,
        "TABLE1",
        "SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1"
            + " WHERE COLUMN4 = 'East' ORDER BY ID");
    logger.info("WHERE filter query validation completed");
  }

  /**
   * Verifies that a {@code GROUP BY} aggregation returns the expected per-category totals.
   *
   * <p>The query aggregates {@code TABLE1} by {@code COLUMN1}, computing the sum of {@code COLUMN3}
   * and the row count per group. The expected result reflects the post-insert state across all
   * categories.
   */
  @Test
  @Tag("normal")
  @DisplayName("should return aggregated totals per category when GROUP BY applied")
  @DataSet
  void shouldReturnAggregatedTotalsPerCategory_whenGroupByApplied() {
    // Given
    logger.info("Inserting an additional Premium-category sale");
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 1, '2024-01-25', 500.00, 'West')
        """);

    // Note: TableReader currently uses ResultSetMetaData#getColumnName, which returns the
    // original column name for plain columns and ignores SQL aliases like `AS CATEGORY_ID`.
    // Aggregate expressions still resolve to their alias, so plain columns are projected
    // without an alias here. See follow-up task on switching TableReader to getColumnLabel.
    final var expected =
        createTable(
            "CATEGORY_TOTALS",
            List.of("COLUMN1", "TOTAL_AMOUNT", "RECORD_COUNT"),
            List.of(
                List.of(1, new BigDecimal("1700.00"), 3L),
                List.of(2, new BigDecimal("300.00"), 1L)));

    // When & Then
    DatabaseQueryAssertion.assertEqualsByQuery(
        expected,
        dataSource,
        "CATEGORY_TOTALS",
        "SELECT COLUMN1,"
            + " SUM(COLUMN3) AS TOTAL_AMOUNT,"
            + " COUNT(*) AS RECORD_COUNT"
            + " FROM TABLE1 GROUP BY COLUMN1 ORDER BY COLUMN1");
    logger.info("GROUP BY aggregation query validation completed");
  }

  /**
   * Verifies that an {@code INNER JOIN} between sales and categories returns labeled rows.
   *
   * <p>The query joins {@code TABLE1.COLUMN1} to {@code CATEGORIES.ID} and projects the category
   * name alongside the sale amount. The expected result demonstrates the join surfaces the
   * category label for each sale row.
   */
  @Test
  @Tag("normal")
  @DisplayName("should return joined sale and category rows when INNER JOIN applied")
  @DataSet
  void shouldReturnJoinedSaleAndCategoryRows_whenInnerJoinApplied() {
    // Given
    logger.info("Inserting an additional sale to exercise the join");
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 1, '2024-02-01', 600.00, 'North')
        """);

    // Note: TableReader currently uses ResultSetMetaData#getColumnName, which returns the
    // original column name and ignores SQL aliases. The columns are projected without aliases
    // so the expected column names mirror the underlying table columns.
    final var expected =
        createTable(
            "SALES_WITH_CATEGORY",
            List.of("ID", "NAME", "COLUMN3"),
            List.of(
                List.of(1, "Premium", new BigDecimal("500.00")),
                List.of(2, "Standard", new BigDecimal("300.00")),
                List.of(3, "Premium", new BigDecimal("700.00")),
                List.of(4, "Premium", new BigDecimal("600.00"))));

    // When & Then
    DatabaseQueryAssertion.assertEqualsByQuery(
        expected,
        dataSource,
        "SALES_WITH_CATEGORY",
        "SELECT s.ID, c.NAME, s.COLUMN3"
            + " FROM TABLE1 s INNER JOIN CATEGORIES c ON s.COLUMN1 = c.ID"
            + " ORDER BY s.ID");
    logger.info("INNER JOIN query validation completed");
  }

  /**
   * Verifies that a {@code BETWEEN} date filter excludes rows outside the requested range.
   *
   * <p>Two rows are inserted: one inside the January range and one in February. The query
   * restricts {@code COLUMN2} to January 2024, and the expected result contains only the January
   * rows.
   */
  @Test
  @Tag("normal")
  @DisplayName("should return only January rows when BETWEEN date filter applied")
  @DataSet
  void shouldReturnOnlyJanuaryRows_whenBetweenDateFilterApplied() {
    // Given
    logger.info("Inserting one January row and one February row");
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 2, '2024-01-25', 450.00, 'South')
        """);
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (5, 1, '2024-02-05', 800.00, 'North')
        """);

    final var expected =
        createTable(
            "TABLE1",
            List.of("ID", "COLUMN1", "COLUMN2", "COLUMN3", "COLUMN4"),
            List.of(
                List.of(1, 1, Date.valueOf("2024-01-10"), new BigDecimal("500.00"), "West"),
                List.of(2, 2, Date.valueOf("2024-01-15"), new BigDecimal("300.00"), "East"),
                List.of(3, 1, Date.valueOf("2024-01-20"), new BigDecimal("700.00"), "North"),
                List.of(4, 2, Date.valueOf("2024-01-25"), new BigDecimal("450.00"), "South")));

    // When & Then
    DatabaseQueryAssertion.assertEqualsByQuery(
        expected,
        dataSource,
        "TABLE1",
        "SELECT ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4 FROM TABLE1"
            + " WHERE COLUMN2 BETWEEN DATE '2024-01-01' AND DATE '2024-01-31'"
            + " ORDER BY ID");
    logger.info("BETWEEN date filter query validation completed");
  }
}
