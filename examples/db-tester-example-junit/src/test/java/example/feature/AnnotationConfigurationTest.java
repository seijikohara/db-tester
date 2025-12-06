package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
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
 * Demonstrates advanced annotation configuration features.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Explicit {@code resourceLocation} specification
 *   <li>Multiple {@code scenarioNames} in a single DataSet
 *   <li>Class-level vs method-level annotation precedence
 *   <li>Custom directory structure
 *   <li>Multiple tables with foreign key relationships
 * </ul>
 *
 * <p>Directory structure:
 *
 * <pre>
 * example/feature/AnnotationConfigurationTest/
 *   custom-location/
 *     TABLE1.csv
 *     TABLE2.csv
 *   expected/
 *     TABLE1.csv
 *     TABLE2.csv
 * </pre>
 */
@ExtendWith(DatabaseTestExtension.class)
@Preparation(
    dataSets =
        @DataSet(
            resourceLocation = "classpath:example/feature/AnnotationConfigurationTest/",
            scenarioNames = "classLevel"))
public final class AnnotationConfigurationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(AnnotationConfigurationTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates AnnotationConfigurationTest instance. */
  public AnnotationConfigurationTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for AnnotationConfigurationTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/AnnotationConfigurationTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:AnnotationConfigurationTest;DB_CLOSE_DELAY=-1");
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
                AnnotationConfigurationTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates explicit resource location specification.
   *
   * <p>Uses custom directory path instead of convention-based resolution.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads from {@code custom-location/} - TABLE1(ID=1,2), TABLE2(ID=1,2)
   *   <li>Execution: Inserts ID=3 (Marketing, Tokyo) into TABLE1
   *   <li>Expectation: Verifies all three departments and two employees exist
   * </ul>
   */
  @Test
  @Preparation(
      dataSets =
          @DataSet(
              resourceLocation =
                  "classpath:example/feature/AnnotationConfigurationTest/custom-location/"))
  @Expectation
  void shouldUseCustomResourceLocation() {
    logger.info("Running custom resource location test");

    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Marketing', 'Tokyo')");

    logger.info("Custom resource location test completed");
  }

  /**
   * Demonstrates multiple scenario names in a single test.
   *
   * <p>Loads rows matching either scenario name from the same CSV files.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads scenario1 and scenario2 - TABLE1(ID=1,2), TABLE2(ID=1,2)
   *   <li>Execution: Updates Bob Smith's salary from 60000.00 to 65000.00
   *   <li>Expectation: Verifies both departments and updated employee salary
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = {"scenario1", "scenario2"}))
  @Expectation
  void shouldHandleMultipleScenarios() {
    logger.info("Running multiple scenarios test");

    executeSql("UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2");

    logger.info("Multiple scenarios test completed");
  }

  /**
   * Demonstrates multiple scenario names in a single {@code @DataSet} for preparation and
   * expectation.
   *
   * <p>This test uses the same pattern as {@link #shouldHandleMultipleScenarios()} to show that
   * combining multiple scenarios in a single {@code @DataSet} annotation correctly merges all
   * matching rows from the CSV files.
   *
   * <p><strong>Note:</strong> When using multiple {@code @DataSet} annotations in an array (e.g.,
   * {@code dataSets = {@DataSet(...), @DataSet(...)}}), each DataSet is processed independently
   * with the specified operation. For {@code CLEAN_INSERT}, this means each subsequent DataSet
   * would clear the table and insert only its data, losing previously inserted rows. To merge data
   * from multiple scenarios, use a single {@code @DataSet} with multiple scenario names as shown
   * here.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads scenario1 and scenario2 - TABLE1(ID=1,2), TABLE2(ID=1,2)
   *   <li>Execution: Updates Bob Smith's salary from 60000.00 to 65000.00
   *   <li>Expectation: Verifies both departments and updated employee salary
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = {"scenario1", "scenario2"}))
  @Expectation(dataSets = @DataSet(scenarioNames = "shouldMergeMultipleDataSets"))
  void shouldMergeMultipleDataSets() {
    logger.info("Running multiple DataSet array test");

    executeSql("UPDATE TABLE2 SET COLUMN3 = 65000.00 WHERE ID = 2");

    logger.info("Multiple DataSet array test completed");
  }

  /**
   * Demonstrates class-level annotation inheritance.
   *
   * <p>This test uses the class-level {@code @Preparation} annotation defined at the class level.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Uses class-level @Preparation with scenario "classLevel" - TABLE1(ID=1 HR),
   *       TABLE2(ID=1 Charlie)
   *   <li>Execution: Inserts new employee (ID=100, New Employee, 45000.00) into TABLE2
   *   <li>Expectation: Verifies HR department and two employees (Charlie + New Employee)
   * </ul>
   */
  @Test
  @Expectation
  void shouldUseClassLevelAnnotation() {
    logger.info("Running class-level annotation test");

    executeSql(
        """
        INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (100, 'New Employee', 1, 45000.00)
        """);

    logger.info("Class-level annotation test completed");
  }

  /**
   * Demonstrates using different scenarios for preparation and expectation.
   *
   * <p>This test shows how preparation and expectation can use different scenario names, allowing
   * for flexible test data setup and verification.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads scenario "multiDataSet1" - TABLE1(ID=1,2,3), TABLE2(ID=1,2)
   *   <li>Execution: Inserts ID=4 (Research, Osaka) into TABLE1
   *   <li>Expectation: Verifies using scenario "multiDataSet" - TABLE1(ID=1,2,3,4), TABLE2(ID=1,2)
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = "multiDataSet1"))
  @Expectation(dataSets = @DataSet(scenarioNames = "multiDataSet"))
  void shouldHandleMultipleDataSets() {
    logger.info("Running multiple DataSets test");

    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
        VALUES (4, 'Research', 'Osaka')
        """);

    logger.info("Multiple DataSets test completed");
  }
}
