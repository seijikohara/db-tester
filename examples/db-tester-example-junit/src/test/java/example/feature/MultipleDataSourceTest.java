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
 * Demonstrates using multiple named data sources in a single test.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Registering multiple named data sources
 *   <li>Using {@code dataSourceName} in {@code @DataSetSource} annotations
 *   <li>Working with different databases simultaneously
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Multi-tenant applications with separate database instances
 *   <li>Microservices with their own databases
 *   <li>Testing data synchronization between databases
 * </ul>
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("MultipleDataSourceTest")
final class MultipleDataSourceTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(MultipleDataSourceTest.class);

  /** Primary database DataSource. */
  private static DataSource primaryDataSource;

  /** Secondary database DataSource. */
  private static DataSource secondaryDataSource;

  /** Creates MultipleDataSourceTest instance. */
  MultipleDataSourceTest() {}

  /**
   * Sets up two H2 in-memory databases.
   *
   * <p>Creates:
   *
   * <ul>
   *   <li>Default database - primary data store
   *   <li>Named database "inventory" - secondary data store
   * </ul>
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabases(final ExtensionContext context) throws Exception {
    logger.info("Setting up multiple H2 in-memory databases");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);

    // Setup primary database (default)
    primaryDataSource = createPrimaryDataSource();
    testRegistry.registerDefault(primaryDataSource);
    executeScript(primaryDataSource, "ddl/feature/MultipleDataSourceTest-primary.sql");

    // Setup secondary database (named "inventory")
    secondaryDataSource = createSecondaryDataSource();
    testRegistry.register("inventory", secondaryDataSource);
    executeScript(secondaryDataSource, "ddl/feature/MultipleDataSourceTest-secondary.sql");

    logger.info("All databases setup completed");
  }

  /**
   * Creates the primary DataSource.
   *
   * @return configured primary DataSource
   */
  private static DataSource createPrimaryDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:MultipleDataSourceTest_Primary;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  /**
   * Creates the secondary DataSource.
   *
   * @return configured secondary DataSource
   */
  private static DataSource createSecondaryDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:MultipleDataSourceTest_Secondary;DB_CLOSE_DELAY=-1");
    dataSource.setUser("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  /**
   * Executes a SQL statement against a specific database.
   *
   * @param dataSource the DataSource to execute against
   * @param sql the SQL statement to execute
   */
  private static void executeSql(final DataSource dataSource, final String sql) {
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
        Optional.ofNullable(MultipleDataSourceTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests operations on the default (primary) database.
   *
   * <p>Uses default dataSourceName (empty string refers to the default data source).
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads default database - TABLE1(ID=1 Alice, ID=2 Bob)
   *   <li>Execution: Inserts ID=3 (Charlie Brown, charlie@example.com) into default database
   *   <li>Expectation: Verifies all three customers exist in default database
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should manage customers in default database")
  @DataSet(
      sources =
          @DataSetSource(
              resourceLocation = "classpath:example/feature/MultipleDataSourceTest/default/",
              scenarioNames = "default"))
  @ExpectedDataSet(
      sources =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/MultipleDataSourceTest/default/expected/",
              scenarioNames = "default"))
  void shouldManageCustomersInDefaultDatabase() throws Exception {
    // Given
    logger.info("Running test on default database");

    // When
    executeSql(
        primaryDataSource,
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
        VALUES (3, 'Charlie Brown', 'charlie@example.com')
        """);

    // Then
    logger.info("Default database test completed");
  }

  /**
   * Tests operations on the named secondary (inventory) database.
   *
   * <p>Uses {@code dataSourceName = "inventory"} to specify the secondary database.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads inventory database - TABLE1(ID=1 Laptop, ID=2 Keyboard)
   *   <li>Execution: Inserts ID=3 (Monitor, 25) into inventory database
   *   <li>Expectation: Verifies all three products exist in inventory database
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should manage products in named inventory database")
  @DataSet(
      sources =
          @DataSetSource(
              dataSourceName = "inventory",
              resourceLocation = "classpath:example/feature/MultipleDataSourceTest/inventory/",
              scenarioNames = "inventory"))
  @ExpectedDataSet(
      sources =
          @DataSetSource(
              dataSourceName = "inventory",
              resourceLocation =
                  "classpath:example/feature/MultipleDataSourceTest/inventory/expected/",
              scenarioNames = "inventory"))
  void shouldManageProductsInInventoryDatabase() throws Exception {
    // Given
    logger.info("Running test on inventory database");

    // When
    executeSql(
        secondaryDataSource,
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
        VALUES (3, 'Monitor', 25)
        """);

    // Then
    logger.info("Inventory database test completed");
  }
}
