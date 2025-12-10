package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import javax.sql.DataSource;

/**
 * Service Provider Interface for executing database operations.
 *
 * <p>This SPI abstracts database operation execution, allowing test framework modules (JUnit,
 * Spock) to depend only on the API module. The actual implementation is provided by the core module
 * and loaded via {@link java.util.ServiceLoader}.
 *
 * <p>Supported operations:
 *
 * <ul>
 *   <li>{@code NONE} - No operation
 *   <li>{@code INSERT} - Insert rows
 *   <li>{@code UPDATE} - Update existing rows
 *   <li>{@code DELETE} - Delete specific rows
 *   <li>{@code DELETE_ALL} - Delete all rows from tables
 *   <li>{@code REFRESH} - Upsert (insert or update)
 *   <li>{@code TRUNCATE_TABLE} - Truncate tables
 *   <li>{@code CLEAN_INSERT} - Delete all then insert
 *   <li>{@code TRUNCATE_INSERT} - Truncate then insert
 * </ul>
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 * @see Operation
 */
public interface OperationProvider {

  /**
   * Executes a database operation on the given dataset.
   *
   * <p>The operation is performed within a transaction that is committed on success or rolled back
   * on failure.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param dataSource the data source for database connections
   * @param tableOrderingStrategy the strategy for determining table processing order
   * @throws DatabaseTesterException if the operation fails
   */
  void execute(
      Operation operation,
      DataSet dataSet,
      DataSource dataSource,
      TableOrderingStrategy tableOrderingStrategy);
}
