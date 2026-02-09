package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.annotation.Strategy;
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
 * Demonstrates template expression processing in CSV dataset values.
 *
 * <p>Template expressions generate dynamic values at data load time. This test covers:
 *
 * <ul>
 *   <li>{@code ${uuid}} - Random UUID generation
 *   <li>{@code ${sequence:N}} and {@code ${sequence}} - Auto-incrementing sequence numbers
 *   <li>{@code ${now}} and {@code ${now+Xd}} - Current and relative timestamp generation
 *   <li>{@code ${faker.xxx.yyy}} - Datafaker integration for realistic test data
 * </ul>
 *
 * <p>Dynamic values are validated using {@link Strategy#REGEX} and {@link Strategy#NOT_NULL}
 * comparison strategies in the expected dataset.
 *
 * @see Strategy
 * @see ColumnStrategy
 */
final class TemplateExpressionTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(TemplateExpressionTest.class);

  /** Creates TemplateExpressionTest instance. */
  TemplateExpressionTest() {}

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
        Optional.ofNullable(TemplateExpressionTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests {@code ${uuid}} template expression.
   *
   * <p>The {@code ${uuid}} expression generates a random UUID (version 4) for each occurrence. Each
   * invocation produces a unique value. The expected dataset uses {@link Strategy#REGEX} to
   * validate the UUID format.
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("UUID Expression")
  class UuidExpressionTest {

    /** Creates UuidExpressionTest instance. */
    UuidExpressionTest() {}

    /**
     * Sets up database for UUID expression tests.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for UUID expression test");

      final var registry = DatabaseTestExtension.getRegistry(context);
      final var dataSource = createDataSource("TemplateExpressionTest_UUID");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TemplateExpressionTest.sql");

      logger.info("UUID expression test setup completed");
    }

    /**
     * Verifies that {@code ${uuid}} generates valid UUID values.
     *
     * <p>The input CSV contains {@code ${uuid}} placeholders. The expected dataset validates UUID
     * format using {@link Strategy#REGEX} with a UUID pattern.
     */
    @Test
    @Tag("normal")
    @DisplayName("should generate valid UUID values from ${uuid} expression")
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$UuidExpressionTest/shouldGenerateUuid/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$UuidExpressionTest/shouldGenerateUuid/expected/",
              columnStrategies = {
                @ColumnStrategy(
                    name = "UUID_VALUE",
                    strategy = Strategy.REGEX,
                    pattern = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")
              })
        })
    void shouldGenerateUuid() {
      // Given & When - data loaded via @DataSet with ${uuid} expressions

      // Then - UUID format validated via REGEX strategy
      logger.info("UUID expression test completed");
    }
  }

  /**
   * Tests {@code ${sequence}} template expressions.
   *
   * <p>The {@code ${sequence:N}} expression sets the counter to N and returns N. The {@code
   * ${sequence}} expression increments the counter and returns the next value. Sequence values are
   * deterministic within a single dataset load.
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("Sequence Expression")
  class SequenceExpressionTest {

    /** Creates SequenceExpressionTest instance. */
    SequenceExpressionTest() {}

    /**
     * Sets up database for sequence expression tests.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for sequence expression test");

      final var registry = DatabaseTestExtension.getRegistry(context);
      final var dataSource = createDataSource("TemplateExpressionTest_Sequence");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TemplateExpressionTest.sql");

      logger.info("Sequence expression test setup completed");
    }

    /**
     * Verifies that {@code ${sequence:N}} and {@code ${sequence}} generate sequential values.
     *
     * <p>The input CSV contains:
     *
     * <ul>
     *   <li>Row 1: {@code ${sequence:1}} sets counter to 1 (ID=1), {@code ${sequence}} increments
     *       to 2 (NAME=User_2)
     *   <li>Row 2: {@code ${sequence}} increments to 3 (ID=3), {@code ${sequence}} increments to 4
     *       (NAME=User_4)
     * </ul>
     */
    @Test
    @Tag("normal")
    @DisplayName("should generate sequential values from ${sequence} expressions")
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$SequenceExpressionTest/shouldGenerateSequence/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$SequenceExpressionTest/shouldGenerateSequence/expected/")
        })
    void shouldGenerateSequence() {
      // Given & When - data loaded via @DataSet with ${sequence} expressions

      // Then - deterministic values verified via STRICT comparison
      logger.info("Sequence expression test completed");
    }
  }

  /**
   * Tests {@code ${now}} template expressions.
   *
   * <p>The {@code ${now}} expression generates the current timestamp in ISO-8601 format. The {@code
   * ${now+Xd}} and {@code ${now-Xd}} expressions generate relative timestamps (d=days, h=hours,
   * m=minutes, s=seconds).
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("Timestamp Expression")
  class TimestampExpressionTest {

    /** Creates TimestampExpressionTest instance. */
    TimestampExpressionTest() {}

    /**
     * Sets up database for timestamp expression tests.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for timestamp expression test");

      final var registry = DatabaseTestExtension.getRegistry(context);
      final var dataSource = createDataSource("TemplateExpressionTest_Timestamp");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TemplateExpressionTest.sql");

      logger.info("Timestamp expression test setup completed");
    }

    /**
     * Verifies that {@code ${now}} and {@code ${now+1d}} generate timestamp values.
     *
     * <p>The input CSV contains {@code ${now}} and {@code ${now+1d}} placeholders. The expected
     * dataset uses {@link Strategy#NOT_NULL} to validate that timestamps were generated.
     */
    @Test
    @Tag("normal")
    @DisplayName("should generate timestamp values from ${now} expressions")
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$TimestampExpressionTest/shouldGenerateTimestamp/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$TimestampExpressionTest/shouldGenerateTimestamp/expected/",
              columnStrategies = {
                @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.NOT_NULL)
              })
        })
    void shouldGenerateTimestamp() {
      // Given & When - data loaded via @DataSet with ${now} expressions

      // Then - timestamps validated via NOT_NULL strategy
      logger.info("Timestamp expression test completed");
    }
  }

  /**
   * Tests {@code ${faker.xxx.yyy}} template expressions.
   *
   * <p>The {@code ${faker.xxx.yyy}} expression uses the Datafaker library to generate realistic
   * test data. Datafaker must be on the classpath as a runtime dependency. If absent, the
   * expression is left unprocessed.
   */
  @Nested
  @ExtendWith(DatabaseTestExtension.class)
  @DisplayName("Faker Expression")
  class FakerExpressionTest {

    /** Creates FakerExpressionTest instance. */
    FakerExpressionTest() {}

    /**
     * Sets up database for Datafaker expression tests.
     *
     * @param context the extension context
     * @throws Exception if setup fails
     */
    @BeforeAll
    static void setupDatabase(final ExtensionContext context) throws Exception {
      logger.info("Setting up database for Datafaker expression test");

      final var registry = DatabaseTestExtension.getRegistry(context);
      final var dataSource = createDataSource("TemplateExpressionTest_Faker");
      registry.registerDefault(dataSource);
      executeScript(dataSource, "ddl/feature/TemplateExpressionTest.sql");

      logger.info("Datafaker expression test setup completed");
    }

    /**
     * Verifies that {@code ${faker.name.fullName}} generates non-null name values.
     *
     * <p>The input CSV contains {@code ${faker.name.fullName}} placeholders. The expected dataset
     * uses {@link Strategy#NOT_NULL} to validate that names were generated by Datafaker.
     */
    @Test
    @Tag("normal")
    @DisplayName("should generate realistic data from ${faker} expressions")
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$FakerExpressionTest/shouldGenerateFakerData/")
        })
    @ExpectedDataSet(
        sources = {
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TemplateExpressionTest$FakerExpressionTest/shouldGenerateFakerData/expected/",
              columnStrategies = {@ColumnStrategy(name = "NAME", strategy = Strategy.NOT_NULL)})
        })
    void shouldGenerateFakerData() {
      // Given & When - data loaded via @DataSet with ${faker} expressions

      // Then - names validated via NOT_NULL strategy
      logger.info("Datafaker expression test completed");
    }
  }
}
