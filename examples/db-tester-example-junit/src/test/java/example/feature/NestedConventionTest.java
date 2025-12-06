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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates @Nested test classes with convention-based data loading.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Using {@code @Nested} classes for logical test grouping
 *   <li>Convention-based CSV resolution for nested classes
 *   <li>Different scenarios within nested test groups
 *   <li>Shared database setup across nested classes
 * </ul>
 *
 * <p>Directory structure:
 *
 * <pre>
 * example/feature/NestedConventionTest/
 *   UserTests/
 *     TABLE1.csv
 *     expected/
 *       TABLE1.csv
 *   ProductTests/
 *     TABLE2.csv
 *     expected/
 *       TABLE2.csv
 * </pre>
 */
@ExtendWith(DatabaseTestExtension.class)
public final class NestedConventionTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(NestedConventionTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates NestedConventionTest instance. */
  public NestedConventionTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for NestedConventionTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/NestedConventionTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:NestedConventionTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(NestedConventionTest.class.getClassLoader().getResource(scriptPath))
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
   * Nested test class for user-related operations.
   *
   * <p>Uses convention-based CSV resolution: files are loaded from {@code
   * classpath:example/feature/NestedConventionTest/UserTests/}.
   */
  @Nested
  class UserTests {

    /** Creates UserTests instance. */
    UserTests() {}

    /**
     * Tests creating a new user with convention-based data loading.
     *
     * <p>CSV files: {@code UserTests/TABLE1.csv} and {@code UserTests/expected/TABLE1.csv}
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads TABLE1(ID=1 john_doe) from UserTests/
     *   <li>Execution: Inserts ID=2 (jane_doe, jane@example.com, true)
     *   <li>Expectation: Verifies both users exist (john_doe and jane_doe)
     * </ul>
     *
     * @throws Exception if database operation fails
     */
    @Test
    @Preparation(
        dataSets =
            @DataSet(
                resourceLocation = "classpath:example/feature/NestedConventionTest/UserTests/",
                scenarioNames = "createUser"))
    @Expectation(
        dataSets =
            @DataSet(
                resourceLocation =
                    "classpath:example/feature/NestedConventionTest/UserTests/expected/",
                scenarioNames = "createUser"))
    void shouldCreateNewUser() throws Exception {
      logger.info("Running nested test: shouldCreateNewUser");

      executeSql(
          """
          INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
          VALUES (2, 'jane_doe', 'jane@example.com', true)
          """);

      logger.info("Nested test completed: shouldCreateNewUser");
    }

    /**
     * Tests updating user status with convention-based data loading.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads TABLE1(ID=1 john_doe, COLUMN3=true) from UserTests/
     *   <li>Execution: Updates ID=1 COLUMN3 from true to false
     *   <li>Expectation: Verifies ID=1 has COLUMN3=false
     * </ul>
     *
     * @throws Exception if database operation fails
     */
    @Test
    @Preparation(
        dataSets =
            @DataSet(
                resourceLocation = "classpath:example/feature/NestedConventionTest/UserTests/",
                scenarioNames = "updateStatus"))
    @Expectation(
        dataSets =
            @DataSet(
                resourceLocation =
                    "classpath:example/feature/NestedConventionTest/UserTests/expected/",
                scenarioNames = "updateStatus"))
    void shouldUpdateUserStatus() throws Exception {
      logger.info("Running nested test: shouldUpdateUserStatus");

      executeSql("UPDATE TABLE1 SET COLUMN3 = false WHERE ID = 1");

      logger.info("Nested test completed: shouldUpdateUserStatus");
    }
  }

  /**
   * Nested test class for product-related operations.
   *
   * <p>Uses convention-based CSV resolution: files are loaded from {@code
   * classpath:example/feature/NestedConventionTest/ProductTests/}.
   */
  @Nested
  class ProductTests {

    /** Creates ProductTests instance. */
    ProductTests() {}

    /**
     * Tests adding a new product with convention-based data loading.
     *
     * <p>CSV files: {@code ProductTests/TABLE2.csv} and {@code ProductTests/expected/TABLE2.csv}
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads TABLE2(ID=1 Laptop, 999.99) from ProductTests/
     *   <li>Execution: Inserts ID=2 (Tablet, 299.99, 15)
     *   <li>Expectation: Verifies both products exist (Laptop and Tablet)
     * </ul>
     *
     * @throws Exception if database operation fails
     */
    @Test
    @Preparation(
        dataSets =
            @DataSet(
                resourceLocation = "classpath:example/feature/NestedConventionTest/ProductTests/",
                scenarioNames = "addProduct"))
    @Expectation(
        dataSets =
            @DataSet(
                resourceLocation =
                    "classpath:example/feature/NestedConventionTest/ProductTests/expected/",
                scenarioNames = "addProduct"))
    void shouldAddNewProduct() throws Exception {
      logger.info("Running nested test: shouldAddNewProduct");

      executeSql(
          """
          INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2, COLUMN3)
          VALUES (2, 'Tablet', 299.99, 15)
          """);

      logger.info("Nested test completed: shouldAddNewProduct");
    }

    /**
     * Tests updating product price with convention-based data loading.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads TABLE2(ID=1 Laptop, COLUMN2=799.99) from ProductTests/
     *   <li>Execution: Updates ID=1 COLUMN2 from 799.99 to 899.99
     *   <li>Expectation: Verifies ID=1 has COLUMN2=899.99
     * </ul>
     *
     * @throws Exception if database operation fails
     */
    @Test
    @Preparation(
        dataSets =
            @DataSet(
                resourceLocation = "classpath:example/feature/NestedConventionTest/ProductTests/",
                scenarioNames = "updatePrice"))
    @Expectation(
        dataSets =
            @DataSet(
                resourceLocation =
                    "classpath:example/feature/NestedConventionTest/ProductTests/expected/",
                scenarioNames = "updatePrice"))
    void shouldUpdateProductPrice() throws Exception {
      logger.info("Running nested test: shouldUpdateProductPrice");

      executeSql("UPDATE TABLE2 SET COLUMN2 = 899.99 WHERE ID = 1");

      logger.info("Nested test completed: shouldUpdateProductPrice");
    }
  }
}
