package io.github.seijikohara.dbtester.api.config;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Configuration settings for expectation verification behavior.
 *
 * <p>This class encapsulates the settings that control how the framework verifies expected database
 * state after test execution. These settings were previously part of {@link ConventionSettings} and
 * have been extracted to provide clearer separation of concerns.
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #standard()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var settings = VerificationSettings.standard();
 *
 * // Customizing with builder
 * var settings = VerificationSettings.builder()
 *     .globalExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
 *     .rowOrdering(RowOrdering.UNORDERED)
 *     .retryCount(3)
 *     .build();
 *
 * // Modifying existing settings
 * var modified = settings.withRetryCount(5);
 * }</pre>
 *
 * @see ConventionSettings
 * @see ExecutionSettings
 * @see Configuration
 */
public final class VerificationSettings {

  /** The column names or glob patterns to exclude from all expectation verifications globally. */
  private final Set<String> globalExcludeColumns;

  /** The column comparison strategies applied globally. */
  private final Map<String, ColumnStrategyMapping> globalColumnStrategies;

  /** The default row ordering strategy for expectation verification. */
  private final RowOrdering rowOrdering;

  /** The number of retry attempts for expectation verification. */
  private final int retryCount;

  /** The delay between retry attempts. */
  private final Duration retryDelay;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private VerificationSettings(final Builder builder) {
    this.globalExcludeColumns = Set.copyOf(builder.globalExcludeColumns);
    this.globalColumnStrategies = Map.copyOf(builder.globalColumnStrategies);
    this.rowOrdering = builder.rowOrdering;
    this.retryCount = builder.retryCount;
    this.retryDelay = builder.retryDelay;
  }

  /**
   * Creates a new builder for constructing VerificationSettings instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a verification settings instance populated with the framework defaults.
   *
   * @return settings using no global exclude columns, no global column strategies, ORDERED row
   *     ordering, no retry, and 100ms retry delay
   */
  public static VerificationSettings standard() {
    return builder().build();
  }

  /**
   * Returns the column names or glob patterns to exclude from all expectation verifications
   * globally.
   *
   * <p>Entries containing {@code *} or {@code ?} are treated as glob patterns and matched against
   * actual column names during verification. Entries without wildcards are treated as exact column
   * names.
   *
   * @return an unmodifiable set of column names or patterns
   */
  public Set<String> globalExcludeColumns() {
    return globalExcludeColumns;
  }

  /**
   * Returns the column comparison strategies applied globally.
   *
   * @return an unmodifiable map of column strategies
   */
  public Map<String, ColumnStrategyMapping> globalColumnStrategies() {
    return globalColumnStrategies;
  }

  /**
   * Returns the default row ordering strategy for expectation verification.
   *
   * @return the row ordering strategy
   */
  public RowOrdering rowOrdering() {
    return rowOrdering;
  }

  /**
   * Returns the number of retry attempts for expectation verification.
   *
   * @return the retry count (0 means no retry)
   */
  public int retryCount() {
    return retryCount;
  }

  /**
   * Returns the delay between retry attempts.
   *
   * @return the retry delay duration
   */
  public Duration retryDelay() {
    return retryDelay;
  }

  /**
   * Creates a new VerificationSettings with the specified global exclude columns.
   *
   * @param globalExcludeColumns the column names to exclude globally
   * @return a new VerificationSettings with the specified global exclude columns
   */
  public VerificationSettings withGlobalExcludeColumns(final Set<String> globalExcludeColumns) {
    return toBuilder().globalExcludeColumns(globalExcludeColumns).build();
  }

  /**
   * Creates a new VerificationSettings with the specified global column strategies.
   *
   * @param globalColumnStrategies the column strategies to apply globally
   * @return a new VerificationSettings with the specified global column strategies
   */
  public VerificationSettings withGlobalColumnStrategies(
      final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
    return toBuilder().globalColumnStrategies(globalColumnStrategies).build();
  }

  /**
   * Creates a new VerificationSettings with the specified row ordering strategy.
   *
   * @param rowOrdering the row ordering strategy to use
   * @return a new VerificationSettings with the specified row ordering
   */
  public VerificationSettings withRowOrdering(final RowOrdering rowOrdering) {
    return toBuilder().rowOrdering(rowOrdering).build();
  }

  /**
   * Creates a new VerificationSettings with the specified retry count.
   *
   * @param retryCount the number of retry attempts (0 for no retry)
   * @return a new VerificationSettings with the specified retry count
   * @throws IllegalArgumentException if retryCount is negative
   */
  public VerificationSettings withRetryCount(final int retryCount) {
    return toBuilder().retryCount(retryCount).build();
  }

