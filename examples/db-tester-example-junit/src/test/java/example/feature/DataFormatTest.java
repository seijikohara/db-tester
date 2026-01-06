package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
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
 * Demonstrates different data format configurations (CSV and TSV).
 *
 * <p>This test demonstrates:
 *
 * <ul>
 *   <li>Using CSV format (default) with {@link DataFormat#CSV}
 *   <li>Using TSV format with {@link DataFormat#TSV}
 *   <li>Configuring data format via {@link ConventionSettings}
 * </ul>
 *
 * <p>CSV files use comma (,) as delimiter, TSV files use tab character as delimiter.
 */
public final class DataFormatTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(DataFormatTest.class);

  /** Creates DataFormatTest instance. */
  public DataFormatTest() {}

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @param dbName the database name
   * @return configured DataSource
   */
  private static DataSource createDataSource(final String dbName) {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1", dbName));
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
        Optional.ofNullable(DataFormatTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests CSV format (default configuration).
   *
   * <p>CSV files use comma as field delimiter:
   *
   * <pre>
   * ID,NAME,DATA_VALUE
   * 1,Alice,100
   * 2,Bob,200
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class CsvFormatTest {

    /** DataSource for CSV format tests. */
    private static DataSource dataSource;

    /** Creates CsvFormatTest instance. */
    CsvFormatTest() {}

    /**
     * Sets up database with CSV format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for CSV format test");

      // CSV is the default format, but we explicitly configure it for clarity
      final var csvConfig =
          Configuration.withConventions(
              new ConventionSettings(
                  null, // classpath-relative
                  "/expected", // default expectation suffix
                  "[Scenario]", // default scenario marker
                  DataFormat.CSV, // CSV format
                  TableMergeStrategy.UNION_ALL,
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME));
      DatabaseTestExtension.setConfiguration(context, csvConfig);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_CSV");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("CSV format test setup completed");
    }

    /**
     * Executes SQL against the test database.
     *
     * @param sql the SQL to execute
     */
    private void executeSql(final String sql) {
      try (final var connection = dataSource.getConnection();
          final var statement = connection.createStatement()) {
        statement.executeUpdate(sql);
      } catch (final SQLException e) {
        throw new RuntimeException(String.format("Failed to execute SQL: %s", sql), e);
      }
    }

    /**
     * Verifies that CSV format files are loaded correctly.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads data from CSV file (comma-separated)
     *   <li>Execution: Inserts additional record
     *   <li>Expectation: Verifies data from expected CSV file
     * </ul>
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$CsvFormatTest/shouldLoadCsvFormatData/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$CsvFormatTest/shouldLoadCsvFormatData/expected/")
        })
    void shouldLoadCsvFormatData() {
      logger.info("Testing CSV format data loading");

      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      logger.info("CSV format test completed");
    }
  }

  /**
   * Tests TSV format configuration.
   *
   * <p>TSV files use tab as field delimiter:
   *
   * <pre>
   * ID	NAME	DATA_VALUE
   * 1	Alice	100
   * 2	Bob	200
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class TsvFormatTest {

    /** DataSource for TSV format tests. */
    private static DataSource dataSource;

    /** Creates TsvFormatTest instance. */
    TsvFormatTest() {}

    /**
     * Sets up database with TSV format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for TSV format test");

      // Configure TSV format
      final var tsvConfig =
          Configuration.withConventions(
              new ConventionSettings(
                  null, // classpath-relative
                  "/expected", // default expectation suffix
                  "[Scenario]", // default scenario marker
                  DataFormat.TSV, // TSV format
                  TableMergeStrategy.UNION_ALL,
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME));
      DatabaseTestExtension.setConfiguration(context, tsvConfig);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_TSV");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("TSV format test setup completed");
    }

    /**
     * Executes SQL against the test database.
     *
     * @param sql the SQL to execute
     */
    private void executeSql(final String sql) {
      try (final var connection = dataSource.getConnection();
          final var statement = connection.createStatement()) {
        statement.executeUpdate(sql);
      } catch (final SQLException e) {
        throw new RuntimeException(String.format("Failed to execute SQL: %s", sql), e);
      }
    }

    /**
     * Verifies that TSV format files are loaded correctly.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads data from TSV file (tab-separated)
     *   <li>Execution: Inserts additional record
     *   <li>Expectation: Verifies data from expected TSV file
     * </ul>
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$TsvFormatTest/shouldLoadTsvFormatData/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$TsvFormatTest/shouldLoadTsvFormatData/expected/")
        })
    void shouldLoadTsvFormatData() {
      logger.info("Testing TSV format data loading");

      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      logger.info("TSV format test completed");
    }
  }
}
