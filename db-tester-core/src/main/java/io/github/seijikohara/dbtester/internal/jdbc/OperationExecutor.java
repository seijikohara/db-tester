package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes database operations for test data management.
 *
 * <p>This class coordinates the execution of database operations using pure JDBC. It delegates
 * specific operations to specialized executor classes:
 *
 * <ul>
 *   <li>{@link InsertExecutor} - INSERT operations
 *   <li>{@link UpdateExecutor} - UPDATE operations
 *   <li>{@link DeleteExecutor} - DELETE and DELETE_ALL operations
 *   <li>{@link TruncateExecutor} - TRUNCATE operations
 *   <li>{@link RefreshExecutor} - REFRESH (upsert) operations
 * </ul>
 *
 * <p>Supported operations include: NONE, INSERT, UPDATE, DELETE, DELETE_ALL, REFRESH,
 * TRUNCATE_TABLE, CLEAN_INSERT, and TRUNCATE_INSERT.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class OperationExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(OperationExecutor.class);

  /** The insert executor for INSERT operations. */
  private final InsertExecutor insertExecutor;

  /** The update executor for UPDATE operations. */
  private final UpdateExecutor updateExecutor;

  /** The delete executor for DELETE operations. */
  private final DeleteExecutor deleteExecutor;

  /** The truncate executor for TRUNCATE operations. */
  private final TruncateExecutor truncateExecutor;

  /** The refresh executor for REFRESH (upsert) operations. */
  private final RefreshExecutor refreshExecutor;

  /** Creates a new operation executor with default dependencies. */
  public OperationExecutor() {
    final var sqlBuilder = new SqlBuilder();
    final var parameterBinder = new ParameterBinder();
    this.insertExecutor = new InsertExecutor(sqlBuilder, parameterBinder);
    this.updateExecutor = new UpdateExecutor(sqlBuilder, parameterBinder);
    this.deleteExecutor = new DeleteExecutor(sqlBuilder, parameterBinder);
    this.truncateExecutor = new TruncateExecutor(sqlBuilder);
    this.refreshExecutor = new RefreshExecutor(insertExecutor, updateExecutor);
  }

  /**
   * Creates a new operation executor with the specified dependencies.
   *
   * @param insertExecutor the insert executor
   * @param updateExecutor the update executor
   * @param deleteExecutor the delete executor
   * @param truncateExecutor the truncate executor
   * @param refreshExecutor the refresh executor
   */
  public OperationExecutor(
      final InsertExecutor insertExecutor,
      final UpdateExecutor updateExecutor,
      final DeleteExecutor deleteExecutor,
      final TruncateExecutor truncateExecutor,
      final RefreshExecutor refreshExecutor) {
    this.insertExecutor = insertExecutor;
    this.updateExecutor = updateExecutor;
    this.deleteExecutor = deleteExecutor;
    this.truncateExecutor = truncateExecutor;
    this.refreshExecutor = refreshExecutor;
  }

  /**
   * Executes a database operation on the given dataset.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param dataSource the data source
   * @throws DatabaseTesterException if the operation fails
   */
  public void execute(
      final Operation operation, final DataSet dataSet, final DataSource dataSource) {
    logger.debug(
        "Executing operation {} on dataset with {} tables", operation, dataSet.getTables().size());

    try (final var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      try {
        executeOperation(operation, dataSet, connection);
        connection.commit();
        logger.debug("Successfully executed operation {}", operation);
      } catch (final SQLException e) {
        connection.rollback();
        throw e;
      }
    } catch (final SQLException e) {
      throw new DatabaseTesterException(
          String.format("Failed to execute operation %s", operation), e);
    }
  }

  /**
   * Executes the specified operation on the dataset using the provided connection.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  void executeOperation(
      final Operation operation, final DataSet dataSet, final Connection connection)
      throws SQLException {
    switch (operation) {
      case NONE -> {
        // Do nothing
      }
      case INSERT -> insertExecutor.execute(dataSet.getTables(), connection);
      case UPDATE -> updateExecutor.execute(dataSet.getTables(), connection);
      case DELETE -> deleteExecutor.execute(dataSet.getTables(), connection);
      case DELETE_ALL -> deleteExecutor.executeDeleteAll(dataSet.getTables(), connection);
      case REFRESH -> refreshExecutor.execute(dataSet.getTables(), connection);
      case TRUNCATE_TABLE -> truncateExecutor.execute(dataSet.getTables(), connection);
      case CLEAN_INSERT -> {
        deleteExecutor.executeDeleteAll(dataSet.getTables().reversed(), connection);
        insertExecutor.execute(dataSet.getTables(), connection);
      }
      case TRUNCATE_INSERT -> {
        truncateExecutor.execute(dataSet.getTables().reversed(), connection);
        insertExecutor.execute(dataSet.getTables(), connection);
      }
    }
  }
}