  /**
   * Creates a new VerificationSettings with the specified retry delay.
   *
   * @param retryDelay the delay between retry attempts
   * @return a new VerificationSettings with the specified retry delay
   */
  public VerificationSettings withRetryDelay(final Duration retryDelay) {
    return toBuilder().retryDelay(retryDelay).build();
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder()
        .globalExcludeColumns(this.globalExcludeColumns)
        .globalColumnStrategies(this.globalColumnStrategies)
        .rowOrdering(this.rowOrdering)
        .retryCount(this.retryCount)
        .retryDelay(this.retryDelay);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VerificationSettings other)) {
      return false;
    }
    return Objects.equals(globalExcludeColumns, other.globalExcludeColumns)
        && Objects.equals(globalColumnStrategies, other.globalColumnStrategies)
        && rowOrdering == other.rowOrdering
        && retryCount == other.retryCount
        && Objects.equals(retryDelay, other.retryDelay);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        globalExcludeColumns, globalColumnStrategies, rowOrdering, retryCount, retryDelay);
  }

  @Override
  public String toString() {
    return "VerificationSettings["
        + "globalExcludeColumns="
        + globalExcludeColumns
        + ", globalColumnStrategies="
        + globalColumnStrategies
        + ", rowOrdering="
        + rowOrdering
        + ", retryCount="
        + retryCount
        + ", retryDelay="
        + retryDelay
        + ']';
  }

  /** Builder for constructing {@link VerificationSettings} instances. */
  public static final class Builder {

    /** The column names or glob patterns to exclude from all expectation verifications globally. */
    private Set<String> globalExcludeColumns = Set.of();

    /** The column comparison strategies applied globally. */
    private Map<String, ColumnStrategyMapping> globalColumnStrategies = Map.of();

    /** The default row ordering strategy for expectation verification. */
    private RowOrdering rowOrdering = RowOrdering.ORDERED;

    /** The number of retry attempts for expectation verification. */
    private int retryCount = 0;

    /** The delay between retry attempts. */
    private Duration retryDelay = Duration.ofMillis(100);

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the column names or glob patterns to exclude from all expectation verifications
     * globally.
     *
     * <p>Entries containing {@code *} or {@code ?} are treated as glob patterns. Example: {@code
     * Set.of("*_AT", "*_BY", "VERSION")} excludes all columns ending with {@code _AT} or {@code
     * _BY}, and the exact column {@code VERSION}.
     *
     * @param globalExcludeColumns the column names or patterns to exclude
     * @return this builder
     */
    public Builder globalExcludeColumns(final Set<String> globalExcludeColumns) {
      this.globalExcludeColumns =
          Objects.requireNonNull(globalExcludeColumns, "globalExcludeColumns");
      return this;
    }

    /**
     * Sets the column comparison strategies applied globally.
     *
     * @param globalColumnStrategies the column strategies
     * @return this builder
     */
    public Builder globalColumnStrategies(
        final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
      this.globalColumnStrategies =
          Objects.requireNonNull(globalColumnStrategies, "globalColumnStrategies");
      return this;
    }

    /**
     * Sets the default row ordering strategy for expectation verification.
     *
     * @param rowOrdering the row ordering strategy
     * @return this builder
     */
    public Builder rowOrdering(final RowOrdering rowOrdering) {
      if (rowOrdering == RowOrdering.UNSET) {
        throw new IllegalArgumentException(
            "RowOrdering.UNSET is only valid in annotation contexts;"
                + " use ORDERED or UNORDERED for programmatic configuration");
      }
      this.rowOrdering = Objects.requireNonNull(rowOrdering, "rowOrdering");
      return this;
    }

    /**
     * Sets the number of retry attempts for expectation verification.
     *
     * @param retryCount the retry count (0 means no retry)
     * @return this builder
     * @throws IllegalArgumentException if retryCount is negative
     */
    public Builder retryCount(final int retryCount) {
      if (retryCount < 0) {
        throw new IllegalArgumentException("retryCount must not be negative");
      }
      this.retryCount = retryCount;
      return this;
    }

    /**
     * Sets the delay between retry attempts.
     *
     * @param retryDelay the retry delay duration
     * @return this builder
     */
    public Builder retryDelay(final Duration retryDelay) {
      this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
      return this;
    }

    /**
     * Builds a new {@link VerificationSettings} instance with the configured values.
     *
     * @return a new VerificationSettings instance
     */
    public VerificationSettings build() {
      return new VerificationSettings(this);
    }
  }
}
