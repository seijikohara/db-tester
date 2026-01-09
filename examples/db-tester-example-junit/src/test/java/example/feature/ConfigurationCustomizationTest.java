package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * Demonstrates customization of framework convention settings.
 *
 * <p>This test demonstrates using {@link DatabaseTestExtension#setConfiguration(ExtensionContext,
 * Configuration)} to customize framework's convention settings while keeping default operations.
 *
 * <p>Customizations demonstrated:
 *
 * <ul>
 *   <li>Custom scenario marker column name: {@code [TestCase]} instead of {@code [Scenario]}
 *   <li>Custom expectation directory suffix: {@code /verify} instead of {@code /expected}
 * </ul>
 *
 * <p>Default convention settings:
 *
 * <ul>
 *   <li>Scenario marker: {@code [Scenario]}
 *   <li>Expectation suffix: {@code /expected}
 * </ul>
 *
 * <p>This test class uses custom conventions while keeping default database operations ({@link
 * Operation#CLEAN_INSERT} for preparation, {@link Operation#NONE} for expectation).
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ConfigurationCustomizationTest")
final class ConfigurationCustomizationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger =
      LoggerFactory.getLogger(ConfigurationCustomizationTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates ConfigurationCustomizationTest instance. */
  ConfigurationCustomizationTest() {}

  /**
   * Sets up H2 in-memory database connection, schema, and custom configuration.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for ConfigurationCustomizationTest");

    // Set custom configuration before registering data source
    final var customConfig =
        Configuration.withConventions(
            new ConventionSettings(
                null, // use classpath-relative resolution
                "/verify", // custom expectation suffix
                "[TestCase]", // custom scenario marker
                DataFormat.CSV, // use CSV format (default)
                TableMergeStrategy.UNION_ALL, // use UNION_ALL merge strategy (default)
                ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                Set.of(),
                Map.of()));
    DatabaseTestExtension.setConfiguration(context, customConfig);

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ConfigurationCustomizationTest.sql");

    logger.info("Database setup completed with custom configuration");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ConfigurationCustomizationTest;DB_CLOSE_DELAY=-1");
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
                ConfigurationCustomizationTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates custom scenario marker usage.
   *
   * <p>CSV files use {@code [TestCase]} column instead of default {@code [Scenario]} to filter rows
   * by test method name.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
   *   <li>Execution: Inserts ID=2 (Bob, ACTIVE, 2024-01-15)
   *   <li>Expectation: Verifies both records exist with correct values
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should use custom scenario marker for row filtering")
  @DataSet
  @ExpectedDataSet
  void shouldUseCustomScenarioMarker() {
    // Given
    logger.info("Running test with custom scenario marker [TestCase]");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (2, 'Bob', 'ACTIVE', '2024-01-15')
        """);

    // Then
    logger.info("Custom scenario marker test completed");
  }

  /**
   * Demonstrates custom expectation suffix usage.
   *
   * <p>Expected data is loaded from {@code /verify} directory instead of default {@code /expected}.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (Alice, ACTIVE, 2024-01-01)
   *   <li>Execution: Updates ID=1 status from ACTIVE to SUSPENDED
   *   <li>Expectation: Verifies status change from {@code verify/TABLE1.csv}
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should use custom expectation suffix for expected data path")
  @DataSet
  @ExpectedDataSet
  void shouldUseCustomExpectationSuffix() {
    // Given
    logger.info("Running test with custom expectation suffix /verify");

    // When
    executeSql("UPDATE TABLE1 SET COLUMN2 = 'SUSPENDED' WHERE ID = 1");

    // Then
    logger.info("Custom expectation suffix test completed");
  }

  /**
   * Demonstrates using default configuration with standard operations.
   *
   * <p>Although the test class customizes scenario marker and expectation suffix, operation
   * defaults remain standard ({@link Operation#CLEAN_INSERT} for preparation).
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (Alice) and ID=2 (Bob) with ACTIVE status
   *   <li>Execution: Inserts ID=3 (Charlie, INACTIVE, 2024-02-01)
   *   <li>Expectation: Verifies all three records exist
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should use custom operation defaults with standard operations")
  @DataSet
  @ExpectedDataSet
  void shouldUseCustomOperationDefaults() {
    // Given
    logger.info("Running test with default operation settings");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Charlie', 'INACTIVE', '2024-02-01')
        """);

    // Then
    logger.info("Test with default operation settings completed");
  }
}
