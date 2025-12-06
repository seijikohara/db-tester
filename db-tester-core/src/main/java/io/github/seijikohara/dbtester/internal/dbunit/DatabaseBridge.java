package io.github.seijikohara.dbtester.internal.dbunit;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSet;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.dbunit.assertion.DatabaseAssert;
import io.github.seijikohara.dbtester.internal.dbunit.assertion.FailureHandlerAdapter;
import io.github.seijikohara.dbtester.internal.dbunit.format.CsvDataSetReader;
import io.github.seijikohara.dbtester.internal.dbunit.format.DataSetReader;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.SchemaName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.operation.DatabaseOperation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal facade that adapts framework-level datasets and operations to DbUnit primitives.
 *
 * <p>The bridge delegates execution, loading, and assertion tasks to DbUnit while shielding the
 * rest of the framework from DbUnit types.
 */
public final class DatabaseBridge {

  /** Logger for tracking operations. */
  private static final Logger logger = LoggerFactory.getLogger(DatabaseBridge.class);

  /** Singleton instance. */
  private static final DatabaseBridge INSTANCE = new DatabaseBridge();

  /** CSV dataset reader. */
  private final DataSetReader csvReader;

  /** DbUnit assertion implementation. */
  private final DatabaseAssert databaseAssert;

  /** Private constructor for singleton pattern. */
  private DatabaseBridge() {
    this.csvReader = new CsvDataSetReader();
    this.databaseAssert = new DatabaseAssert();
  }

  /**
   * Gets the singleton instance of DatabaseBridge.
   *
   * @return the singleton instance
   */
  public static DatabaseBridge getInstance() {
    return INSTANCE;
  }

  // ===========================================================================================
  // Database Operations
  // ===========================================================================================

  /**
   * Executes a database operation using the specified dataset.
   *
   * <p>This method performs the complete operation execution workflow:
   *
   * <ol>
   *   <li>Converts framework dataset to DbUnit format
   *   <li>Converts framework operation to DbUnit format
   *   <li>Creates database connection from data source
   *   <li>Executes operation via DbUnit
   *   <li>Closes connection (automatic via try-with-resources)
   * </ol>
   *
   * @param scenarioDataSet the dataset to use for the operation
   * @param operation the operation to execute
   * @param dataSource the target data source
   * @param schemaName the database schema, or null for default
   * @throws DatabaseTesterException if the operation fails
   */
  public void executeOperation(
      final ScenarioDataSet scenarioDataSet,
      final Operation operation,
      final DataSource dataSource,
      final @Nullable SchemaName schemaName) {

    logger.debug(
        "Executing operation {} on {} tables with schema: {}",
        operation,
        scenarioDataSet.getTables().size(),
        formatSchemaName(schemaName));

    try (final var jdbcConnection = dataSource.getConnection()) {
      final var dbUnitDataSet = TypeConverter.toDbUnitDataSet(scenarioDataSet);
      final var dbUnitOperation = toDbUnitOperation(operation);
      final var resolvedSchemaName = resolveSchemaName(schemaName, jdbcConnection);

      withDbUnitConnection(
          jdbcConnection,
          resolvedSchemaName.orElse(null),
          connection -> dbUnitOperation.execute(connection, dbUnitDataSet));

      logger.debug(
          "Successfully executed operation {} on {} tables",
          operation,
          scenarioDataSet.getTables().size());
    } catch (final DatabaseUnitException | SQLException e) {
      throw new DatabaseTesterException(
          String.format(
              "Failed to execute operation: %s on schema: %s",
              operation, formatSchemaName(schemaName)),
          e);
    }
  }

  /**
   * Executes a database operation using the connection's default schema.
   *
   * <p>Convenience method equivalent to {@code executeOperation(scenarioDataSet, operation,
   * dataSource, null)}.
   *
   * @param scenarioDataSet the dataset to use for the operation
   * @param operation the operation to execute
   * @param dataSource the target data source
   * @throws DatabaseTesterException if the operation fails
   */
  public void executeOperation(
      final ScenarioDataSet scenarioDataSet,
      final Operation operation,
      final DataSource dataSource) {
    executeOperation(scenarioDataSet, operation, dataSource, null);
  }

  // ===========================================================================================
  // Dataset Loading
  // ===========================================================================================

