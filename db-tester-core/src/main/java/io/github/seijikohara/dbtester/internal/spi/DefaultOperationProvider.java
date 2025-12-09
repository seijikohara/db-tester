package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import io.github.seijikohara.dbtester.internal.jdbc.OperationExecutor;
import javax.sql.DataSource;

/**
 * Default implementation of {@link OperationProvider} that uses JDBC for database operations.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the implementation for
 * database operation execution.
 *
 * <p>The implementation delegates to {@link OperationExecutor} for the actual database operations.
 */
public final class DefaultOperationProvider implements OperationProvider {

  /** The operation executor for database operations. */
  private final OperationExecutor operationExecutor;

  /** Creates a new instance with default operation executor. */
  public DefaultOperationProvider() {
    this.operationExecutor = new OperationExecutor();
  }

  /**
   * Creates a new instance with specified operation executor.
   *
   * @param operationExecutor the operation executor to use
   */
  public DefaultOperationProvider(final OperationExecutor operationExecutor) {
    this.operationExecutor = operationExecutor;
  }

  @Override
  public void execute(
      final Operation operation, final DataSet dataSet, final DataSource dataSource) {
    operationExecutor.execute(operation, dataSet, dataSource);
  }
}
