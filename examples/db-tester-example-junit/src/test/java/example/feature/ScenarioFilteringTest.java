package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
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
 * Demonstrates scenario-based testing with CSV row filtering.
 *
 * <p>This test illustrates the scenario filtering feature that enables sharing a single CSV file
 * across multiple test methods. Each test automatically loads only rows matching its method name
 * from the {@code [Scenario]} marker column.
 *
 * <p>Features demonstrated:
 *
 * <ul>
 *   <li>Using scenario marker column for row filtering
 *   <li>Sharing a single CSV file across multiple test methods
 *   <li>Test method name as automatic scenario filter
 *   <li>Reducing CSV file duplication
 *   <li>Maintaining related test data in one place
 *   <li>Class-level {@code @Preparation} and {@code @Expectation} annotations
 * </ul>
 *
 * <p>CSV files contain scenario marker column that filters rows by test method name:
 *
 * <pre>
 * [Scenario],ID,COLUMN1,COLUMN2,COLUMN3
 * shouldCreateActiveUser,1,alice,alice@example.com,ACTIVE
 * shouldCreateInactiveUser,1,bob,bob@example.com,INACTIVE
 * </pre>
 *
 * <p>This test uses class-level annotations in contrast to {@link MinimalExampleTest} which
 * demonstrates method-level annotation usage.
 */
@ExtendWith(DatabaseTestExtension.class)
@Preparation
@Expectation
public final class ScenarioFilteringTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(ScenarioFilteringTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates ScenarioFilteringTest instance. */
  public ScenarioFilteringTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for ScenarioFilteringTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ScenarioFilteringTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ScenarioFilteringTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(ScenarioFilteringTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates scenario filtering for active user creation.
   *
   * <p>Only rows matching the test method name are loaded from TABLE1.csv using the {@code
   * [Scenario]} marker column.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (alice, ACTIVE) filtered by scenario name
   *   <li>Execution: Inserts ID=2 (charlie, ACTIVE)
   *   <li>Expectation: Verifies both records exist with ACTIVE status
   * </ul>
   *
   * <p>Note: {@code @Preparation} and {@code @Expectation} are applied at the class level.
   */
  @Test
  void shouldCreateActiveUser() {
    logger.info("Running scenario test: shouldCreateActiveUser");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (2, 'charlie', 'charlie@example.com', 'ACTIVE')
        """);

    logger.info("Active user created successfully");
  }

  /**
   * Demonstrates scenario filtering for inactive user creation.
   *
   * <p>Only rows matching the test method name are loaded from TABLE1.csv using the {@code
   * [Scenario]} marker column.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (bob, INACTIVE) filtered by scenario name
   *   <li>Execution: Inserts ID=2 (david, INACTIVE)
   *   <li>Expectation: Verifies both records exist with INACTIVE status
   * </ul>
   *
   * <p>Note: {@code @Preparation} and {@code @Expectation} are applied at the class level.
   */
  @Test
  void shouldCreateInactiveUser() {
    logger.info("Running scenario test: shouldCreateInactiveUser");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (2, 'david', 'david@example.com', 'INACTIVE')
        """);

    logger.info("Inactive user created successfully");
  }

  /**
   * Demonstrates scenario filtering with multiple existing users.
   *
   * <p>This test loads multiple rows from the same CSV file using scenario filtering and updates
   * one of them.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads ID=1 (eve, ACTIVE) and ID=2 (frank, INACTIVE)
   *   <li>Execution: Updates ID=2 status from INACTIVE to SUSPENDED
   *   <li>Expectation: Verifies ID=1 remains ACTIVE and ID=2 is SUSPENDED
   * </ul>
   *
   * <p>Note: {@code @Preparation} and {@code @Expectation} are applied at the class level.
   */
  @Test
  void shouldHandleMultipleUsers() {
    logger.info("Running scenario test: shouldHandleMultipleUsers");

    executeSql("UPDATE TABLE1 SET COLUMN3 = 'SUSPENDED' WHERE ID = 2");

    logger.info("User status updated successfully");
  }
}
