package example.feature;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates annotation inheritance from a base test class.
 *
 * <p>This test class inherits:
 *
 * <ul>
 *   <li>{@code @ExtendWith(DatabaseTestExtension.class)} from {@link InheritanceTestBase}
 *   <li>Class-level {@code @DataSet} annotation from {@link InheritanceTestBase}
 *   <li>Database setup and utility methods
 * </ul>
 *
 * <p>Each test method automatically uses the base class's {@code @DataSet} unless overridden at the
 * method level.
 *
 * <p>Directory structure:
 *
 * <pre>
 * example/feature/InheritanceTestBase/
 *   TABLE1.csv          (base setup data)
 *   expected/
 *     TABLE1.csv
 * example/feature/InheritedAnnotationTest/
 *   expected/
 *     TABLE1.csv        (child class specific expectations)
 * </pre>
 */
@DisplayName("InheritedAnnotationTest")
final class InheritedAnnotationTest extends InheritanceTestBase {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(InheritedAnnotationTest.class);

  /** Creates InheritedAnnotationTest instance. */
  InheritedAnnotationTest() {}

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
   * Tests using inherited class-level @DataSet annotation.
   *
   * <p>This test uses the {@code @DataSet} from {@link InheritanceTestBase} automatically.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Uses inherited @DataSet (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
   *   <li>Execution: Inserts ID=3 (Monitor, 20, Warehouse B)
   *   <li>Expectation: Verifies all three products exist (Laptop, Keyboard, Monitor)
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should use inherited class-level @DataSet annotation")
  @ExpectedDataSet
  void shouldUseInheritedPreparation() throws Exception {
    // Given
    logger.info("Running test with inherited @DataSet");

    // When
    executeSql(
        """
        INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
        VALUES (3, 'Monitor', 20, 'Warehouse B')
        """);

    // Then
    final var count = getRecordCount("TABLE1");
    logger.info("Record count after insert: {}", count);

    logger.info("Test with inherited @DataSet completed");
  }

  /**
   * Tests overriding inherited @DataSet with method-level annotation.
   *
   * <p>The method-level {@code @DataSet} takes precedence over the inherited class-level
   * annotation.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Uses method-level @DataSet (overrideSetup) - TABLE1(ID=1 Laptop, COLUMN2=30)
   *   <li>Execution: Updates ID=1 COLUMN2 from 30 to 50
   *   <li>Expectation: Verifies ID=1 has COLUMN2=50
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should override inherited @DataSet with method-level annotation")
  @DataSet(sources = @DataSetSource(scenarioNames = "overrideSetup"))
  @ExpectedDataSet(sources = @DataSetSource(scenarioNames = "overrideSetup"))
  void shouldOverrideInheritedPreparation() throws Exception {
    // Given
    logger.info("Running test with overridden @DataSet");

    // When
    executeSql("UPDATE TABLE1 SET COLUMN2 = 50 WHERE ID = 1");

    // Then
    logger.info("Test with overridden @DataSet completed");
  }

  /**
   * Tests combining inherited and method-level expectations.
   *
   * <p>Uses inherited {@code @DataSet} but adds method-level {@code @ExpectedDataSet}.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Uses inherited @DataSet (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
   *   <li>Execution: Updates Laptop's COLUMN3 from 'Warehouse A' to 'Warehouse C'
   *   <li>Expectation: Verifies ID=1 has COLUMN3='Warehouse C', ID=2 unchanged
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should combine inherited @DataSet with method-level @ExpectedDataSet")
  @ExpectedDataSet(sources = @DataSetSource(scenarioNames = "combinedTest"))
  void shouldCombineInheritedAndMethodLevelAnnotations() throws Exception {
    // Given
    logger.info("Running test with combined annotations");

    // When
    executeSql(
        """
        UPDATE TABLE1 SET COLUMN3 = 'Warehouse C' WHERE COLUMN1 = 'Laptop'
        """);

    // Then
    logger.info("Test with combined annotations completed");
  }
}
