package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.export.DataSetExporter;
import io.github.seijikohara.dbtester.api.export.ExportConfiguration;
import io.github.seijikohara.dbtester.api.export.LobHandling;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates the {@link DataSetExporter} API for exporting database state to files.
 *
 * <p>This test covers:
 *
 * <ul>
 *   <li>{@link DataSetExporter#csv(DataSource, List, Path)} - Export to CSV format
 *   <li>{@link DataSetExporter#json(DataSource, List, Path)} - Export to JSON format
 *   <li>{@link DataSetExporter#yaml(DataSource, List, Path)} - Export to YAML format
 *   <li>{@link ExportConfiguration} - Custom export settings including {@link LobHandling}
 * </ul>
 *
 * <p>The export API generates dataset files from existing database state. Exported files follow the
 * same format as input dataset files and can be used as expected datasets.
 *
 * @see DataSetExporter
 * @see ExportConfiguration
 * @see LobHandling
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("DataSetExportTest")
final class DataSetExportTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(DataSetExportTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates DataSetExportTest instance. */
  DataSetExportTest() {}

  /**
   * Sets up H2 in-memory database with test data.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for DataSetExportTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/DataSetExportTest.sql");

    // Insert test data
    executeSql("INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (1, 'Alice', 99.99)");
    executeSql("INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (2, 'Bob', 149.50)");
    executeSql("INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (3, 'Charlie', 75.00)");

    logger.info("Database setup completed with test data");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:DataSetExportTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(DataSetExportTest.class.getClassLoader().getResource(scriptPath))
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

  /** Tests for CSV export. */
  @Nested
  @DisplayName("CSV Export")
  class CsvExportTests {

    /** Creates CsvExportTests instance. */
    CsvExportTests() {}

    /**
     * Verifies that database state exports to CSV format.
     *
     * @param tempDir temporary directory for export output
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should export database state to CSV file")
    void shouldExportToCsv(final @TempDir Path tempDir) throws IOException {
      // Given
      logger.info("Testing CSV export to {}", tempDir);
      final var tableNames = List.of("EXPORT_DATA");

      // When
      DataSetExporter.csv(dataSource, tableNames, tempDir);

      // Then
      final var exportedFile = tempDir.resolve("EXPORT_DATA.csv");
      final var content = Files.readString(exportedFile, UTF_8);
      assertAll(
          "CSV export file",
          () -> assertTrue(Files.exists(exportedFile), "exported file should exist"),
          () -> assertTrue(content.contains("Alice"), "should contain Alice"),
          () -> assertTrue(content.contains("Bob"), "should contain Bob"),
          () -> assertTrue(content.contains("Charlie"), "should contain Charlie"));
      logger.info("CSV export test completed");
    }
  }

  /** Tests for JSON export. */
  @Nested
  @DisplayName("JSON Export")
  class JsonExportTests {

    /** Creates JsonExportTests instance. */
    JsonExportTests() {}

    /**
     * Verifies that database state exports to JSON format.
     *
     * @param tempDir temporary directory for export output
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should export database state to JSON file")
    void shouldExportToJson(final @TempDir Path tempDir) throws IOException {
      // Given
      logger.info("Testing JSON export to {}", tempDir);
      final var tableNames = List.of("EXPORT_DATA");

      // When
      DataSetExporter.json(dataSource, tableNames, tempDir);

      // Then
      final var exportedFile = tempDir.resolve("EXPORT_DATA.json");
      final var content = Files.readString(exportedFile, UTF_8);
      assertAll(
          "JSON export file",
          () -> assertTrue(Files.exists(exportedFile), "exported file should exist"),
          () -> assertTrue(content.contains("Alice"), "should contain Alice"),
          () -> assertTrue(content.contains("Bob"), "should contain Bob"));
      logger.info("JSON export test completed");
    }
  }

  /** Tests for YAML export. */
  @Nested
  @DisplayName("YAML Export")
  class YamlExportTests {

    /** Creates YamlExportTests instance. */
    YamlExportTests() {}

    /**
     * Verifies that database state exports to YAML format.
     *
     * @param tempDir temporary directory for export output
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should export database state to YAML file")
    void shouldExportToYaml(final @TempDir Path tempDir) throws IOException {
      // Given
      logger.info("Testing YAML export to {}", tempDir);
      final var tableNames = List.of("EXPORT_DATA");

      // When
      DataSetExporter.yaml(dataSource, tableNames, tempDir);

      // Then
      final var exportedFile = tempDir.resolve("EXPORT_DATA.yaml");
      final var content = Files.readString(exportedFile, UTF_8);
      assertAll(
          "YAML export file",
          () -> assertTrue(Files.exists(exportedFile), "exported file should exist"),
          () -> assertTrue(content.contains("Alice"), "should contain Alice"),
          () -> assertTrue(content.contains("Bob"), "should contain Bob"));
      logger.info("YAML export test completed");
    }
  }

  /** Tests for custom export configuration. */
  @Nested
  @DisplayName("Custom Export Configuration")
  class CustomConfigTests {

    /** Creates CustomConfigTests instance. */
    CustomConfigTests() {}

    /**
     * Verifies that export configuration customizes output behavior.
     *
     * <p>Demonstrates using {@link ExportConfiguration.Builder} to customize null value
     * representation and {@link LobHandling} strategy.
     *
     * @param tempDir temporary directory for export output
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should apply custom export configuration")
    void shouldApplyCustomConfiguration(final @TempDir Path tempDir) throws IOException {
      // Given
      logger.info("Testing custom export configuration");
      final var tableNames = List.of("EXPORT_DATA");
      final var config =
          ExportConfiguration.builder().lobHandling(LobHandling.OMIT).nullValue("[NULL]").build();

      // When
      DataSetExporter.export(dataSource, tableNames, tempDir, DataFormat.CSV, config);

      // Then
      final var exportedFile = tempDir.resolve("EXPORT_DATA.csv");
      assertTrue(Files.exists(exportedFile), "exported file should exist");
      logger.info("Custom export configuration test completed");
    }
  }
}
