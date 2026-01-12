package io.github.seijikohara.dbtester.api.config;

/**
 * Defines the transaction behavior for database operations.
 *
 * <p>This enum controls how transactions are managed during test data preparation and cleanup
 * phases. The default is {@link #SINGLE_TRANSACTION} for atomic operations.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Use auto-commit for foreign key constraint issues
 * var settings = ConventionSettings.standard()
 *     .withTransactionMode(TransactionMode.AUTO_COMMIT);
 *
 * // Use external transaction management
 * var settings = ConventionSettings.standard()
 *     .withTransactionMode(TransactionMode.NONE);
 * }</pre>
 *
 * @see ConventionSettings
 */
public enum TransactionMode {

  /**
   * Each statement is committed immediately (connection autoCommit = true).
   *
   * <p>Use this mode when:
   *
   * <ul>
   *   <li>Foreign key constraints prevent transactional insertion
   *   <li>Debugging requires seeing data immediately
   *   <li>Database does not support transactions for certain operations
   * </ul>
   *
   * <p>Warning: Partial failures cannot be rolled back in this mode.
   */
  AUTO_COMMIT,

  /**
   * All statements execute within a single transaction (default).
   *
   * <p>The transaction is committed after all operations complete successfully, or rolled back if
   * any operation fails. This provides atomic all-or-nothing semantics.
   *
   * <p>This is the default behavior and is recommended for most use cases.
   */
  SINGLE_TRANSACTION,

  /**
   * No transaction management (connection state unchanged).
   *
   * <p>Use this mode when:
   *
   * <ul>
   *   <li>External transaction management is required (e.g., Spring's @Transactional)
   *   <li>The test framework manages transactions
   *   <li>Custom transaction boundaries are needed
   * </ul>
   *
   * <p>Note: The connection's autoCommit state is not modified. Ensure proper transaction
   * management is in place.
   */
  NONE
}
