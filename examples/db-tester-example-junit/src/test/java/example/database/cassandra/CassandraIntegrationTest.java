package example.database.cassandra;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.ing.data.cassandra.jdbc.CassandraDataSource;
import com.ing.data.cassandra.jdbc.utils.ContactPoint;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Apache Cassandra integration test using Testcontainers.
 *
 * <p>This test validates that the DB Tester framework works with Apache Cassandra, demonstrating
 * framework versatility with wide-column NoSQL databases that provide JDBC wrappers. Cassandra uses
 * CQL (Cassandra Query Language), which has SQL-like syntax compatible with JDBC operations.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li>Uses ING Bank Cassandra JDBC Wrapper (com.ing.data:cassandra-jdbc-wrapper)
 *   <li>Queries use CQL (Cassandra Query Language) with SQL-like syntax
 *   <li>Results are returned as JDBC ResultSets in tabular format
 *   <li>Tables (column families) work similarly to RDBMS tables
 * </ul>
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
public final class CassandraIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(CassandraIntegrationTest.class);

  /** Keyspace name for test data. */
  private static final String KEYSPACE = "test_keyspace";

  /** Creates Cassandra integration test instance. */
  public CassandraIntegrationTest() {}

  /** Cassandra container for integration testing. */
  @Container static final CassandraContainer cassandra = new CassandraContainer("cassandra:4.1");

  /**
   * Sets up Cassandra database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up Cassandra Testcontainer");

    // First create keyspace using direct connection
    createKeyspace(cassandra);

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(cassandra);
    testRegistry.registerDefault(dataSource);
    executeScript(cassandra, "ddl/database/cassandra/cassandra-integration.cql");

    logger.info("Cassandra database setup completed");
  }

  /**
   * Creates the keyspace for testing.
   *
   * @param container the Cassandra container
   * @throws SQLException if keyspace creation fails
   */
  private static void createKeyspace(final CassandraContainer container) throws SQLException {
    final var jdbcUrl =
        String.format(
            "jdbc:cassandra://%s:%d/system_schema",
            container.getHost(), container.getMappedPort(9042));

    try (final var connection = DriverManager.getConnection(jdbcUrl);
        final var statement = connection.createStatement()) {
      statement.execute(
          String.format(
              "CREATE KEYSPACE IF NOT EXISTS %s "
                  + "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}",
              KEYSPACE));
    }
  }

  /**
   * Creates a Cassandra DataSource from the Testcontainer.
   *
   * @param container the Cassandra container
   * @return configured DataSource
   */
  private static DataSource createDataSource(final CassandraContainer container) {
    final var contactPoints =
        List.of(ContactPoint.of(container.getHost(), container.getMappedPort(9042)));
    final var dataSource = new CassandraDataSource(contactPoints, KEYSPACE);
    dataSource.setLocalDataCenter("datacenter1");
    return dataSource;
  }

  /**
   * Executes a CQL script from classpath.
   *
   * @param container the Cassandra container for connection info
   * @param scriptPath the classpath resource path
   * @throws Exception if script execution fails
   */
  private static void executeScript(final CassandraContainer container, final String scriptPath)
      throws Exception {
    final var resource =
        Optional.ofNullable(CassandraIntegrationTest.class.getClassLoader().getResource(scriptPath))
            .orElseThrow(
                () -> new IllegalStateException(String.format("Script not found: %s", scriptPath)));

    final var jdbcUrl =
        String.format(
            "jdbc:cassandra://%s:%d/%s",
            container.getHost(), container.getMappedPort(9042), KEYSPACE);

    try (final var connection = DriverManager.getConnection(jdbcUrl);
        final var statement = connection.createStatement();
        final var inputStream = resource.openStream()) {
      final var cql = new String(inputStream.readAllBytes(), UTF_8);
      Arrays.stream(cql.split(";"))
          .map(String::trim)
          .filter(Predicate.not(String::isEmpty))
          .filter(line -> !line.startsWith("--"))
          .forEach(
              trimmed -> {
                try {
                  statement.execute(trimmed);
                } catch (final SQLException e) {
                  throw new RuntimeException(
                      String.format("Failed to execute CQL: %s", trimmed), e);
                }
              });
    }
  }

  /**
   * Smoke test verifying basic framework functionality with Cassandra.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into Cassandra tables
   *   <li>Data can be verified against expected CSV via CQL queries
   *   <li>Framework handles wide-column database results correctly
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  @Expectation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnCassandra() {
    logger.info("Running Cassandra integration smoke test");
  }
}
