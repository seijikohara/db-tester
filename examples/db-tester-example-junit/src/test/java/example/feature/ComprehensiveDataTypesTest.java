package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
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
 * Demonstrates comprehensive data type coverage in CSV files.
 *
 * <p>This test shows all CSV-representable H2 data types:
 *
 * <ul>
 *   <li>Integer types: TINYINT, SMALLINT, INTEGER, BIGINT
 *   <li>Decimal types: DECIMAL, NUMERIC
 *   <li>Floating point: REAL, FLOAT, DOUBLE, DOUBLE PRECISION
 *   <li>Character types: CHAR, VARCHAR, VARCHAR_IGNORECASE, LONGVARCHAR, CLOB, TEXT
 *   <li>Date/Time types: DATE, TIME, TIMESTAMP
 *   <li>Boolean types: BOOLEAN, BIT
 *   <li>Binary type: BLOB (Base64 encoded with {@code [BASE64]} prefix)
 *   <li>UUID values (stored as VARCHAR for CSV compatibility)
 *   <li>NULL value handling (empty column in CSV)
 * </ul>
 *
 * <p>CSV format examples:
 *
 * <pre>{@code
 * ID,TINYINT_COL,CHAR_COL,VARCHAR_COL,DATE_COL,BOOLEAN_COL,BLOB_COL,UUID_COL
 * 1,127,CHAR10,Sample Text,2024-01-15,true,[BASE64]VGVzdA==,550e8400-e29b-41d4-a716-446655440000
 * 2,,ABC,NULL Test,,false,,[null]
 * }</pre>
 *
 * <p>Note: CHAR columns are stored without space padding in CSV. NULL values are represented by
 * empty columns (nothing between commas).
 */
@ExtendWith(DatabaseTestExtension.class)
public final class ComprehensiveDataTypesTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(ComprehensiveDataTypesTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates ComprehensiveDataTypesTest instance. */
  public ComprehensiveDataTypesTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for ComprehensiveDataTypesTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ComprehensiveDataTypesTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ComprehensiveDataTypesTest;DB_CLOSE_DELAY=-1");
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
                ComprehensiveDataTypesTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates handling of all H2 CSV-representable data types.
   *
   * <p>Validates that CSV can represent all data types including integers, decimals, floating
   * points, character types, date/time, booleans, binary (BLOB with Base64), and UUIDs.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads DATA_TYPES(ID=1,2) with comprehensive data type values
   *   <li>Execution: Inserts ID=3 with all 24 data type columns (TINYINT to UUID)
   *   <li>Expectation: Verifies all three records including BLOB (Base64) and CHAR (space-padded)
   * </ul>
   */
  @Test
  @DataSet
  @ExpectedDataSet
  void shouldHandleAllDataTypes() {
    logger.info("Running comprehensive data types test");

    executeSql(
        """
        INSERT INTO DATA_TYPES (
            ID,
            TINYINT_COL, SMALLINT_COL, INT_COL, BIGINT_COL,
            DECIMAL_COL, NUMERIC_COL,
            REAL_COL, FLOAT_COL, DOUBLE_COL, DOUBLE_PRECISION_COL,
            CHAR_COL, VARCHAR_COL, VARCHAR_IGNORECASE_COL, LONGVARCHAR_COL, CLOB_COL, TEXT_COL,
            DATE_COL, TIME_COL, TIMESTAMP_COL,
            BOOLEAN_COL, BIT_COL,
            BLOB_COL,
            UUID_COL
        ) VALUES (
            3,
            127, 32767, 999, 9999999999,
            888.88, 12345.67890,
            123.45, 456.78, 777.77, 888.88,
            'NEWCHAR', 'New Value', 'CaseTest', 'Long variable text', 'CLOB content here', 'Text content',
            '2024-12-31', '23:59:59', '2024-12-31 23:59:59',
            false, false,
            X'DEADBEEF',
            '550e8400-e29b-41d4-a716-446655440099'
        )
        """);

    logger.info("Data types test completed successfully");
  }
}
