package io.github.seijikohara.dbtester.api.config;

import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Configuration settings for database operation execution behavior.
 *
 * <p>This class encapsulates the settings that control how the framework executes database
 * operations during test preparation. These settings were previously part of {@link
 * ConventionSettings} and have been extracted to provide clearer separation of concerns.
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #standard()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var settings = ExecutionSettings.standard();
 *
 * // Customizing with builder
 * var settings = ExecutionSettings.builder()
 *     .queryTimeout(Duration.ofSeconds(30))
 *     .transactionMode(TransactionMode.AUTO_COMMIT)
 *     .build();
 *
 * // Modifying existing settings
 * var modified = settings.withTransactionMode(TransactionMode.AUTO_COMMIT);
 * }</pre>
 *
 * @see ConventionSettings
 * @see VerificationSettings
 * @see Configuration
 */
public final class ExecutionSettings {

  /** The maximum time to wait for database queries. */
  private final @Nullable Duration queryTimeout;

  /** The transaction behavior for database operations. */
  private final TransactionMode transactionMode;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private ExecutionSettings(final Builder builder) {
    this.queryTimeout = builder.queryTimeout;
    this.transactionMode = builder.transactionMode;
  }

  /**
   * Creates a new builder for constructing ExecutionSettings instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates an execution settings instance populated with the framework defaults.
   *
   * @return settings using no query timeout and SINGLE_TRANSACTION transaction mode
   */
  public static ExecutionSettings standard() {
    return builder().build();
  }

  /**
   * Returns the maximum time to wait for database queries.
   *
   * <p>When {@code null}, the JDBC driver's default timeout is used (typically unlimited). When set
   * to a positive duration, queries exceeding this limit will throw {@link
   * java.sql.SQLTimeoutException}.
   *
   * @return the query timeout duration, or null to use driver default (typically unlimited)
   */
  public @Nullable Duration queryTimeout() {
    return queryTimeout;
  }

  /**
   * Returns the transaction behavior for database operations.
   *
   * @return the transaction mode
   */
  public TransactionMode transactionMode() {
    return transactionMode;
  }

  /**
   * Creates a new ExecutionSettings with the specified query timeout.
   *
   * @param queryTimeout the query timeout duration, or null to use driver default (typically
   *     unlimited)
   * @return a new ExecutionSettings with the specified query timeout
   */
  public ExecutionSettings withQueryTimeout(final @Nullable Duration queryTimeout) {
    return toBuilder().queryTimeout(queryTimeout).build();
  }

  /**
   * Creates a new ExecutionSettings with the specified transaction mode.
   *
   * @param transactionMode the transaction mode to use
   * @return a new ExecutionSettings with the specified transaction mode
   */
  public ExecutionSettings withTransactionMode(final TransactionMode transactionMode) {
    return toBuilder().transactionMode(transactionMode).build();
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder().queryTimeout(this.queryTimeout).transactionMode(this.transactionMode);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExecutionSettings other)) {
      return false;
    }
    return Objects.equals(queryTimeout, other.queryTimeout)
        && transactionMode == other.transactionMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(queryTimeout, transactionMode);
  }

  @Override
  public String toString() {
    return "ExecutionSettings["
        + "queryTimeout="
        + queryTimeout
        + ", transactionMode="
        + transactionMode
        + ']';
  }

  /** Builder for constructing {@link ExecutionSettings} instances. */
  public static final class Builder {

    /** The maximum time to wait for database queries. */
    private @Nullable Duration queryTimeout = null;

    /** The transaction behavior for database operations. */
    private TransactionMode transactionMode = TransactionMode.SINGLE_TRANSACTION;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the maximum time to wait for database queries.
     *
     * @param queryTimeout the query timeout duration, or null for no timeout
     * @return this builder
     */
    public Builder queryTimeout(final @Nullable Duration queryTimeout) {
      this.queryTimeout = queryTimeout;
      return this;
    }

    /**
     * Sets the transaction behavior for database operations.
     *
     * @param transactionMode the transaction mode
     * @return this builder
     */
    public Builder transactionMode(final TransactionMode transactionMode) {
      this.transactionMode = Objects.requireNonNull(transactionMode, "transactionMode");
      return this;
    }

    /**
     * Builds a new {@link ExecutionSettings} instance with the configured values.
     *
     * @return a new ExecutionSettings instance
     */
    public ExecutionSettings build() {
      return new ExecutionSettings(this);
    }
  }
}
