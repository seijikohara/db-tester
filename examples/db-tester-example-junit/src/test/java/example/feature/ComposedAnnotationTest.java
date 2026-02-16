package example.feature;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
 * Demonstrates meta-annotation (composed annotation) support for {@link DataSet} and {@link
 * ExpectedDataSet}.
 *
 * <p>Meta-annotations allow encapsulating reusable {@code @DataSet} and {@code @ExpectedDataSet}
 * configurations into custom annotations. This reduces duplication when multiple test methods share
 * the same dataset locations or column exclusion settings.
 *
 * <p>This test class covers three composition patterns:
 *
 * <ul>
 *   <li>Composed {@code @DataSet}: a custom annotation wrapping {@code @DataSet} with a fixed
 *       resource location
 *   <li>Composed {@code @ExpectedDataSet}: a custom annotation wrapping {@code @ExpectedDataSet}
 *       with column exclusions
 *   <li>Two-level composition: a custom annotation that combines both composed annotations,
 *       requiring the framework to traverse two levels of meta-annotation hierarchy
 * </ul>
 *
 * <p>The framework's {@code AnnotationUtils} discovers these annotations through recursive
 * meta-annotation traversal with cycle detection.
 *
 * @see DataSet
 * @see ExpectedDataSet
 * @see DatabaseTestExtension
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("ComposedAnnotationTest")
final class ComposedAnnotationTest {

  /** Logger instance. */
  private static final Logger logger = LoggerFactory.getLogger(ComposedAnnotationTest.class);

  /** Test database connection. */
  private static DataSource dataSource;

  /** Creates ComposedAnnotationTest instance. */
  ComposedAnnotationTest() {}

  /**
   * Composed annotation that wraps {@link DataSet} to load user seed data.
   *
   * <p>Demonstrates a meta-annotation that encapsulates a specific {@code @DataSet} resource
   * location, reducing repetition across tests that share the same preparation data.
   */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @DataSet(
      sources =
          @DataSetSource(
              resourceLocation = "classpath:example/feature/ComposedAnnotationTest/user-seed/"))
  @interface UserSeedData {}

  /**
   * Composed annotation that wraps {@link ExpectedDataSet} to exclude audit columns.
   *
   * <p>Demonstrates a meta-annotation that encapsulates a specific {@code @ExpectedDataSet}
   * configuration with column exclusions, allowing tests to verify data without matching audit
   * timestamps.
   */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ExpectedDataSet(
      sources =
          @DataSetSource(
              excludeColumns = {"CREATED_AT", "UPDATED_AT"},
              resourceLocation =
                  "classpath:example/feature/ComposedAnnotationTest/verify-users/expected/"))
  @interface VerifyIgnoringAuditColumns {}

  /**
   * Deeply composed annotation combining {@link UserSeedData} and {@link
   * VerifyIgnoringAuditColumns}.
   *
   * <p>Demonstrates two-level meta-annotation traversal: the framework discovers {@code @DataSet}
   * through {@code @UserDataTest} then {@code @UserSeedData} then {@code @DataSet}, and
   * {@code @ExpectedDataSet} through {@code @UserDataTest} then {@code @VerifyIgnoringAuditColumns}
   * then {@code @ExpectedDataSet}.
   */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @UserSeedData
  @VerifyIgnoringAuditColumns
  @interface UserDataTest {}

  /**
   * Sets up H2 in-memory database and schema.
   *
   * @param context extension context
   * @throws Exception if setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up database for ComposedAnnotationTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/ComposedAnnotationTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:ComposedAnnotationTest;DB_CLOSE_DELAY=-1");
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
        Optional.ofNullable(ComposedAnnotationTest.class.getClassLoader().getResource(scriptPath))
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
   * Tests for composed {@link DataSet} annotation behavior.
   *
   * <p>Verifies that the framework discovers {@code @DataSet} through a single level of
   * meta-annotation indirection via {@link UserSeedData}.
   */
  @Nested
  @DisplayName("composed @DataSet annotation")
  class ComposedDataSetAnnotation {

