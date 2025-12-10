package io.github.seijikohara.dbtester.internal.jdbc;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.open;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.run;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.internal.jdbc.executor.DeleteExecutor;
import io.github.seijikohara.dbtester.internal.jdbc.executor.InsertExecutor;
import io.github.seijikohara.dbtester.internal.jdbc.executor.RefreshExecutor;
import io.github.seijikohara.dbtester.internal.jdbc.executor.TruncateExecutor;
import io.github.seijikohara.dbtester.internal.jdbc.executor.UpdateExecutor;
import java.sql.Connection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
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

  /** The foreign key resolver for table ordering. */
  private final ForeignKeyResolver foreignKeyResolver;

  /** Creates a new operation executor with default dependencies. */
  public OperationExecutor() {
    final var sqlBuilder = new SqlBuilder();
    final var parameterBinder = new ParameterBinder();
    this.insertExecutor = new InsertExecutor(sqlBuilder, parameterBinder);
    this.updateExecutor = new UpdateExecutor(sqlBuilder, parameterBinder);
    this.deleteExecutor = new DeleteExecutor(sqlBuilder, parameterBinder);
    this.truncateExecutor = new TruncateExecutor(sqlBuilder);
    this.refreshExecutor = new RefreshExecutor(insertExecutor, updateExecutor);
    this.foreignKeyResolver = new ForeignKeyResolver();
  }

  /**
   * Creates a new operation executor with the specified dependencies.
   *
   * <p>This constructor is package-private for testing purposes.
   *
   * @param insertExecutor the insert executor
   * @param updateExecutor the update executor
   * @param deleteExecutor the delete executor
   * @param truncateExecutor the truncate executor
   * @param refreshExecutor the refresh executor
   * @param foreignKeyResolver the foreign key resolver
   */
  OperationExecutor(
      final InsertExecutor insertExecutor,
      final UpdateExecutor updateExecutor,
      final DeleteExecutor deleteExecutor,
      final TruncateExecutor truncateExecutor,
      final RefreshExecutor refreshExecutor,
      final ForeignKeyResolver foreignKeyResolver) {
    this.insertExecutor = insertExecutor;
    this.updateExecutor = updateExecutor;
    this.deleteExecutor = deleteExecutor;
    this.truncateExecutor = truncateExecutor;
    this.refreshExecutor = refreshExecutor;
    this.foreignKeyResolver = foreignKeyResolver;
  }

  /**
   * Executes a database operation on the given dataset.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param dataSource the data source
   * @param tableOrderingStrategy the strategy for determining table processing order
   * @throws DatabaseTesterException if the operation fails
   */
  public void execute(
      final Operation operation,
      final DataSet dataSet,
      final DataSource dataSource,
      final TableOrderingStrategy tableOrderingStrategy) {
    logger.debug(
        "Executing operation {} on dataset with {} tables using strategy {}",
        operation,
        dataSet.getTables().size(),
        tableOrderingStrategy);

    try (final var connectionResource = open(dataSource::getConnection)) {
      final var connection = connectionResource.value();
      run(() -> connection.setAutoCommit(false));
      try {
        executeOperation(operation, dataSet, connection, tableOrderingStrategy);
        run(connection::commit);
        logger.debug("Successfully executed operation {}", operation);
      } catch (final DatabaseOperationException e) {
        run(connection::rollback);
        throw e;
      }
    }
  }

  /**
   * Executes the specified operation on the dataset using the provided connection.
   *
   * @param operation the operation to execute
   * @param dataSet the dataset to operate on
   * @param connection the database connection
   * @param tableOrderingStrategy the strategy for determining table processing order
   * @throws DatabaseOperationException if a database error occurs
   */
  void executeOperation(
      final Operation operation,
      final DataSet dataSet,
      final Connection connection,
      final TableOrderingStrategy tableOrderingStrategy) {
    final var tables = resolveTableOrder(dataSet.getTables(), connection, tableOrderingStrategy);

    switch (operation) {
      case NONE -> {
        // Do nothing
      }
      case INSERT -> insertExecutor.execute(tables, connection);
      case UPDATE -> updateExecutor.execute(tables, connection);
      case DELETE -> deleteExecutor.execute(tables, connection);
      case DELETE_ALL -> deleteExecutor.executeDeleteAll(tables, connection);
      case REFRESH -> refreshExecutor.execute(tables, connection);
      case TRUNCATE_TABLE -> truncateExecutor.execute(tables, connection);
      case CLEAN_INSERT -> {
        deleteExecutor.executeDeleteAll(tables.reversed(), connection);
        insertExecutor.execute(tables, connection);
      }
      case TRUNCATE_INSERT -> {
        truncateExecutor.execute(tables.reversed(), connection);
        insertExecutor.execute(tables, connection);
      }
    }
  }

  /**
   * Resolves the table order based on the specified strategy.
   *
   * @param tables the original table list
   * @param connection the database connection for metadata queries
   * @param strategy the table ordering strategy
   * @return the reordered table list
   */
  private List<Table> resolveTableOrder(
      final List<Table> tables, final Connection connection, final TableOrderingStrategy strategy) {
    if (tables.size() <= 1) {
      return tables;
    }

    return switch (strategy) {
      case AUTO -> resolveTableOrderAuto(tables, connection);
      case LOAD_ORDER_FILE -> tables; // Already ordered by load order file during dataset loading
      case FOREIGN_KEY -> resolveTableOrderByForeignKey(tables, connection);
      case ALPHABETICAL -> resolveTableOrderAlphabetically(tables);
    };
  }

  /**
   * Resolves table order using AUTO strategy.
   *
   * <p>AUTO strategy attempts in order:
   *
   * <ol>
   *   <li>Assumes load order file was already applied during dataset loading if present
   *   <li>Foreign key resolution via JDBC metadata
   *   <li>Falls back to original order (which may be alphabetical from dataset loading)
   * </ol>
   *
   * @param tables the original table list
   * @param connection the database connection
   * @return the reordered table list
   */
  private List<Table> resolveTableOrderAuto(final List<Table> tables, final Connection connection) {
    // Try foreign key resolution
    try {
      final var schema = getSchema(connection);
      final var tableNames = tables.stream().map(Table::getName).toList();
      final var orderedNames = foreignKeyResolver.resolveOrder(tableNames, connection, schema);

      if (!tableNames.equals(orderedNames)) {
        logger.debug("Resolved table order based on foreign keys: {}", orderedNames);
        final var tableMap =
            tables.stream().collect(Collectors.toMap(Table::getName, Function.identity()));
        return orderedNames.stream().map(tableMap::get).toList();
      }
    } catch (final DatabaseOperationException e) {
      logger.debug("Foreign key resolution failed, using original order: {}", e.getMessage());
    }

    // Return original order (may already be from load order file or alphabetical)
    return tables;
  }

  /**
   * Resolves table order based on foreign key relationships.
   *
   * @param tables the original table list
   * @param connection the database connection
   * @return the reordered table list
   */
  private List<Table> resolveTableOrderByForeignKey(
      final List<Table> tables, final Connection connection) {
    try {
      final var schema = getSchema(connection);
      final var tableNames = tables.stream().map(Table::getName).toList();
      final var orderedNames = foreignKeyResolver.resolveOrder(tableNames, connection, schema);

      if (tableNames.equals(orderedNames)) {
        logger.debug("No foreign key dependencies found, using original order");
        return tables;
      }

      logger.debug("Resolved table order based on foreign keys: {}", orderedNames);
      final var tableMap =
          tables.stream().collect(Collectors.toMap(Table::getName, Function.identity()));
      return orderedNames.stream().map(tableMap::get).toList();

    } catch (final DatabaseOperationException e) {
      logger.warn("Failed to resolve table order based on foreign keys, using original order", e);
      return tables;
    }
  }

  /**
   * Resolves table order alphabetically by table name.
   *
   * @param tables the original table list
   * @return the alphabetically sorted table list
   */
  private List<Table> resolveTableOrderAlphabetically(final List<Table> tables) {
    logger.debug("Sorting tables alphabetically");
    return tables.stream()
        .sorted(Comparator.comparing(table -> table.getName().value().toLowerCase(Locale.ROOT)))
        .toList();
  }

  /**
   * Gets the schema from the connection.
   *
   * @param connection the database connection
   * @return the schema name, or null if not available
   */
  private @Nullable String getSchema(final Connection connection) {
    return get(connection::getSchema);
  }
}