  /**
   * Loads CSV dataset from the specified directory.
   *
   * <p>The directory should contain one CSV file per table, where each filename (without extension)
   * becomes the table name. The first row of each CSV file must contain column headers.
   *
   * <p><strong>Directory Structure:</strong>
   *
   * <pre>
   * test-data/
   *   ├── TABLE1.csv
   *   ├── TABLE2.csv
   *   └── TABLE3.csv
   * </pre>
   *
   * @param directory the directory containing CSV files
   * @return the loaded dataset
   * @throws io.github.seijikohara.dbtester.api.exception.DataSetLoadException if loading fails
   */
  public DataSet loadCsvDataSet(final Path directory) {
    return csvReader.read(directory);
  }

  // ===========================================================================================
  // Assertions - DataSet Level
  // ===========================================================================================

  /**
   * Asserts equality between expected and actual datasets.
   *
   * <p>Compares all tables, rows, and columns. Fails with detailed error message showing the first
   * difference found.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset to compare against
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the datasets do not match
   */
  public void assertEquals(final DataSet expected, final DataSet actual) {
    databaseAssert.assertEquals(
        TypeConverter.toDbUnitDataSet(expected), TypeConverter.toDbUnitDataSet(actual));
  }

  /**
   * Asserts equality between expected and actual datasets with custom failure handling.
   *
   * <p>Allows customization of assertion failure reporting through a user-provided handler.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset to compare against
   * @param failureHandler custom failure handler, or {@code null} for default handler
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the datasets do not match
   */
  public void assertEquals(
      final DataSet expected,
      final DataSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    databaseAssert.assertEquals(
        TypeConverter.toDbUnitDataSet(expected),
        TypeConverter.toDbUnitDataSet(actual),
        Optional.ofNullable(failureHandler)
            .map(FailureHandlerAdapter::toDbUnitHandler)
            .orElse(null));
  }

  /**
   * Asserts equality for specific table in datasets, excluding specified columns.
   *
   * <p>Useful for ignoring auto-generated columns (timestamps, IDs) during comparison.
   *
   * @param expected the expected dataset containing table data
   * @param actual the actual dataset to compare against
   * @param tableName the name of the table to compare
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the table data does not match
   */
  public void assertEqualsIgnoreColumns(
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    databaseAssert.assertEqualsIgnoreColumns(
        TypeConverter.toDbUnitDataSet(expected),
        TypeConverter.toDbUnitDataSet(actual),
        new TableName(tableName),
        ignoreColumnNames.stream().map(ColumnName::new).toList());
  }

  // ===========================================================================================
  // Assertions - Table Level
  // ===========================================================================================

  /**
   * Asserts equality between expected and actual tables.
   *
   * <p>Compares all rows and columns. Fails with detailed error message showing the first
   * difference found.
   *
   * @param expected the expected table data
   * @param actual the actual table data to compare against
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the tables do not match
   */
  public void assertEquals(final Table expected, final Table actual) {
    databaseAssert.assertEquals(
        TypeConverter.toDbUnitTable(expected), TypeConverter.toDbUnitTable(actual));
  }

  /**
   * Asserts equality between expected and actual tables with additional column names.
   *
   * <p>Column names supplement table definitions. DbUnit infers data types from actual table data.
   *
   * @param expected the expected table data
   * @param actual the actual table data to compare against
   * @param additionalColumnNames additional column names to include in comparison
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the tables do not match
   */
  public void assertEquals(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    databaseAssert.assertEquals(
        TypeConverter.toDbUnitTable(expected),
        TypeConverter.toDbUnitTable(actual),
        List.of(TypeConverter.toDbUnitColumns(additionalColumnNames)));
  }

