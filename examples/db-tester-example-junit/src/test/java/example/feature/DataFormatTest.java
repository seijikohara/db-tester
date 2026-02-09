package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates different data format configurations (AUTO, CSV, TSV, JSON, and YAML).
 *
 * <p>This test demonstrates:
 *
 * <ul>
 *   <li>Using AUTO format (default) with {@link DataFormat#AUTO} for automatic detection
 *   <li>Using CSV format with {@link DataFormat#CSV}
 *   <li>Using TSV format with {@link DataFormat#TSV}
 *   <li>Using JSON format with {@link DataFormat#JSON}
 *   <li>Using YAML format with {@link DataFormat#YAML}
 *   <li>Mixing CSV and JSON in the same directory with AUTO detection
 *   <li>Configuring data format via {@link ConventionSettings}
 * </ul>
 *
 * <p>CSV files use comma as delimiter, TSV files use tab character as delimiter. JSON files use
 * arrays of objects, and YAML files use lists of mappings. AUTO mode detects all formats
 * automatically.
 */
@DisplayName("DataFormatTest")
final class DataFormatTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(DataFormatTest.class);

  /** Creates DataFormatTest instance. */
  DataFormatTest() {}

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
  @DisplayName("CsvFormatTest")
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
          Configuration.builder()
              .conventions(ConventionSettings.builder().dataFormat(DataFormat.CSV).build())
              .build();
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
    @Tag("normal")
    @DisplayName("should load CSV format data correctly")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$CsvFormatTest/shouldLoadCsvFormatData/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$CsvFormatTest/shouldLoadCsvFormatData/expected/")
        })
    void shouldLoadCsvFormatData() {
      // Given
      logger.info("Testing CSV format data loading");

      // When
      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      // Then
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
  @DisplayName("TsvFormatTest")
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
          Configuration.builder()
              .conventions(ConventionSettings.builder().dataFormat(DataFormat.TSV).build())
              .build();
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
    @Tag("normal")
    @DisplayName("should load TSV format data correctly")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$TsvFormatTest/shouldLoadTsvFormatData/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$TsvFormatTest/shouldLoadTsvFormatData/expected/")
        })
    void shouldLoadTsvFormatData() {
      // Given
      logger.info("Testing TSV format data loading");

      // When
      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      // Then
      logger.info("TSV format test completed");
    }
  }

  /**
   * Tests JSON format configuration.
   *
   * <p>JSON files use arrays of objects:
   *
   * <pre>{@code
   * [
   *   {"ID": 1, "NAME": "Alice", "DATA_VALUE": 100},
   *   {"ID": 2, "NAME": "Bob", "DATA_VALUE": 200}
   * ]
   * }</pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("JsonFormatTest")
  class JsonFormatTest {

    /** DataSource for JSON format tests. */
    private static DataSource dataSource;

    /** Creates JsonFormatTest instance. */
    JsonFormatTest() {}

    /**
     * Sets up database with JSON format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for JSON format test");

      // Configure JSON format
      final var jsonConfig =
          Configuration.builder()
              .conventions(ConventionSettings.builder().dataFormat(DataFormat.JSON).build())
              .build();
      DatabaseTestExtension.setConfiguration(context, jsonConfig);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_JSON");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("JSON format test setup completed");
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
     * Verifies that JSON format files are loaded correctly.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads data from JSON file (array of objects)
     *   <li>Execution: Inserts additional record
     *   <li>Expectation: Verifies data from expected JSON file
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should load JSON format data correctly")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$JsonFormatTest/shouldLoadJsonFormatData/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$JsonFormatTest/shouldLoadJsonFormatData/expected/")
        })
    void shouldLoadJsonFormatData() {
      // Given
      logger.info("Testing JSON format data loading");

      // When
      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      // Then
      logger.info("JSON format test completed");
    }
  }

  /**
   * Tests YAML format configuration.
   *
   * <p>YAML files use lists of mappings:
   *
   * <pre>
   * - ID: 1
   *   NAME: Alice
   *   DATA_VALUE: 100
   * - ID: 2
   *   NAME: Bob
   *   DATA_VALUE: 200
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("YamlFormatTest")
  class YamlFormatTest {

    /** DataSource for YAML format tests. */
    private static DataSource dataSource;

    /** Creates YamlFormatTest instance. */
    YamlFormatTest() {}

    /**
     * Sets up database with YAML format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for YAML format test");

      // Configure YAML format
      final var yamlConfig =
          Configuration.builder()
              .conventions(ConventionSettings.builder().dataFormat(DataFormat.YAML).build())
              .build();
      DatabaseTestExtension.setConfiguration(context, yamlConfig);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_YAML");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("YAML format test setup completed");
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
     * Verifies that YAML format files are loaded correctly.
     *
     * <p>Test flow:
     *
     * <ul>
     *   <li>Preparation: Loads data from YAML file (list of mappings)
     *   <li>Execution: Inserts additional record
     *   <li>Expectation: Verifies data from expected YAML file
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should load YAML format data correctly")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$YamlFormatTest/shouldLoadYamlFormatData/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$YamlFormatTest/shouldLoadYamlFormatData/expected/")
        })
    void shouldLoadYamlFormatData() {
      // Given
      logger.info("Testing YAML format data loading");

      // When
      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      // Then
      logger.info("YAML format test completed");
    }
  }

  /**
   * Tests AUTO format (default configuration).
   *
   * <p>AUTO mode automatically detects all supported file formats in the dataset directory. No
   * explicit format configuration is required.
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("AutoFormatTest")
  class AutoFormatTest {

    /** DataSource for AUTO format tests. */
    private static DataSource dataSource;

    /** Creates AutoFormatTest instance. */
    AutoFormatTest() {}

    /**
     * Sets up database with default (AUTO) format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for AUTO format test");

      // AUTO is the default format; no explicit configuration needed
      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_AUTO");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("AUTO format test setup completed");
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
     * Verifies that AUTO format detects CSV files without explicit configuration.
     *
     * <p>This test uses the default AUTO format, which automatically detects CSV files in the
     * dataset directory.
     */
    @Test
    @Tag("normal")
    @DisplayName("should auto-detect CSV format")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$AutoFormatTest/shouldAutoDetectCsvFormat/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$AutoFormatTest/shouldAutoDetectCsvFormat/expected/")
        })
    void shouldAutoDetectCsvFormat() {
      // Given
      logger.info("Testing AUTO format with CSV files");

      // When
      executeSql("INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE) VALUES (3, 'Charlie', 300)");

      // Then
      logger.info("AUTO format CSV detection test completed");
    }
  }

  /**
   * Tests AUTO format with mixed file formats in the same directory.
   *
   * <p>AUTO mode loads CSV and JSON files from the same directory, each mapped to a different
   * table. This demonstrates zero-configuration multi-format support.
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("MixedFormatTest")
  class MixedFormatTest {

    /** DataSource for mixed format tests. */
    private static DataSource dataSource;

    /** Creates MixedFormatTest instance. */
    MixedFormatTest() {}

    /**
     * Sets up database with default (AUTO) format configuration.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for mixed format test");

      // AUTO is the default format; no explicit configuration needed
      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("DataFormatTest_MIXED");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/DataFormatTest.sql");

      logger.info("Mixed format test setup completed");
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
     * Verifies that AUTO format loads both CSV and JSON files from the same directory.
     *
     * <p>The dataset directory contains DATA_FORMAT_CSV.csv and DATA_FORMAT_JSON.json. AUTO mode
     * detects and loads both formats, each mapped to its respective table.
     */
    @Test
    @Tag("normal")
    @DisplayName("should load mixed CSV and JSON formats")
    @DataSet(
        operation = Operation.INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$MixedFormatTest/shouldLoadMixedFormats/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/DataFormatTest$MixedFormatTest/shouldLoadMixedFormats/expected/")
        })
    void shouldLoadMixedFormats() {
      // Given
      logger.info("Testing AUTO format with mixed CSV and JSON files");

      // When
      executeSql("INSERT INTO DATA_FORMAT_CSV (ID, NAME) VALUES (2, 'Bob')");
      executeSql("INSERT INTO DATA_FORMAT_JSON (ID, NAME) VALUES (2, 'Gadget')");

      // Then
      logger.info("Mixed format test completed");
    }
  }
}
