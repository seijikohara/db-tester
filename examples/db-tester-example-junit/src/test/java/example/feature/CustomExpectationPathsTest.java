package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
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
 * Demonstrates custom expectation paths for flexible test data organization.
 *
 * <p>This test demonstrates using {@link DataSetSource#resourceLocation()} to specify custom paths
 * for expectation data, enabling flexible test data organization beyond convention-based defaults.
 *
 * <p>Key features demonstrated:
 *
 * <ul>
 *   <li>Custom expectation paths using {@link DataSetSource} annotation
 *   <li>Organizing multiple expectation scenarios in subdirectories
 *   <li>Multi-stage testing with different expected states
 *   <li>Complex business logic validation with database state changes
 * </ul>
 *
 * <p>This approach is useful when tests require multiple expectation variants or when
 * convention-based paths are insufficient for complex test scenarios.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("CustomExpectationPathsTest")
final class CustomExpectationPathsTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(CustomExpectationPathsTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates CustomExpectationPathsTest instance. */
  CustomExpectationPathsTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for CustomExpectationPathsTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/CustomExpectationPathsTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:CustomExpectationPathsTest;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
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
        Optional.ofNullable(
                CustomExpectationPathsTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates custom expectation paths with basic INSERT operation.
   *
   * <p>This test uses {@link DataSetSource#resourceLocation()} to specify a custom path for
   * expectation data, demonstrating how to organize test data in non-default directories.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 from default location
   *   <li>Execution: Inserts ID=2 (customer_id=1, amount=299.99, PENDING)
   *   <li>Expectation: Verifies both records from {@code expected-basic/} directory
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should insert new order with custom expectation path")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomExpectationPathsTest/expected-basic/"))
  void shouldInsertNewOrder() {
    // Given
    logger.info("Running basic INSERT test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (2, 1, 299.99, '2024-02-15', 'PENDING')
        """);

    // Then
    logger.info("Basic INSERT test completed");
  }

  /**
   * Demonstrates partial column validation using custom expectation paths.
   *
   * <p>CSV files in the custom path contain only the columns to validate, allowing partial
   * validation without programmatic assertions.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 from default location
   *   <li>Execution: Inserts ID=2 (customer_id=2, amount=599.99, SHIPPED)
   *   <li>Expectation: Validates selected columns from {@code expected-ignore-columns/} directory
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate with partial columns using custom expectation path")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomExpectationPathsTest/expected-ignore-columns/"))
  void shouldValidateWithPartialColumns() {
    // Given
    logger.info("Running partial column validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (2, 2, 599.99, '2024-03-20', 'SHIPPED')
        """);

    // Then
    logger.info("Partial column validation test completed");
  }

  /**
   * Demonstrates validating related tables with custom expectation paths.
   *
   * <p>This test inserts data into TABLE2 and validates the relationship with TABLE1 using a custom
   * expectation directory.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads existing orders and items
   *   <li>Execution: Inserts ID=3 (order_id=1, product=Headphones)
   *   <li>Expectation: Verifies order-item relationship from {@code expected-query/} directory
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate order items with custom expectation path")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomExpectationPathsTest/expected-query/"))
  void shouldValidateOrderItems() {
    // Given
    logger.info("Running order items validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (3, 1, 'Headphones', 1, 79.99)
        """);

    // Then
    logger.info("Order items validation completed");
  }

  /**
   * Demonstrates multi-stage workflow testing with custom expectation paths (stage 1).
   *
   * <p>This test represents the first stage of an order lifecycle, validating the initial PENDING
   * state using a stage-specific expectation directory.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (existing order)
   *   <li>Execution: Inserts ID=2 (customer_id=1, amount=150.00, PENDING)
   *   <li>Expectation: Verifies PENDING status from {@code expected-stage1/} directory
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should create order in pending state (stage 1)")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomExpectationPathsTest/expected-stage1/"))
  void shouldCreateOrder() {
    // Given
    logger.info("Running order creation test (stage 1)");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
        """);

    // Then
    logger.info("Order creation test completed");
  }

  /**
   * Demonstrates multi-stage workflow testing with custom expectation paths (stage 2).
   *
   * <p>This test represents order status transition from PENDING to SHIPPED, demonstrating how
   * different expectation directories can validate different workflow stages.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (existing order)
   *   <li>Execution: Creates ID=2 as PENDING, then updates to SHIPPED
   *   <li>Expectation: Verifies SHIPPED status from {@code expected-stage2/} directory
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should ship order and transition to shipped state (stage 2)")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomExpectationPathsTest/expected-stage2/"))
  void shouldShipOrder() {
    // Given
    logger.info("Running order shipment test (stage 2)");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (2, 1, 150.00, '2024-04-10', 'PENDING')
        """);
    executeSql("UPDATE TABLE1 SET COLUMN4 = 'SHIPPED' WHERE ID = 2");

    // Then
    logger.info("Order shipment test completed");
  }
}
