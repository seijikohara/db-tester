package io.github.seijikohara.dbtester.api.preparation;

import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import java.util.ServiceLoader;
import javax.sql.DataSource;

/**
 * Static facade for programmatic database preparation.
 *
 * <p>While the extension primarily relies on annotation-driven preparation via {@link
 * io.github.seijikohara.dbtester.api.annotation.DataSet}, there are situations where tests need
 * direct control over data setup. {@code DatabasePreparation} supports those scenarios by exposing
 * preparation helpers that operate on framework abstractions ({@link TableSet}, {@link DataSource})
 * and internally delegate to the {@link OperationProvider} implementation loaded via {@link
 * ServiceLoader}.
 *
 * <p>Typical use cases include:
 *
 * <ul>
 *   <li>Programmatic data generation (loops, computed values)
 *   <li>Dynamic test data that cannot be expressed in static files
 *   <li>Mid-test data manipulation
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var users = Table.ofValues("USERS",
 *     List.of("ID", "NAME"),
 *     List.of(List.of(1, "Alice"), List.of(2, "Bob")));
 *
 * // Clean insert with default configuration
 * DatabasePreparation.cleanInsert(dataSource, TableSet.of(users));
 *
 * // Execute a specific operation
 * DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.INSERT);
 *
 * // Execute with custom configuration
 * var config = PreparationConfig.standard()
 *     .withTransactionMode(TransactionMode.AUTO_COMMIT)
 *     .withBatchSize(1000);
 * DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.CLEAN_INSERT, config);
 * }</pre>
 *
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
 * @see OperationProvider
 */
public final class DatabasePreparation {

  /** Lazy holder for the OperationProvider instance loaded via SPI. */
  private static final class ProviderHolder {
    /** The singleton OperationProvider instance. */
    private static final OperationProvider INSTANCE = loadProvider();

    /** Private constructor to prevent instantiation. */
    private ProviderHolder() {}

    /**
     * Loads the OperationProvider implementation via ServiceLoader.
     *
     * @return the OperationProvider instance
     */
    private static OperationProvider loadProvider() {
      return ServiceLoader.load(OperationProvider.class)
          .findFirst()
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "No OperationProvider implementation found. "
                          + "Add db-tester-core to your classpath."));
    }
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws UnsupportedOperationException always, as this class is not instantiable
   */
  private DatabasePreparation() {
    throw new UnsupportedOperationException("This class has only static methods");
  }

  /**
   * Returns the provider for delegating operations to the underlying database implementation.
   *
   * <p>This method retrieves the singleton OperationProvider instance loaded via SPI. The provider
   * is lazily initialized on first access through the ProviderHolder inner class.
   *
   * @return the operation provider instance loaded via ServiceLoader
   */
  private static OperationProvider getProvider() {
    return ProviderHolder.INSTANCE;
  }

  /**
   * Executes a {@link Operation#CLEAN_INSERT} operation with standard configuration.
   *
   * <p>This convenience method deletes all existing rows from the tables referenced in the dataset
   * and then inserts the provided data. Uses {@link PreparationConfig#standard()} defaults.
   *
   * @param dataSource the data source for database connections
   * @param tableSet the dataset to insert
   */
  public static void cleanInsert(final DataSource dataSource, final TableSet tableSet) {
    execute(dataSource, tableSet, Operation.CLEAN_INSERT);
  }

  /**
   * Executes a {@link Operation#CLEAN_INSERT} operation with custom configuration.
   *
   * <p>This convenience method deletes all existing rows from the tables referenced in the dataset
   * and then inserts the provided data, using the specified configuration for transaction handling,
   * table ordering, and batch sizing.
   *
   * @param dataSource the data source for database connections
   * @param tableSet the dataset to insert
   * @param config the preparation configuration
   */
  public static void cleanInsert(
      final DataSource dataSource, final TableSet tableSet, final PreparationConfig config) {
    execute(dataSource, tableSet, Operation.CLEAN_INSERT, config);
  }

  /**
   * Executes the specified database operation with standard configuration.
   *
   * <p>Uses {@link PreparationConfig#standard()} defaults for transaction handling, table ordering,
   * and batch sizing.
   *
   * @param dataSource the data source for database connections
   * @param tableSet the dataset to operate on
   * @param operation the operation to execute
   */
  public static void execute(
      final DataSource dataSource, final TableSet tableSet, final Operation operation) {
    execute(dataSource, tableSet, operation, PreparationConfig.standard());
  }

  /**
   * Executes the specified database operation with custom configuration.
   *
   * <p>This method delegates to the {@link OperationProvider} SPI implementation, passing the
   * configuration settings for transaction handling, table ordering, query timeout, and batch
   * sizing.
   *
   * @param dataSource the data source for database connections
   * @param tableSet the dataset to operate on
   * @param operation the operation to execute
   * @param config the preparation configuration
   */
  public static void execute(
      final DataSource dataSource,
      final TableSet tableSet,
      final Operation operation,
      final PreparationConfig config) {
    getProvider()
        .execute(
            operation,
            tableSet,
            dataSource,
            config.tableOrderingStrategy(),
            config.transactionMode(),
            config.queryTimeout(),
            config.batchSize());
  }
}
