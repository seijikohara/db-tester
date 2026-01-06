package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class demonstrating annotation inheritance for database tests.
 *
 * <p>This base class provides:
 *
 * <ul>
 *   <li>Shared {@code @ExtendWith(DatabaseTestExtension.class)} configuration
 *   <li>Class-level {@code @DataSet} annotation inherited by subclasses
 *   <li>Common database setup and utility methods
 *   <li>Reusable test infrastructure
 * </ul>
 *
 * <p>Child classes inherit:
 *
 * <ul>
 *   <li>The {@code @ExtendWith} annotation
 *   <li>The class-level {@code @DataSet} annotation
 *   <li>Database setup and helper methods
 * </ul>
 *
 * @see InheritedAnnotationTest
 */
@ExtendWith(DatabaseTestExtension.class)
@DataSet(
    dataSets =
        @DataSetSource(
            resourceLocation = "classpath:example/feature/InheritanceTestBase/",
            scenarioNames = "baseSetup"))
public abstract class InheritanceTestBase {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(InheritanceTestBase.class);

  /** DataSource for test database operations. */
  protected static DataSource dataSource;

  /** Creates InheritanceTestBase instance. */
  protected InheritanceTestBase() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * <p>This method is inherited by subclasses and provides shared database initialization.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for InheritanceTestBase");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/InheritanceTestBase.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:InheritanceTestBase;DB_CLOSE_DELAY=-1");
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
  protected static void executeScript(final DataSource dataSource, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(InheritanceTestBase.class.getClassLoader().getResource(scriptPath))
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
   * Gets the count of records in a table.
   *
   * @param tableName the table name
   * @return the record count
   * @throws Exception if query fails
   */
  protected int getRecordCount(final String tableName) throws Exception {
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement();
        final var resultSet =
            statement.executeQuery(String.format("SELECT COUNT(*) FROM %s", tableName))) {
      resultSet.next();
      return resultSet.getInt(1);
    }
  }
}
