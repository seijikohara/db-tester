package example.database.neo4j;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.neo4j.jdbc.Neo4jDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.neo4j.Neo4jContainer;

/**
 * Neo4j Graph Database integration test using Testcontainers.
 *
 * <p>This test validates that the DB Tester framework works with Neo4j, demonstrating framework
 * versatility with non-RDBMS databases that provide JDBC drivers. Neo4j is a graph database that
 * stores data as nodes and relationships, but its JDBC driver returns query results in tabular
 * format compatible with standard JDBC operations.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>Uses official Neo4j JDBC Driver with SQL translation (org.neo4j:neo4j-jdbc-full-bundle)
 *   <li>Queries use Cypher language instead of SQL
 *   <li>Results are returned as JDBC ResultSets in tabular format
 *   <li>Nodes with properties map to table-like structures for testing
 * </ul>
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
@DisplayName("Neo4jIntegrationTest")
final class Neo4jIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(Neo4jIntegrationTest.class);

  /** Creates Neo4j integration test instance. */
  Neo4jIntegrationTest() {}

  /** Neo4j container for integration testing. */
  @Container
  static final Neo4jContainer neo4j =
      new Neo4jContainer("neo4j:5-community").withoutAuthentication();

  /**
   * Sets up Neo4j database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up Neo4j Testcontainer");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(neo4j);
    testRegistry.registerDefault(dataSource);
    executeScript(neo4j, "ddl/database/neo4j/neo4j-integration.cypher");

    logger.info("Neo4j database setup completed");
  }

  /**
   * Creates a Neo4j DataSource from the Testcontainer.
   *
   * @param container the Neo4j container
   * @return configured DataSource
   */
  private static DataSource createDataSource(final Neo4jContainer container) {
    final var dataSource = new Neo4jDataSource();
    // Neo4j JDBC driver 6.x URL format: jdbc:neo4j://<host>:<port>
    // enableSQLTranslation: Automatically translate SQL to Cypher
    final var jdbcUrl =
        container.getBoltUrl().replace("bolt://", "jdbc:neo4j://") + "?enableSQLTranslation=true";
    dataSource.setUrl(jdbcUrl);
    return dataSource;
  }

  /**
   * Executes a Cypher script from classpath.
   *
   * @param container the Neo4j container for direct connection
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final Neo4jContainer container, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(Neo4jIntegrationTest.class.getClassLoader().getResource(scriptPath))
            .orElseThrow(
                () -> new IllegalStateException(String.format("Script not found: %s", scriptPath)));

    // Use Cypher natively without SQL translation for DDL script
    final var jdbcUrl = container.getBoltUrl().replace("bolt://", "jdbc:neo4j://");
    try (final var connection = DriverManager.getConnection(jdbcUrl);
        final var statement = connection.createStatement();
        final var inputStream = resource.openStream()) {
      final var cypher = new String(inputStream.readAllBytes(), UTF_8);
      Arrays.stream(cypher.split(";"))
          .map(String::trim)
          .filter(Predicate.not(String::isEmpty))
          .filter(line -> !line.startsWith("//"))
          .forEach(
              trimmed -> {
                try {
                  statement.execute(trimmed);
                } catch (final Exception e) {
                  throw new RuntimeException(
                      String.format("Failed to execute Cypher: %s", trimmed), e);
                }
              });
    }
  }

  /**
   * Smoke test verifying basic framework functionality with Neo4j graph database.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into Neo4j nodes
   *   <li>Data can be verified against expected CSV via Cypher queries
   *   <li>Framework handles graph database results correctly
   * </ul>
   */
  @Test
  @Tag("normal")
  @DisplayName("should execute basic database operations on Neo4j")
  @DataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  @ExpectedDataSet(dataSets = @DataSetSource(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnNeo4j() {
    // When & Then
    logger.info("Running Neo4j integration smoke test");
  }
}