    /** Creates ComposedDataSetAnnotation instance. */
    ComposedDataSetAnnotation() {}

    /**
     * Verifies that the framework loads preparation data via composed {@code @DataSet} annotation.
     *
     * <p>The {@link UserSeedData} meta-annotation carries {@code @DataSet} with a fixed resource
     * location. The framework traverses the annotation hierarchy to discover and apply it. A direct
     * {@code @ExpectedDataSet} verifies that the data was loaded correctly.
     */
    @Test
    @Tag("normal")
    @DisplayName("should load data via composed @DataSet annotation")
    @UserSeedData
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                resourceLocation =
                    "classpath:example/feature/ComposedAnnotationTest/composed-dataset/expected/"))
    void shouldLoadDataViaComposedAnnotation() {
      // Given
      logger.info("Running test with composed @DataSet annotation");

      // When — framework loads data via @UserSeedData meta-annotation

      // Then — framework verifies via direct @ExpectedDataSet
      logger.info("Composed @DataSet annotation test completed");
    }
  }

  /**
   * Tests for composed {@link ExpectedDataSet} annotation behavior.
   *
   * <p>Verifies that the framework discovers {@code @ExpectedDataSet} through a single level of
   * meta-annotation indirection via {@link VerifyIgnoringAuditColumns}.
   */
  @Nested
  @DisplayName("composed @ExpectedDataSet annotation")
  class ComposedExpectedDataSetAnnotation {

    /** Creates ComposedExpectedDataSetAnnotation instance. */
    ComposedExpectedDataSetAnnotation() {}

    /**
     * Verifies that the framework applies column exclusions via composed {@code @ExpectedDataSet}
     * annotation.
     *
     * <p>A direct {@code @DataSet} loads preparation data, and the {@link
     * VerifyIgnoringAuditColumns} meta-annotation carries {@code @ExpectedDataSet} with {@code
     * CREATED_AT} and {@code UPDATED_AT} excluded from comparison.
     */
    @Test
    @Tag("normal")
    @DisplayName("should exclude audit columns via composed @ExpectedDataSet annotation")
    @DataSet(
        sources =
            @DataSetSource(
                resourceLocation = "classpath:example/feature/ComposedAnnotationTest/user-seed/"))
    @VerifyIgnoringAuditColumns
    void shouldExcludeAuditColumnsViaComposedAnnotation() {
      // Given
      logger.info("Running test with composed @ExpectedDataSet annotation");

      // When — framework loads data via direct @DataSet

      // Then — framework verifies via @VerifyIgnoringAuditColumns meta-annotation
      logger.info("Composed @ExpectedDataSet annotation test completed");
    }
  }

  /**
   * Tests for two-level composed annotation behavior.
   *
   * <p>Verifies that the framework discovers both {@code @DataSet} and {@code @ExpectedDataSet}
   * through two levels of meta-annotation indirection via {@link UserDataTest}.
   */
  @Nested
  @DisplayName("deeply composed annotation")
  class DeeplyComposedAnnotation {

    /** Creates DeeplyComposedAnnotation instance. */
    DeeplyComposedAnnotation() {}

    /**
     * Verifies that the framework applies both preparation and expectation via a two-level composed
     * annotation.
     *
     * <p>The {@link UserDataTest} meta-annotation carries {@link UserSeedData} and {@link
     * VerifyIgnoringAuditColumns}, which in turn carry {@code @DataSet} and
     * {@code @ExpectedDataSet}. The framework traverses two levels of meta-annotation hierarchy to
     * discover and apply both annotations.
     */
    @Test
    @Tag("normal")
    @DisplayName("should apply both annotations via deeply composed annotation")
    @UserDataTest
    void shouldApplyBothAnnotationsViaDeeplyComposedAnnotation() {
      // Given
      logger.info("Running test with deeply composed annotation");

      // When — framework loads data via @UserDataTest -> @UserSeedData -> @DataSet

      // Then — framework verifies via @UserDataTest -> @VerifyIgnoringAuditColumns ->
      //        @ExpectedDataSet
      logger.info("Deeply composed annotation test completed");
    }
  }
}
