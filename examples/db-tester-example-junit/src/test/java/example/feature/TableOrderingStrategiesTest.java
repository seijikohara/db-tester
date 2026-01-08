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
 * Demonstrates table ordering strategies for foreign key constraints.
 *
 * <p>This test shows:
 *
 * <ul>
 *   <li>Automatic alphabetical table ordering (default)
 *   <li>Manual ordering via {@code load-order.txt} file
 *   <li>Programmatic ordering via {@code load-order.txt} in custom directories
 *   <li>Handling foreign key constraints
 *   <li>Complex table dependencies (many-to-many relationships)
 * </ul>
 *
 * <p>Table ordering is critical when:
 *
 * <ul>
 *   <li>Tables have foreign key relationships
 *   <li>Parent tables must be loaded before child tables
 *   <li>Junction tables require both parent tables
 *   <li>Deletion order must be reverse of insertion order
 * </ul>
 *
 * <p>Schema:
 *
 * <pre>
 * TABLE1 (parent)
 *   ↓
 * TABLE2 (child of TABLE1)
 *   ↓
 * TABLE3 (independent)
 *   ↓
 * TABLE4 (junction: TABLE2 + TABLE3)
 * </pre>
 */
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("TableOrderingStrategiesTest")
final class TableOrderingStrategiesTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(TableOrderingStrategiesTest.class);

  /** DataSource for test database operations. */
  private static DataSource dataSource;

  /** Creates TableOrderingStrategiesTest instance. */
  TableOrderingStrategiesTest() {}

  /**
   * Sets up H2 in-memory database connection and schema.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up H2 in-memory database for TableOrderingStrategiesTest");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    dataSource = createDataSource();
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/feature/TableOrderingStrategiesTest.sql");

    logger.info("Database setup completed");
  }

  /**
   * Creates an H2 in-memory DataSource.
   *
   * @return configured DataSource
   */
  private static DataSource createDataSource() {
    final var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:TableOrderingStrategiesTest;DB_CLOSE_DELAY=-1");
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
                TableOrderingStrategiesTest.class.getClassLoader().getResource(scriptPath))
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
   * Demonstrates automatic alphabetical table ordering.
   *
   * <p>Framework orders tables alphabetically: TABLE1, TABLE2, TABLE3, TABLE4. This works well when
   * foreign keys follow alphabetical order.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,2), TABLE2(1,2,3), TABLE3(1,2,3), TABLE4(3 rows)
   *   <li>Execution: Inserts TABLE1(3,'Services'), TABLE2(4,3,'Consulting')
   *   <li>Expectation: Verifies TABLE1 has 3 rows, TABLE2 has 4 rows
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should use automatic alphabetical table ordering")
  @DataSet
  @ExpectedDataSet
  void shouldUseAlphabeticalOrdering() throws Exception {
    // Given
    logger.info("Running alphabetical ordering test");

    // When
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1) VALUES (3, 'Services')");
    executeSql("INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (4, 3, 'Consulting')");

    // Then
    logger.info("Alphabetical ordering test completed");
  }

  /**
   * Demonstrates manual table ordering via load-order.txt file.
   *
   * <p>Uses {@code load-order.txt} to specify correct insertion order for foreign keys.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2,3), TABLE4(2 rows)
   *   <li>Execution: Inserts TABLE3(4,'Featured'), TABLE4(1,4)
   *   <li>Expectation: Verifies TABLE3 has 4 rows, TABLE4 has 3 rows with new association
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should use manual table ordering via load-order.txt file")
  @DataSet
  @ExpectedDataSet
  void shouldUseManualOrdering() throws Exception {
    // Given
    logger.info("Running manual ordering test");

    // When
    executeSql("INSERT INTO TABLE3 (ID, COLUMN1) VALUES (4, 'Featured')");
    executeSql("INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (1, 4)");

    // Then
    logger.info("Manual ordering test completed");
  }

  /**
   * Demonstrates custom resource location for table ordering.
   *
   * <p>Uses {@code load-order.txt} in a custom directory to explicitly control table insertion
   * order.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE2(1,1,'Widget') from programmatic/ directory
   *   <li>Execution: Updates TABLE2 COLUMN2 from 'Widget' to 'Updated Widget' WHERE ID=1
   *   <li>Expectation: Verifies TABLE2(1,1,'Updated Widget')
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should use programmatic ordering via custom resource location")
  @DataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableOrderingStrategiesTest/programmatic/"))
  @ExpectedDataSet(
      dataSets =
          @DataSetSource(
              resourceLocation =
                  "classpath:example/feature/TableOrderingStrategiesTest/programmatic/expected/"))
  void shouldUseProgrammaticOrdering() throws Exception {
    // Given
    logger.info("Running programmatic ordering test");

    // When
    executeSql("UPDATE TABLE2 SET COLUMN2 = 'Updated Widget' WHERE ID = 1");

    // Then
    logger.info("Programmatic ordering test completed");
  }

  /**
   * Demonstrates handling complex many-to-many relationships.
   *
   * <p>Shows proper ordering for junction tables with multiple foreign keys.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: TABLE1(1,2), TABLE2(1,2), TABLE3(1,2), TABLE4(2 rows)
   *   <li>Execution: Adds TABLE1(4,'Accessories'), TABLE2(5,4,'Cable'), TABLE3(5,'Essential'),
   *       TABLE4(5,1), TABLE4(5,5)
   *   <li>Expectation: Verifies all 4 tables have new records with proper foreign key relationships
   * </ul>
   *
   * @throws Exception if database operation fails
   */
  @Test
  @Tag("normal")
  @DisplayName("should handle complex many-to-many relationships with proper ordering")
  @DataSet
  @ExpectedDataSet
  void shouldHandleManyToManyRelationships() throws Exception {
    // Given
    logger.info("Running many-to-many relationships test");

    // When
    executeSql("INSERT INTO TABLE1 (ID, COLUMN1) VALUES (4, 'Accessories')");
    executeSql("INSERT INTO TABLE2 (ID, COLUMN1, COLUMN2) VALUES (5, 4, 'Cable')");
    executeSql("INSERT INTO TABLE3 (ID, COLUMN1) VALUES (5, 'Essential')");
    executeSql("INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 1)");
    executeSql("INSERT INTO TABLE4 (COLUMN1, COLUMN2) VALUES (5, 5)");

    // Then
    logger.info("Many-to-many relationships test completed");
  }
}
