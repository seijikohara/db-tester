package io.github.seijikohara.dbtester.api.preparation;

import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.time.Duration;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for programmatic database preparation operations.
 *
 * <p>This record encapsulates execution settings for {@link DatabasePreparation} methods. Use
 * {@link #standard()} to obtain an instance with default values, then use {@code with*} methods to
 * customize individual settings.
 *
 * <p>Default values:
 *
 * <ul>
 *   <li>{@code tableOrderingStrategy} = {@link TableOrderingStrategy#AUTO}
 *   <li>{@code transactionMode} = {@link TransactionMode#SINGLE_TRANSACTION}
 *   <li>{@code queryTimeout} = {@code null} (no timeout)
 *   <li>{@code batchSize} = {@code 0} (single batch)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Standard defaults
 * var config = PreparationConfig.standard();
 *
 * // Custom configuration
 * var config = PreparationConfig.standard()
 *     .withTransactionMode(TransactionMode.AUTO_COMMIT)
 *     .withBatchSize(1000);
 *
 * DatabasePreparation.execute(dataSource, tableSet, Operation.CLEAN_INSERT, config);
 * }</pre>
 *
 * @param tableOrderingStrategy the strategy for determining table processing order
 * @param transactionMode the transaction behavior mode
 * @param queryTimeout the query timeout, or null for no timeout
 * @param batchSize the number of rows per INSERT batch, or zero for single-batch execution
 * @see DatabasePreparation
 */
public record PreparationConfig(
    TableOrderingStrategy tableOrderingStrategy,
    TransactionMode transactionMode,
    @Nullable Duration queryTimeout,
    int batchSize) {

  /**
   * Creates a new instance with validation.
   *
   * @throws IllegalArgumentException if batchSize is negative
   */
  public PreparationConfig {
    if (batchSize < 0) {
      throw new IllegalArgumentException(
          String.format("batchSize must be zero or positive, but was: %d", batchSize));
    }
  }

  /**
   * Returns an instance with standard default values.
   *
   * <p>Defaults: {@link TableOrderingStrategy#AUTO}, {@link TransactionMode#SINGLE_TRANSACTION}, no
   * query timeout, single-batch execution.
   *
   * @return a new instance with standard defaults
   */
  public static PreparationConfig standard() {
    return new PreparationConfig(
        TableOrderingStrategy.AUTO, TransactionMode.SINGLE_TRANSACTION, null, 0);
  }

  /**
   * Returns a new instance with the specified table ordering strategy.
   *
   * @param tableOrderingStrategy the table ordering strategy
   * @return a new instance with the specified strategy
   */
  public PreparationConfig withTableOrderingStrategy(
      final TableOrderingStrategy tableOrderingStrategy) {
    return new PreparationConfig(
        tableOrderingStrategy, this.transactionMode, this.queryTimeout, this.batchSize);
  }

  /**
   * Returns a new instance with the specified transaction mode.
   *
   * @param transactionMode the transaction mode
   * @return a new instance with the specified mode
   */
  public PreparationConfig withTransactionMode(final TransactionMode transactionMode) {
    return new PreparationConfig(
        this.tableOrderingStrategy, transactionMode, this.queryTimeout, this.batchSize);
  }

  /**
   * Returns a new instance with the specified query timeout.
   *
   * @param queryTimeout the query timeout, or null for no timeout
   * @return a new instance with the specified timeout
   */
  public PreparationConfig withQueryTimeout(final @Nullable Duration queryTimeout) {
    return new PreparationConfig(
        this.tableOrderingStrategy, this.transactionMode, queryTimeout, this.batchSize);
  }

  /**
   * Returns a new instance with the specified batch size.
   *
   * @param batchSize the number of rows per batch, or zero for single-batch execution
   * @return a new instance with the specified batch size
   * @throws IllegalArgumentException if batchSize is negative
   */
  public PreparationConfig withBatchSize(final int batchSize) {
    return new PreparationConfig(
        this.tableOrderingStrategy, this.transactionMode, this.queryTimeout, batchSize);
  }
}
