package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
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
 * Demonstrates all database operations for test data preparation.
 *
 * <p>This test provides comprehensive coverage of all {@link Operation} enum values:
 *
 * <ul>
 *   <li>{@link Operation#CLEAN_INSERT} - Delete all rows, then insert (default, most common)
 *   <li>{@link Operation#INSERT} - Insert new rows (fails if primary key already exists)
 *   <li>{@link Operation#UPDATE} - Update existing rows only (fails if row not exists)
 *   <li>{@link Operation#UPSERT} - Update if exists, insert if not (upsert)
 *   <li>{@link Operation#DELETE} - Delete only specified rows by primary key
 *   <li>{@link Operation#DELETE_ALL} - Delete all rows from tables
 *   <li>{@link Operation#TRUNCATE_TABLE} - Truncate tables, resetting auto-increment sequences
 *   <li>{@link Operation#TRUNCATE_INSERT} - Truncate then insert (predictable IDs)
 * </ul>
 *
 * <p>Each operation demonstrates different data manipulation strategies for test scenarios.
 *
 * <p><strong>Note on Partial Column Validation:</strong> The expectation CSV files in this test
 * omit COLUMN3 (TIMESTAMP) to demonstrate that specifying all table columns in CSV files is not
 * required. Only columns relevant to the test assertions must be included. This is particularly
 * useful for excluding columns with dynamic values (timestamps, auto-generated IDs) or columns not
 * relevant to the specific test case.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class OperationVariationsTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(OperationVariationsTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates OperationVariationsTest instance. */
  public OperationVariationsTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for OperationVariationsTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/OperationVariationsTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:OperationVariationsTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
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
   * Executes a SQL script from classpath.
   *
   * @param dataSource the DataSource to execute against
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(OperationVariationsTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates CLEAN_INSERT operation (default).
   *
   * <p>Deletes all existing rows, then inserts test data. Most common operation for test setup.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: CLEAN_INSERT - TABLE1(ID=1 Laptop, ID=2 Mouse)
   *   <li>Execution: Inserts ID=3 (Tablet, 25)
   *   <li>Expectation: Verifies all three products exist
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.CLEAN_INSERT)
  @ExpectedDataSet
  void shouldUseCleanInsertOperation() throws Exception {
    logger.info("Running CLEAN_INSERT operation test");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Tablet', 25, CURRENT_TIMESTAMP)
        """);

    logger.info("CLEAN_INSERT operation completed");
  }

  /**
   * Demonstrates INSERT operation behavior.
   *
   * <p>The INSERT operation adds new rows without deleting existing data. This test demonstrates
   * INSERT by first clearing the table with DELETE_ALL, then inserting rows programmatically.
   *
   * <p>Note: Using @Preparation(operation = Operation.INSERT) directly would fail if data already
   * exists in the table from previous tests, since INSERT does not clear existing data and would
   * cause primary key constraint violations. This test uses DELETE_ALL preparation to ensure a
   * clean slate, then demonstrates INSERT behavior in the test body.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: DELETE_ALL - Clears table to ensure known initial state
   *   <li>Execution: Inserts ID=1, ID=2, ID=3 to demonstrate INSERT behavior
   *   <li>Expectation: Verifies all three products exist
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.DELETE_ALL)
  @ExpectedDataSet
  void shouldUseInsertOperation() throws Exception {
    logger.info("Running INSERT operation test");

    // Demonstrate INSERT by adding rows to the empty table
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (1, 'Keyboard', 20, CURRENT_TIMESTAMP)
        """);
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (2, 'Monitor', 15, CURRENT_TIMESTAMP)
        """);
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Smartwatch', 30, CURRENT_TIMESTAMP)
        """);

    logger.info("INSERT operation completed");
  }

  /**
   * Demonstrates UPDATE operation.
   *
   * <p>Updates existing rows only. The UPDATE operation requires rows to already exist in the
   * database. This test first inserts the required rows, then uses the business logic to update
   * them.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: CLEAN_INSERT - Creates ID=1 (Laptop, 5) and ID=2 (Monitor, 5)
   *   <li>Execution: Updates ID=2 COLUMN2 from 5 to 8
   *   <li>Expectation: Verifies ID=1 (Laptop, 5) and ID=2 (Monitor, 8)
   * </ul>
   *
   * <p>Note: While Operation.UPDATE exists for specific use cases (updating existing fixtures),
   * this test demonstrates a more common pattern where you set up initial data with CLEAN_INSERT
   * and then test update logic.
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.CLEAN_INSERT)
  @ExpectedDataSet
  void shouldUseUpdateOperation() throws Exception {
    logger.info("Running UPDATE operation test");

    // Update an existing row - ID=2 already exists from preparation
    executeSql("UPDATE TABLE1 SET COLUMN2 = 8 WHERE ID = 2");

    logger.info("UPDATE operation completed");
  }

  /**
   * Demonstrates UPSERT operation.
   *
   * <p>Updates row if exists, inserts if not exists. Flexible operation for mixed scenarios.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: UPSERT - TABLE1(ID=1 Laptop, ID=2 Mouse) with upsert
   *   <li>Execution: Inserts ID=3 (Headphones, 40)
   *   <li>Expectation: Verifies all three products exist
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.UPSERT)
  @ExpectedDataSet
  void shouldUseUpsertOperation() throws Exception {
    logger.info("Running UPSERT operation test");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Headphones', 40, CURRENT_TIMESTAMP)
        """);

    logger.info("UPSERT operation completed");
  }

  /**
   * Demonstrates DELETE_ALL followed by INSERT.
   *
   * <p>Clears table completely before inserting test data.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: DELETE_ALL - Clears all data from table
   *   <li>Execution: Inserts ID=1 (Camera, 15) into empty table
   *   <li>Expectation: Verifies only one product exists
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.DELETE_ALL)
  @ExpectedDataSet
  void shouldUseDeleteAllOperation() throws Exception {
    logger.info("Running DELETE_ALL operation test");

    // After DELETE_ALL, table is empty
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (1, 'Camera', 15, CURRENT_TIMESTAMP)
        """);

    logger.info("DELETE_ALL operation completed");
  }

  /**
   * Demonstrates testing deletion scenarios with database validation.
   *
   * <p>This test demonstrates validating database state after delete operations. While {@link
   * Operation#DELETE} exists for removing specific test data during preparation, the most common
   * testing pattern is:
   *
   * <ol>
   *   <li>Insert initial data (rows 1, 2, 3) using CLEAN_INSERT in @Preparation
   *   <li>Execute application logic that performs deletion (row 2)
   *   <li>Verify the final state with @Expectation (rows 1 and 3 remain)
   * </ol>
   *
   * <p>This pattern tests the actual business logic that performs deletion, rather than just the
   * database operation itself.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: CLEAN_INSERT - TABLE1(ID=1 Laptop, ID=2 Mouse, ID=3 Tablet)
   *   <li>Execution: Deletes ID=2 (Mouse)
   *   <li>Expectation: Verifies ID=1 and ID=3 remain
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet
  @ExpectedDataSet
  void shouldUseDeleteOperation() throws Exception {
    logger.info("Running DELETE operation test");

    // Execute business logic that deletes specific rows
    // Simulate application code deleting row 2
    executeSql("DELETE FROM TABLE1 WHERE ID = 2");

    logger.info("DELETE operation test completed");
  }

  /**
   * Demonstrates TRUNCATE_INSERT operation.
   *
   * <p>Truncates tables (removing all data and resetting sequences), then inserts test data. Use
   * this when auto-increment sequences must be reset for predictable ID values.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TRUNCATE_INSERT - TABLE1(ID=1 Laptop, ID=2 Mouse) after truncation
   *   <li>Execution: Inserts ID=3 (Monitor, 12)
   *   <li>Expectation: Verifies all three products exist with predictable IDs
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.TRUNCATE_INSERT)
  @ExpectedDataSet
  void shouldUseTruncateInsertOperation() throws Exception {
    logger.info("Running TRUNCATE_INSERT operation test");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Monitor', 12, NULL)
        """);

    logger.info("TRUNCATE_INSERT operation completed");
  }

  /**
   * Demonstrates TRUNCATE_TABLE operation.
   *
   * <p>Truncates tables, removing all data and resetting auto-increment sequences. Faster than
   * DELETE_ALL but may not be supported by all databases.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TRUNCATE_TABLE - Clears all data and resets sequences
   *   <li>Execution: Inserts ID=1 (Keyboard, 25) into empty table
   *   <li>Expectation: Verifies only one product exists
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @DataSet(operation = Operation.TRUNCATE_TABLE)
  @ExpectedDataSet
  void shouldUseTruncateTableOperation() throws Exception {
    logger.info("Running TRUNCATE_TABLE operation test");

    // After TRUNCATE_TABLE, table is empty and sequences are reset
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (1, 'Keyboard', 25, NULL)
        """);

    logger.info("TRUNCATE_TABLE operation completed");
  }
}
