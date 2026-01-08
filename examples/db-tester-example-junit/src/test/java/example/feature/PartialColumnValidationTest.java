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
 * Demonstrates partial column validation techniques using CSV files.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Validating only specific columns via partial CSV files
 *   <li>Excluding auto-generated columns (ID, timestamps) from CSV expectations
 *   <li>Testing business logic without worrying about database-generated values
 *   <li>Using custom expectation paths for different validation scenarios
 * </ul>
 *
 * <p>Use partial column validation when:
 *
 * <ul>
 *   <li>Testing tables with auto-increment IDs
 *   <li>Ignoring timestamp columns (CREATED_AT, UPDATED_AT)
 *   <li>Focusing on business-relevant columns only
 *   <li>Dealing with database-generated values (UUIDs, sequences)
 * </ul>
 *
 * <p>Note: For programmatic column exclusion using {@code
 * DatabaseAssertion.assertEqualsIgnoreColumns}, you would need to manually create datasets using
 * DbUnit APIs.
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("PartialColumnValidationTest")
final class PartialColumnValidationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(PartialColumnValidationTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates PartialColumnValidationTest instance. */
  PartialColumnValidationTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for PartialColumnValidationTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/PartialColumnValidationTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:PartialColumnValidationTest;DB_CLOSE_DELAY=-1");
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
                PartialColumnValidationTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates partial column validation using CSV with subset of columns.
   *
   * <p>CSV contains only business-relevant columns, ignoring auto-generated ID and timestamp.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
   *   <li>Execution: Inserts (DELETE,User,789) - ID and COLUMN4/5 auto-generated
   *   <li>Expectation: Verifies all three records exist with expected COLUMN1/2/3 values
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate partial columns via CSV with subset of columns")
  @DataSet
  @ExpectedDataSet
  void shouldValidatePartialColumnsViaCSV() throws Exception {
    // Given
    logger.info("Running partial column validation via CSV test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
        VALUES ('DELETE', 'User', 789)
        """);

    // Then
    logger.info("Partial column validation via CSV completed");
  }

  /**
   * Demonstrates validation with partial CSV (ignoring auto-generated columns).
   *
   * <p>CSV file contains only business columns, excluding ID, COLUMN4, and COLUMN5.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
   *   <li>Execution: Inserts (UPDATE,Product,456) - same values but different auto-generated ID
   *   <li>Expectation: Verifies three records with matching COLUMN1/2/3, ignoring ID differences
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should ignore auto-generated columns like ID and timestamps")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/PartialColumnValidationTest/expected-ignore-columns/"))
  void shouldIgnoreAutoGeneratedColumns() throws Exception {
    // Given
    logger.info("Running ignore auto-generated columns test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
        VALUES ('UPDATE', 'Product', 456)
        """);

    // Then
    logger.info("Ignore auto-generated columns test completed");
  }

  /**
   * Demonstrates validation with minimal CSV columns including default value verification.
   *
   * <p>CSV contains essential business columns plus COLUMN5 to verify DEFAULT 'SYSTEM' value.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
   *   <li>Execution: Inserts (CREATE,Order,999) - COLUMN5 defaults to 'SYSTEM'
   *   <li>Expectation: Verifies COLUMN1/2/3/5 values including default COLUMN5='SYSTEM'
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate with minimal columns including default value verification")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/PartialColumnValidationTest/expected-combined/"))
  void shouldValidateWithMinimalColumns() throws Exception {
    // Given
    logger.info("Running minimal columns validation test");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
        VALUES ('CREATE', 'Order', 999)
        """);

    // Then
    logger.info("Minimal columns validation test completed");
  }

  /**
   * Demonstrates validation after UPDATE operation.
   *
   * <p>Note: This test validates the complete table state after an update operation. True partial
   * column validation (validating only specific columns while ignoring others) requires
   * programmatic assertions using {@code DatabaseAssertion.assertEqualsIgnoreColumns}, which is
   * beyond the scope of annotation-based testing.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
   *   <li>Execution: Updates ID=1 COLUMN3 from 123 to 555
   *   <li>Expectation: Verifies (CREATE,User,555), (UPDATE,Product,456) with updated value
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should validate database state after UPDATE operation")
  @DataSet
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/PartialColumnValidationTest/expected-after-update/"))
  void shouldValidateAfterUpdate() throws Exception {
    // Given
    logger.info("Running validate after update test");

    // When
    executeSql("UPDATE TABLE1 SET COLUMN3 = 555 WHERE ID = 1");

    // Then
    logger.info("Validate after update test completed");
  }
}
