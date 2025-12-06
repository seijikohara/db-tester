package example.database.mssql;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

/**
 * Microsoft SQL Server integration test using Testcontainers.
 *
 * <p>This test validates that the framework works correctly with SQL Server using Testcontainers.
 * This is a smoke test to ensure SQL Server compatibility.
 */
@Testcontainers
@ExtendWith(DatabaseTestExtension.class)
public final class MSSQLServerIntegrationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(MSSQLServerIntegrationTest.class);

  /** Creates SQL Server integration test instance. */
  public MSSQLServerIntegrationTest() {}

  /** SQL Server container for integration testing. */
  @Container
  static final MSSQLServerContainer mssql =
      new MSSQLServerContainer("mcr.microsoft.com/mssql/server:latest")
          .acceptLicense()
          .withPassword("StrongPassword123!");

  /**
   * Sets up SQL Server database connection and schema using Testcontainers.
   *
   * @param context the extension context
   * @throws Exception if database setup fails
   */
  @BeforeAll
  static void setupDatabase(final ExtensionContext context) throws Exception {
    logger.info("Setting up SQL Server Testcontainer");

    final var testRegistry = DatabaseTestExtension.getRegistry(context);
    final var dataSource = createDataSource(mssql);
    testRegistry.registerDefault(dataSource);
    executeScript(dataSource, "ddl/database/mssql/mssql-integration.sql");

    logger.info("SQL Server database setup completed");
  }

  /**
   * Creates a SQL Server DataSource from the Testcontainer.
   *
   * @param container the SQL Server container
   * @return configured DataSource
   */
  private static DataSource createDataSource(final MSSQLServerContainer container) {
    final var dataSource = new SQLServerDataSource();
    dataSource.setURL(container.getJdbcUrl());
    dataSource.setUser(container.getUsername());
    dataSource.setPassword(container.getPassword());
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
                MSSQLServerIntegrationTest.class.getClassLoader().getResource(scriptPath))
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
   * Smoke test verifying basic framework functionality with SQL Server.
   *
   * <p>This test validates:
   *
   * <ul>
   *   <li>Data can be loaded from CSV into SQL Server
   *   <li>Data can be verified against expected CSV
   *   <li>Basic CRUD operations work correctly
   * </ul>
   */
  @Test
  @Preparation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  @Expectation(dataSets = @DataSet(scenarioNames = "smokeTest"))
  void shouldExecuteBasicDatabaseOperationsOnSQLServer() {
    logger.info("Running SQL Server integration smoke test");
  }
}