  /**
   * Asserts equality between expected and actual tables with custom failure handling.
   *
   * <p>Allows customization of assertion failure reporting through a user-provided handler.
   *
   * @param expected the expected table data
   * @param actual the actual table data to compare against
   * @param failureHandler custom failure handler, or {@code null} for default handler
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the tables do not match
   */
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    databaseAssert.assertEquals(
        TypeConverter.toDbUnitTable(expected),
        TypeConverter.toDbUnitTable(actual),
        Optional.ofNullable(failureHandler)
            .map(FailureHandlerAdapter::toDbUnitHandler)
            .orElse(null));
  }

  /**
   * Asserts equality between expected and actual tables, excluding specified columns.
   *
   * <p>Useful for ignoring auto-generated columns (timestamps, IDs) during comparison.
   *
   * @param expected the expected table data
   * @param actual the actual table data to compare against
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if type conversion fails
   * @throws AssertionError if the tables do not match
   */
  public void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    databaseAssert.assertEqualsIgnoreColumns(
        TypeConverter.toDbUnitTable(expected),
        TypeConverter.toDbUnitTable(actual),
        ignoreColumnNames.stream().map(ColumnName::new).toList());
  }

  // ===========================================================================================
  // Assertions - Query Based
  // ===========================================================================================

  /**
   * Asserts equality by comparing expected dataset against SQL query results.
   *
   * <p>Retrieves actual data by executing the provided SQL query and compares it against the
   * expected dataset for the specified table.
   *
   * @param expected the expected dataset containing table data
   * @param dataSource database connection source for executing the query
   * @param sqlQuery SQL query to retrieve actual data
   * @param tableName the name of the table in the expected dataset to compare
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if type conversion or connection creation fails
   * @throws AssertionError if the query results do not match the expected data
   */
  public void assertEqualsByQuery(
      final DataSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var expectedDataSet = TypeConverter.toDbUnitDataSet(expected);
    try (final var jdbcConnection = dataSource.getConnection()) {
      withDbUnitConnection(
          jdbcConnection,
          null,
          connection ->
              databaseAssert.assertEqualsByQuery(
                  expectedDataSet,
                  connection,
                  sqlQuery,
                  new TableName(tableName),
                  ignoreColumnNames.stream().map(ColumnName::new).toList()));
    } catch (final DatabaseUnitException | SQLException e) {
      throw new ValidationException(
          String.format("Failed to execute query comparison for table '%s'", tableName), e);
    }
  }

  /**
   * Asserts equality by comparing expected table against SQL query results.
   *
   * <p>Retrieves actual data by executing the provided SQL query and compares it against the
   * expected table.
   *
   * @param expected the expected table data
   * @param dataSource database connection source for executing the query
   * @param tableName the name to assign to the query results
   * @param sqlQuery SQL query to retrieve actual data
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if type conversion or connection creation fails
   * @throws AssertionError if the query results do not match the expected table
   */
  public void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    final var expectedTable = TypeConverter.toDbUnitTable(expected);
    try (final var jdbcConnection = dataSource.getConnection()) {
      withDbUnitConnection(
          jdbcConnection,
          null,
          connection ->
              databaseAssert.assertEqualsByQuery(
                  expectedTable,
                  connection,
                  new TableName(tableName),
                  sqlQuery,
                  ignoreColumnNames.stream().map(ColumnName::new).toList()));
    } catch (final DatabaseUnitException | SQLException e) {
      throw new ValidationException(
          String.format("Failed to execute query comparison for table '%s'", tableName), e);
    }
  }

  // ===========================================================================================
  // Verification
  // ===========================================================================================

  /**
   * Verifies database state matches expected dataset.
   *
   * <p>For each table in the expected dataset:
   *
   * <ol>
   *   <li>Retrieves actual data from database
   *   <li>Filters actual data to only include columns present in expected table
   *   <li>Compares filtered actual data against expected data
   * </ol>
   *
   * <p>Only columns present in expected dataset are compared, allowing partial column validation.
   *
   * @param expectedDataSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @throws ValidationException if verification fails
   */
  public void verifyExpectation(final DataSet expectedDataSet, final DataSource dataSource) {
    verifyExpectationWithConnection(expectedDataSet, dataSource, null);
  }

  /**
   * Verifies expectation dataset using established database connection.
   *
   * @param expectedDataSet the expectation dataset containing expected table data
   * @param dataSource the database connection source
   * @param schemaName the database schema name
   * @throws ValidationException if verification fails
   */
  private void verifyExpectationWithConnection(
      final DataSet expectedDataSet,
      final DataSource dataSource,
      final @Nullable SchemaName schemaName) {
    final var dbUnitExpectedDataSet = TypeConverter.toDbUnitDataSet(expectedDataSet);

    try (final var jdbcConnection = dataSource.getConnection()) {
      final var resolvedSchemaName = resolveSchemaName(schemaName, jdbcConnection);
      final var schemaForErrors = formatSchemaName(resolvedSchemaName.orElse(null));

      withDbUnitConnection(
          jdbcConnection,
          resolvedSchemaName.orElse(null),
          connection -> {
            expectedDataSet
                .getTables()
                .forEach(
                    expectedTable -> {
                      try {
                        verifyTable(expectedTable, dbUnitExpectedDataSet, connection);
                      } catch (final DataSetException | SQLException e) {
                        throw new ValidationException(
                            String.format(
                                "Failed to verify table: %s (schema: %s)",
                                expectedTable.getName().value(), schemaForErrors),
                            e);
                      }
                    });
          });
    } catch (final DatabaseUnitException | SQLException e) {
      throw new ValidationException(
          String.format(
              "Failed to verify expectation dataset for schema: %s", formatSchemaName(schemaName)),
          e);
    }
  }

  /**
   * Resolves the schema name to use for database interactions.
   *
   * @param requestedSchemaName schema name explicitly provided by the caller, may be null
   * @param jdbcConnection the active JDBC connection
   * @return resolved schema name, or empty if the default schema should be used
   * @throws SQLException if schema resolution fails
   */
  private Optional<SchemaName> resolveSchemaName(
      final @Nullable SchemaName requestedSchemaName, final Connection jdbcConnection)
      throws SQLException {
    return Optional.ofNullable(requestedSchemaName)
        .or(
            () -> {
              try {
                return Optional.ofNullable(jdbcConnection.getSchema()).map(SchemaName::new);
              } catch (final SQLException e) {
                throw new DatabaseTesterException("Failed to resolve database schema", e);
              }
            });
  }

  /**
   * Formats a schema name for display in logging and error messages.
   *
   * @param schemaName schema name, may be {@code null}
   * @return schema value or {@code "default"} when absent
   */
  private static String formatSchemaName(final @Nullable SchemaName schemaName) {
    return Optional.ofNullable(schemaName).map(SchemaName::value).orElse("default");
  }

  /**
   * Creates a DbUnit connection, executes the provided action, and closes the connection safely.
   *
   * @param jdbcConnection active JDBC connection
   * @param schemaName schema to use when creating the DbUnit connection (nullable)
   * @param consumer action that interacts with the DbUnit connection
   * @throws DatabaseUnitException if the DbUnit connection or action fails
   * @throws SQLException if JDBC access fails
   */
  private void withDbUnitConnection(
      final Connection jdbcConnection,
      final @Nullable SchemaName schemaName,
      final ConnectionConsumer consumer)
      throws DatabaseUnitException, SQLException {
    final var connection = TypeConverter.toDbUnitConnection(jdbcConnection, schemaName);
    try {
      consumer.accept(connection);
    } finally {
      closeQuietly(connection);
    }
  }

  /**
   * Closes the provided DbUnit connection without propagating exceptions.
   *
   * @param connection the connection to close
   */
  private void closeQuietly(final IDatabaseConnection connection) {
    try {
      connection.close();
    } catch (final SQLException e) {
      logger.warn("Failed to close IDatabaseConnection cleanly", e);
    }
  }

  /**
   * Verifies single table against actual database state.
   *
   * @param expectedTable the expected table data
   * @param dbUnitExpectedDataSet the DbUnit expected dataset
   * @param connection the DbUnit database connection
   * @throws DataSetException if table verification fails
   * @throws SQLException if database access fails
   */
  private void verifyTable(
      final Table expectedTable,
      final IDataSet dbUnitExpectedDataSet,
      final IDatabaseConnection connection)
      throws DataSetException, SQLException {
    final var tableName = expectedTable.getName();

    // Get expected table from DbUnit dataset
    final var dbUnitExpectedTable = dbUnitExpectedDataSet.getTable(tableName.value());

    // Retrieve actual table data from database
    final var actualCompleteTable = connection.createDataSet().getTable(tableName.value());

    // Extract column names from expected table
    final var expectedColumnNames =
        expectedTable.getColumns().stream().map(ColumnName::value).toList();

    // Filter actual table to only include columns present in expected table
    // Note: DbUnit API requires String[] array parameter (cannot use List)
    final var actualFilteredTable =
        DefaultColumnFilter.includedColumnsTable(
            actualCompleteTable, expectedColumnNames.toArray(String[]::new));

    // Perform assertion comparison
    databaseAssert.assertEquals(dbUnitExpectedTable, actualFilteredTable);
  }

  // ===========================================================================================
  // Operation Mapping
  // ===========================================================================================

  /**
   * Converts framework Operation to DbUnit DatabaseOperation.
   *
   * @param operation the framework operation
   * @return the corresponding DbUnit operation
   */
  private static DatabaseOperation toDbUnitOperation(final Operation operation) {
    return OperationConverter.toDbUnitOperation(operation);
  }
}

/** Functional callback used to execute logic with an {@link IDatabaseConnection}. */
@FunctionalInterface
interface ConnectionConsumer {

  /**
   * Executes logic using the provided DbUnit connection.
   *
   * @param connection active DbUnit connection
   * @throws DatabaseUnitException when DbUnit reports a failure
   * @throws SQLException when JDBC access fails
   */
  void accept(final IDatabaseConnection connection) throws DatabaseUnitException, SQLException;
}
