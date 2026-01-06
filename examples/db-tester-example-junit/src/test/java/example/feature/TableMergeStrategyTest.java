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
import java.util.Set;
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
 * Demonstrates different table merge strategies when multiple datasets contain the same table.
 *
 * <p>This test demonstrates the four merge strategies:
 *
 * <ul>
 *   <li>{@link TableMergeStrategy#FIRST} - Use only the first occurrence of each table
 *   <li>{@link TableMergeStrategy#LAST} - Use only the last occurrence of each table
 *   <li>{@link TableMergeStrategy#UNION} - Merge tables, removing duplicate rows
 *   <li>{@link TableMergeStrategy#UNION_ALL} - Merge tables, keeping all rows (default)
 * </ul>
 *
 * <p>Each nested test class configures a different merge strategy and verifies the expected
 * behavior when loading multiple datasets that contain the same table.
 */
public final class TableMergeStrategyTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(TableMergeStrategyTest.class);

  /** Creates TableMergeStrategyTest instance. */
  public TableMergeStrategyTest() {}

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
        Optional.ofNullable(TableMergeStrategyTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests FIRST merge strategy.
   *
   * <p>When the same table appears in multiple datasets, only the first occurrence is used.
   *
   * <pre>
   * DataSet 1: USERS table with rows [Alice, Bob]
   * DataSet 2: USERS table with rows [Charlie, Dave]
   * Result:    USERS table with rows [Alice, Bob]
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class FirstStrategyTest {

    /** DataSource for FIRST strategy tests. */
    private static DataSource dataSource;

    /** Creates FirstStrategyTest instance. */
    FirstStrategyTest() {}

    /**
     * Sets up database with FIRST merge strategy.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for FIRST merge strategy test");

      final var config =
          Configuration.withConventions(
              new ConventionSettings(
                  null,
                  "/expected",
                  "[Scenario]",
                  DataFormat.CSV,
                  TableMergeStrategy.FIRST, // FIRST strategy
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  Set.of()));
      DatabaseTestExtension.setConfiguration(context, config);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("TableMergeStrategyTest_FIRST");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TableMergeStrategyTest.sql");

      logger.info("FIRST merge strategy test setup completed");
    }

    /**
     * Verifies FIRST strategy uses only the first dataset's table.
     *
     * <p>Test loads two datasets:
     *
     * <ul>
     *   <li>dataset1: MERGE_TABLE with rows [1=Alice, 2=Bob]
     *   <li>dataset2: MERGE_TABLE with rows [3=Charlie, 4=Dave]
     * </ul>
     *
     * <p>With FIRST strategy, only dataset1's rows should be loaded.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_FirstStrategyTest/dataset1/"),
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_FirstStrategyTest/dataset2/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_FirstStrategyTest/shouldUseOnlyFirstDataset/expected/")
        })
    void shouldUseOnlyFirstDataset() {
      logger.info("Testing FIRST merge strategy - expecting only first dataset rows");
      // No operation needed - just verify the merged result
      logger.info("FIRST merge strategy test completed");
    }
  }

  /**
   * Tests LAST merge strategy.
   *
   * <p>When the same table appears in multiple datasets, only the last occurrence is used.
   *
   * <pre>
   * DataSet 1: USERS table with rows [Alice, Bob]
   * DataSet 2: USERS table with rows [Charlie, Dave]
   * Result:    USERS table with rows [Charlie, Dave]
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class LastStrategyTest {

    /** DataSource for LAST strategy tests. */
    private static DataSource dataSource;

    /** Creates LastStrategyTest instance. */
    LastStrategyTest() {}

    /**
     * Sets up database with LAST merge strategy.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for LAST merge strategy test");

      final var config =
          Configuration.withConventions(
              new ConventionSettings(
                  null,
                  "/expected",
                  "[Scenario]",
                  DataFormat.CSV,
                  TableMergeStrategy.LAST, // LAST strategy
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  Set.of()));
      DatabaseTestExtension.setConfiguration(context, config);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("TableMergeStrategyTest_LAST");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TableMergeStrategyTest.sql");

      logger.info("LAST merge strategy test setup completed");
    }

    /**
     * Verifies LAST strategy uses only the last dataset's table.
     *
     * <p>Test loads two datasets:
     *
     * <ul>
     *   <li>dataset1: MERGE_TABLE with rows [1=Alice, 2=Bob]
     *   <li>dataset2: MERGE_TABLE with rows [3=Charlie, 4=Dave]
     * </ul>
     *
     * <p>With LAST strategy, only dataset2's rows should be loaded.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_LastStrategyTest/dataset1/"),
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_LastStrategyTest/dataset2/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_LastStrategyTest/shouldUseOnlyLastDataset/expected/")
        })
    void shouldUseOnlyLastDataset() {
      logger.info("Testing LAST merge strategy - expecting only last dataset rows");
      // No operation needed - just verify the merged result
      logger.info("LAST merge strategy test completed");
    }
  }

  /**
   * Tests UNION merge strategy.
   *
   * <p>Merges all tables, removing duplicate rows (like SQL UNION).
   *
   * <pre>
   * DataSet 1: USERS table with rows [Alice, Bob]
   * DataSet 2: USERS table with rows [Bob, Charlie]
   * Result:    USERS table with rows [Alice, Bob, Charlie]
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class UnionStrategyTest {

    /** DataSource for UNION strategy tests. */
    private static DataSource dataSource;

    /** Creates UnionStrategyTest instance. */
    UnionStrategyTest() {}

    /**
     * Sets up database with UNION merge strategy.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for UNION merge strategy test");

      final var config =
          Configuration.withConventions(
              new ConventionSettings(
                  null,
                  "/expected",
                  "[Scenario]",
                  DataFormat.CSV,
                  TableMergeStrategy.UNION, // UNION strategy
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  Set.of()));
      DatabaseTestExtension.setConfiguration(context, config);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("TableMergeStrategyTest_UNION");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TableMergeStrategyTest.sql");

      logger.info("UNION merge strategy test setup completed");
    }

    /**
     * Verifies UNION strategy merges tables and removes duplicates.
     *
     * <p>Test loads two datasets with overlapping data:
     *
     * <ul>
     *   <li>dataset1: MERGE_TABLE with rows [1=Alice, 2=Bob]
     *   <li>dataset2: MERGE_TABLE with rows [2=Bob, 3=Charlie]
     * </ul>
     *
     * <p>With UNION strategy, duplicate row [2=Bob] should appear only once.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionStrategyTest/dataset1/"),
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionStrategyTest/dataset2/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionStrategyTest/shouldMergeAndRemoveDuplicates/expected/")
        })
    void shouldMergeAndRemoveDuplicates() {
      logger.info("Testing UNION merge strategy - expecting merged rows without duplicates");
      // No operation needed - just verify the merged result
      logger.info("UNION merge strategy test completed");
    }
  }

  /**
   * Tests UNION_ALL merge strategy (default).
   *
   * <p>Merges all tables, keeping all rows including duplicates (like SQL UNION ALL).
   *
   * <pre>
   * DataSet 1: USERS table with rows [Alice, Bob]
   * DataSet 2: USERS table with rows [Bob, Charlie]
   * Result:    USERS table with rows [Alice, Bob, Bob, Charlie]
   * </pre>
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  class UnionAllStrategyTest {

    /** DataSource for UNION_ALL strategy tests. */
    private static DataSource dataSource;

    /** Creates UnionAllStrategyTest instance. */
    UnionAllStrategyTest() {}

    /**
     * Sets up database with UNION_ALL merge strategy.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for UNION_ALL merge strategy test");

      final var config =
          Configuration.withConventions(
              new ConventionSettings(
                  null,
                  "/expected",
                  "[Scenario]",
                  DataFormat.CSV,
                  TableMergeStrategy.UNION_ALL, // UNION_ALL strategy (default)
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  Set.of()));
      DatabaseTestExtension.setConfiguration(context, config);

      final var registry = DatabaseTestExtension.getRegistry(context);
      dataSource = createDataSource("TableMergeStrategyTest_UNION_ALL");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TableMergeStrategyTest.sql");

      logger.info("UNION_ALL merge strategy test setup completed");
    }

    /**
     * Verifies UNION_ALL strategy merges tables and keeps all rows.
     *
     * <p>Test loads two datasets with overlapping data:
     *
     * <ul>
     *   <li>dataset1: MERGE_TABLE with rows [1=Alice, 2=Bob]
     *   <li>dataset2: MERGE_TABLE with rows [2=Bob, 3=Charlie]
     * </ul>
     *
     * <p>With UNION_ALL strategy, all rows including duplicate [2=Bob] should be kept. Note: This
     * may cause primary key violations if the table has a primary key constraint on ID.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionAllStrategyTest/dataset1/"),
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionAllStrategyTest/dataset2/")
        })
    @ExpectedDataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableMergeStrategyTest_UnionAllStrategyTest/shouldMergeAndKeepAllRows/expected/")
        })
    void shouldMergeAndKeepAllRows() {
      logger.info("Testing UNION_ALL merge strategy - expecting all rows including duplicates");
      // No operation needed - just verify the merged result
      logger.info("UNION_ALL merge strategy test completed");
    }
  }
}
