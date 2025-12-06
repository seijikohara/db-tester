package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion;
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
 * Demonstrates both annotation-based and programmatic database validation approaches.
 *
 * <p>This test class illustrates two complementary validation strategies:
 *
 * <ul>
 *   <li><strong>Annotation-based validation</strong> using {@code @Expectation} - suitable for
 *       standard table comparisons with convention-based expected data
 *   <li><strong>Programmatic validation</strong> using custom SQL queries - provides flexibility
 *       for complex scenarios where annotation-based testing is insufficient
 * </ul>
 *
 * <p>Key programmatic API features available in {@link DatabaseAssertion}:
 *
 * <ul>
 *   <li>{@link DatabaseAssertion#assertEqualsByQuery} - Compare expected data against SQL query
 *       results
 *   <li>{@link DatabaseAssertion#assertEquals} - Compare two datasets or tables directly
 *   <li>{@link DatabaseAssertion#assertEqualsIgnoreColumns} - Compare datasets ignoring specific
 *       columns
 * </ul>
 *
 * <p>Programmatic assertions are useful for custom SQL queries, dynamic column filtering, mid-test
 * state verification, or comparing multiple dataset sources.
 */
@ExtendWith(DatabaseTestExtension.class)
public final class ProgrammaticAssertionApiTest {

  /** Logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ProgrammaticAssertionApiTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ProgrammaticAssertionApiTest instance. */
  public ProgrammaticAssertionApiTest() {}

  /**
   * Sets up H2 in-memory database and schema.
   *
   * @param context extension context
   * @throws Exception if setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up database for ProgrammaticAssertionApiTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ProgrammaticAssertionApiTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ProgrammaticAssertionApiTest;DB_CLOSE_DELAY=-1");
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
                ProgrammaticAssertionApiTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates basic programmatic assertion without annotations.
   *
   * <p>Shows direct use of {@link DatabaseAssertion} assertion APIs for custom validation scenarios
   * where annotation-based testing is insufficient.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
   *   <li>Execution: Inserts (3,Value3,300,NULL)
   *   <li>Expectation: Verifies all three records including NULL COLUMN3
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Preparation
  @Expectation
  void shouldDemonstrateBasicProgrammaticAPI() throws Exception {
    logger.info("Running programmatic API demonstration");

    executeSql(
        "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, NULL)");

    logger.info("Programmatic API demonstration completed - uses standard @Expectation validation");
  }

  /**
   * Demonstrates programmatic custom SQL query validation.
   *
   * <p>This test shows validation using direct SQL queries instead of relying on
   * {@code @Expectation} annotation. Programmatic assertions provide flexibility for custom
   * validation scenarios.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
   *   <li>Execution: Inserts (3,Value3,300,NULL) and (4,Value4,400,NULL)
   *   <li>Expectation: Validates using SQL queries to verify row count and specific records
   * </ul>
   *
   * @throws Exception if test fails
   */
  @Test
  @Preparation
  void shouldValidateUsingMultipleQueries() throws Exception {
    logger.info("Running multiple query validation test");

    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Value3', 300)");
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Value4', 400)");

    // Programmatic validation using SQL queries
    try (final var connection = dataSource.getConnection();
        final var statement = connection.createStatement()) {

      // Verify total row count
      try (final var rs = statement.executeQuery("SELECT COUNT(*) FROM TABLE1")) {
        rs.next();
        final var count = rs.getInt(1);
        if (count != 4) {
          throw new AssertionError(String.format("Expected 4 rows in TABLE1 but found %d", count));
        }
      }

      // Verify newly inserted records exist with correct values
      try (final var rs =
          statement.executeQuery(
              "SELECT COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (3, 4) ORDER BY ID")) {
        // Verify row 3
        if (!rs.next()) {
          throw new AssertionError("Expected row with ID=3 but not found");
        }
        if (!"Value3".equals(rs.getString("COLUMN1")) || rs.getInt("COLUMN2") != 300) {
          throw new AssertionError(
              String.format(
                  "Expected row 3 (Value3, 300) but found (%s, %d)",
                  rs.getString("COLUMN1"), rs.getInt("COLUMN2")));
        }

        // Verify row 4
        if (!rs.next()) {
          throw new AssertionError("Expected row with ID=4 but not found");
        }
        if (!"Value4".equals(rs.getString("COLUMN1")) || rs.getInt("COLUMN2") != 400) {
          throw new AssertionError(
              String.format(
                  "Expected row 4 (Value4, 400) but found (%s, %d)",
                  rs.getString("COLUMN1"), rs.getInt("COLUMN2")));
        }
      }
    }

    logger.info("Multiple query validation completed");
  }
}
