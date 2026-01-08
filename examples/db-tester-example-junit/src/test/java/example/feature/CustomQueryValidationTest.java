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
 * Demonstrates database testing with custom query validation scenarios.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Testing INSERT operations with data
 *   <li>Using custom expectation paths for different scenarios
 *   <li>Validating filtered data
 *   <li>Testing aggregation scenarios
 *   <li>Validating date-range queries
 * </ul>
 *
 * <p>Note: For actual SQL query result validation using {@code
 * DatabaseAssertion.assertEqualsByQuery}, you would need to programmatically create expected
 * datasets using DbUnit APIs.
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
   * Demonstrates validation with filtered data.
   *
   * <p>Validates data after adding new record with specific filter criteria.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data
   *   <li>Execution: Inserts ID=4 (COLUMN1=3, East region, 2024-01-25, 350.00)
   *   <li>Expectation: Verifies all four records from {@code expected-filtered/}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate regional sales with filtered data")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomQueryValidationTest/expected-filtered/"))
  void shouldValidateRegionalSales() {
    // Given
    logger.info("Running regional sales validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 3, '2024-01-25', 350.00, 'East')
        """);

    // Then
    logger.info("Regional sales validation completed");
  }

  /**
   * Demonstrates validation with aggregated data.
   *
   * <p>Validates aggregated data after adding new record.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data
   *   <li>Execution: Inserts ID=4 (COLUMN1=1, West region, 2024-01-25, 500.00)
   *   <li>Expectation: Verifies all four records from {@code expected-aggregation/}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate sales summary with aggregated data")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomQueryValidationTest/expected-aggregation/"))
  void shouldValidateSalesSummary() {
    // Given
    logger.info("Running sales summary validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 1, '2024-01-25', 500.00, 'West')
        """);

    // Then
    logger.info("Sales summary validation completed");
  }

  /**
   * Demonstrates validation with high-value records.
   *
   * <p>Validates data after adding a high-value record.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1,2,3) with sales data (January)
   *   <li>Execution: Inserts ID=4 (COLUMN1=1, North region, 2024-02-01, 600.00)
   *   <li>Expectation: Verifies all four records including February data from {@code
   *       expected-join/}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate high-value sales with join data")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomQueryValidationTest/expected-join/"))
  void shouldValidateHighValueSales() {
    // Given
    logger.info("Running high-value sales validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 1, '2024-02-01', 600.00, 'North')
        """);

    // Then
    logger.info("High-value sales validation completed");
  }

  /**
   * Demonstrates validation with date range filtering for January sales.
   *
   * <p>Validates that only January sales data is present in the database by adding a January record
   * and verifying the final state contains only January data.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads TABLE1(ID=1,2,3) with January sales data
   *   <li>Execution: Inserts ID=4 (COLUMN1=2, South region, 2024-01-25, 450.00)
   *   <li>Expectation: Verifies all four January records from {@code expected-daterange/}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate January sales with date range filtering")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/CustomQueryValidationTest/expected-daterange/"))
  void shouldValidateJanuarySales() {
    // Given
    logger.info("Running January sales validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3, COLUMN4)
        VALUES (4, 2, '2024-01-25', 450.00, 'South')
        """);

    // Then
    logger.info("January sales validation completed");
  }
}
