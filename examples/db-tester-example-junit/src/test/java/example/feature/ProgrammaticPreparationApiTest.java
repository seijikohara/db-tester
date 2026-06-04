package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion;
import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.api.preparation.DatabasePreparation;
import io.github.seijikohara.dbtester.api.preparation.PreparationConfig;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the {@link DatabasePreparation} programmatic API for preparing test data without
 * {@code @DataSet} annotations.
 *
 * <p>This test class illustrates how to use the programmatic preparation API for scenarios where
 * annotation-based dataset loading is insufficient or impractical:
 *
 * <ul>
 *   <li>{@link DatabasePreparation#cleanInsert(DataSource, TableSet)} - Clean insert with default
 *       configuration
 *   <li>{@link DatabasePreparation#cleanInsert(DataSource, TableSet, PreparationConfig)} - Clean
 *       insert with custom batch size and transaction mode
 *   <li>{@link DatabasePreparation#execute(DataSource, TableSet, Operation)} - Execute a specific
 *       operation (INSERT, UPDATE, DELETE, etc.)
 *   <li>{@link DatabasePreparation#execute(DataSource, TableSet, Operation, PreparationConfig)} -
 *       Execute with full configuration control
 * </ul>
 *
 * <p>Programmatic preparation supports dynamic test data generation, computed values, and mid-test
 * data manipulation that cannot be expressed in static CSV files.
 *
 * @see DatabasePreparation
 * @see PreparationConfig
 * @see Operation
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ProgrammaticPreparationApiTest")
final class ProgrammaticPreparationApiTest {

  /** Logger instance. */
  private static final Logger logger =
      LoggerFactory.getLogger(ProgrammaticPreparationApiTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ProgrammaticPreparationApiTest instance. */
  ProgrammaticPreparationApiTest() {}

  /**
   * Sets up H2 in-memory database and schema.
   *
   * @param context extension context
   * @throws Exception if setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up database for ProgrammaticPreparationApiTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ProgrammaticPreparationApiTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ProgrammaticPreparationApiTest;DB_CLOSE_DELAY=-1");
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
                ProgrammaticPreparationApiTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests for the {@link DatabasePreparation#cleanInsert(DataSource, TableSet)} method and its
   * overload with {@link PreparationConfig}.
   */
  @Nested
  @DisplayName("cleanInsert() method")
  class CleanInsertMethod {

    /** Creates CleanInsertMethod instance. */
    CleanInsertMethod() {}

    /**
     * Verifies that {@link DatabasePreparation#cleanInsert(DataSource, TableSet)} inserts data with
     * default configuration.
     *
     * <p>This test demonstrates programmatic preparation by building tables using {@link
     * Table#ofValues} and inserting them via {@code cleanInsert} with standard defaults.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Programmatically constructs USERS and ORDERS tables
     *   <li>Execution: Calls {@code DatabasePreparation.cleanInsert(dataSource, tableSet)}
     *   <li>Expectation: Verifies database state via {@code @ExpectedDataSet}
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should insert data with default settings")
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                resourceLocation =
                    "classpath:example/feature/ProgrammaticPreparationApiTest/clean-insert-default/expected/"))
    void shouldInsertDataWithDefaultSettings() {
      // Given
      logger.info("Running cleanInsert with default settings");

      final var usersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(
                  List.of(1, "Alice", "alice@example.com"), List.of(2, "Bob", "bob@example.com")));

      final var ordersTable =
          Table.ofValues(
              "ORDERS",
              List.of("ID", "USER_ID", "PRODUCT", "AMOUNT"),
              List.of(List.of(101, 1, "Laptop", 999.99), List.of(102, 2, "Mouse", 29.99)));

      final var tableSet = TableSet.of(usersTable, ordersTable);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet);

      // Then
      logger.info("cleanInsert with default settings completed");
    }

    /**
     * Verifies that {@link DatabasePreparation#cleanInsert(DataSource, TableSet,
     * PreparationConfig)} inserts data with custom batch size and transaction mode.
     *
     * <p>This test demonstrates customizing preparation behavior via {@link PreparationConfig}
     * method chaining, including batch size, transaction mode, and table ordering strategy.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Constructs USERS (3 rows) and ORDERS (3 rows) with custom config
     *   <li>Execution: Calls {@code cleanInsert} with batch size 2, AUTO_COMMIT, FOREIGN_KEY
     *       ordering
     *   <li>Expectation: Verifies all six records via {@code @ExpectedDataSet}
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should insert data with custom batch size")
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                resourceLocation =
                    "classpath:example/feature/ProgrammaticPreparationApiTest/clean-insert-custom-batch/expected/"))
    void shouldInsertDataWithCustomBatchSize() {
      // Given
      logger.info("Running cleanInsert with custom batch size");

      final var usersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(
                  List.of(1, "Alice", "alice@example.com"),
                  List.of(2, "Bob", "bob@example.com"),
                  List.of(3, "Charlie", "charlie@example.com")));

      final var ordersTable =
          Table.ofValues(
              "ORDERS",
              List.of("ID", "USER_ID", "PRODUCT", "AMOUNT"),
              List.of(
                  List.of(101, 1, "Laptop", 999.99),
                  List.of(102, 2, "Mouse", 29.99),
                  List.of(103, 3, "Keyboard", 79.99)));

      final var tableSet = TableSet.of(usersTable, ordersTable);

      final var config =
          PreparationConfig.standard()
              .withBatchSize(2)
              .withTransactionMode(TransactionMode.AUTO_COMMIT)
              .withTableOrdering(TableOrderingStrategy.FOREIGN_KEY);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet, config);

      // Then
      logger.info("cleanInsert with custom batch size completed");
    }
  }

  /**
   * Tests for the {@link DatabasePreparation#execute(DataSource, TableSet, Operation)} method and
   * its overload with {@link PreparationConfig}.
   */
  @Nested
  @DisplayName("execute() method")
  class ExecuteMethod {

    /** Creates ExecuteMethod instance. */
    ExecuteMethod() {}

    /**
     * Verifies that {@link DatabasePreparation#execute(DataSource, TableSet, Operation)} executes
     * an INSERT operation.
     *
     * <p>This test demonstrates using a specific {@link Operation#INSERT} instead of the default
     * CLEAN_INSERT. The tables are manually cleared before insertion to ensure a clean state.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Clears ORDERS then USERS tables via SQL DELETE
     *   <li>Execution: Calls {@code DatabasePreparation.execute} with {@link Operation#INSERT}
     *   <li>Expectation: Verifies single USERS record via {@code @ExpectedDataSet}
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should execute insert operation")
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                resourceLocation =
                    "classpath:example/feature/ProgrammaticPreparationApiTest/execute-insert/expected/"))
    void shouldExecuteInsertOperation() {
      // Given
      logger.info("Running execute with INSERT operation");

      executeSql("DELETE FROM ORDERS");
      executeSql("DELETE FROM USERS");

      final var usersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(List.of(1, "Alice", "alice@example.com")));

      final var tableSet = TableSet.of(usersTable);

      // When
      DatabasePreparation.execute(dataSource, tableSet, Operation.INSERT);

      // Then
      logger.info("execute with INSERT operation completed");
    }

    /**
     * Verifies that {@link DatabasePreparation#execute(DataSource, TableSet, Operation,
     * PreparationConfig)} executes with explicit operation and configuration.
     *
     * <p>This test demonstrates combining a specific operation with custom configuration settings
     * for transaction mode and table ordering strategy.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Constructs USERS and ORDERS tables with custom config
     *   <li>Execution: Calls {@code execute} with CLEAN_INSERT, FOREIGN_KEY ordering, and
     *       AUTO_COMMIT transaction mode
     *   <li>Expectation: Verifies all records via {@code @ExpectedDataSet}
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should execute with explicit operation and configuration")
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                resourceLocation =
                    "classpath:example/feature/ProgrammaticPreparationApiTest/execute-full-config/expected/"))
    void shouldExecuteWithExplicitOperationAndConfiguration() {
      // Given
      logger.info("Running execute with explicit operation and configuration");

      final var usersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(
                  List.of(1, "Alice", "alice@example.com"), List.of(2, "Bob", "bob@example.com")));

      final var ordersTable =
          Table.ofValues(
              "ORDERS",
              List.of("ID", "USER_ID", "PRODUCT", "AMOUNT"),
              List.of(List.of(101, 1, "Laptop", 999.99), List.of(102, 2, "Mouse", 29.99)));

      final var tableSet = TableSet.of(usersTable, ordersTable);

      final var config =
          PreparationConfig.standard()
              .withTransactionMode(TransactionMode.AUTO_COMMIT)
              .withTableOrdering(TableOrderingStrategy.FOREIGN_KEY);

      // When
      DatabasePreparation.execute(dataSource, tableSet, Operation.CLEAN_INSERT, config);

      // Then
      logger.info("execute with explicit operation and configuration completed");
    }
  }

  /**
   * Tests for the fully programmatic flow combining {@link DatabasePreparation} and {@link
   * DatabaseQueryAssertion} without any annotations.
   */
  @Nested
  @DisplayName("full programmatic flow")
  class FullProgrammaticFlowMethod {

    /** Creates FullProgrammaticFlowMethod instance. */
    FullProgrammaticFlowMethod() {}

    /**
     * Verifies that test data can be prepared and verified without any annotations.
     *
     * <p>This test demonstrates a fully programmatic approach using {@link DatabasePreparation} for
     * data setup and {@link DatabaseQueryAssertion#assertEqualsByQuery} for verification, without
     * relying on {@code @DataSet} or {@code @ExpectedDataSet} annotations.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Clears ORDERS (FK child) then USERS, constructs and inserts USERS and
     *       ORDERS data via {@code cleanInsert}
     *   <li>Execution: No additional operation (preparation is the action under test)
     *   <li>Expectation: Builds expected data programmatically and verifies via {@code
     *       assertEqualsByQuery}
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should prepare and verify without annotations")
    void shouldPrepareAndVerifyWithoutAnnotations() {
      // Given
      logger.info("Running full programmatic flow without annotations");

      final var usersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(
                  List.of(1, "Alice", "alice@example.com"), List.of(2, "Bob", "bob@example.com")));

      final var ordersTable =
          Table.ofValues(
              "ORDERS",
              List.of("ID", "USER_ID", "PRODUCT", "AMOUNT"),
              List.of(List.of(101, 1, "Laptop", 999.99), List.of(102, 2, "Mouse", 29.99)));

      final var tableSet = TableSet.of(usersTable, ordersTable);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet);

      // Then
      final var expectedUsersTable =
          Table.ofValues(
              "USERS",
              List.of("ID", "NAME", "EMAIL"),
              List.of(
                  List.of(1, "Alice", "alice@example.com"), List.of(2, "Bob", "bob@example.com")));

      DatabaseQueryAssertion.assertEqualsByQuery(
          expectedUsersTable, dataSource, "USERS", "SELECT ID, NAME, EMAIL FROM USERS ORDER BY ID");

      final var expectedOrdersTable =
          Table.ofValues(
              "ORDERS",
              List.of("ID", "USER_ID", "PRODUCT", "AMOUNT"),
              List.of(List.of(101, 1, "Laptop", 999.99), List.of(102, 2, "Mouse", 29.99)));

      DatabaseQueryAssertion.assertEqualsByQuery(
          expectedOrdersTable,
          dataSource,
          "ORDERS",
          "SELECT ID, USER_ID, PRODUCT, AMOUNT FROM ORDERS ORDER BY ID");

      logger.info("Full programmatic flow completed");
    }
  }
}
