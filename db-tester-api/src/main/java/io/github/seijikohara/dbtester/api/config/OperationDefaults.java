package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.operation.Operation;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Encapsulates the default {@link Operation} values applied to the preparation and expectation
 * phases.
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #standard()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var defaults = OperationDefaults.standard();
 *
 * // Customizing with builder
 * var defaults = OperationDefaults.builder()
 * .preparation(Operation.TRUNCATE_INSERT)
 * .expectation(Operation.NONE)
 * .floatingPointEpsilon(1e-9)
 * .build();
 * }</pre>
 */
public final class OperationDefaults {

  /** The default epsilon value used for floating-point comparisons. */
  public static final double DEFAULT_FLOATING_POINT_EPSILON = 1e-6;

  /** The default operation executed before a test runs. */
  private final Operation preparation;

  /** The default operation executed after a test finishes. */
  private final Operation expectation;

  /** The epsilon value used for floating-point comparisons. */
  private final double floatingPointEpsilon;

  /** The number of rows per batch for INSERT operations. Zero means all rows in a single batch. */
  private final int batchSize;

  /** Selects how the comparison engine reacts to parse failures inside flexible strategies. */
  private final ComparisonMode comparisonMode;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private OperationDefaults(final Builder builder) {
    this.preparation = builder.preparation;
    this.expectation = builder.expectation;
    this.floatingPointEpsilon = builder.floatingPointEpsilon;
    this.batchSize = builder.batchSize;
    this.comparisonMode = builder.comparisonMode;
  }

  /**
   * Creates a new builder for constructing OperationDefaults instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns an instance initialised with {@link Operation#CLEAN_INSERT}, {@link Operation#NONE} and
   * the default floating-point epsilon.
   *
   * @return defaults using {@code CLEAN_INSERT} for preparation and {@code NONE} for verification
   */
  public static OperationDefaults standard() {
    return builder().build();
  }

  /**
   * Returns the default operation executed before a test runs.
   *
   * @return the preparation operation
   */
  public Operation preparation() {
    return preparation;
  }

  /**
   * Returns the default operation executed after a test finishes.
   *
   * @return the expectation operation
   */
  public Operation expectation() {
    return expectation;
  }

  /**
   * Returns the epsilon value used for floating-point comparisons.
   *
   * @return the floating-point epsilon
   */
  public double floatingPointEpsilon() {
    return floatingPointEpsilon;
  }

  /**
   * Returns the number of rows per batch for INSERT operations.
   *
   * <p>A value of zero means all rows are added to a single batch before execution (the default
   * behavior). A positive value causes the executor to flush the batch every N rows, reducing
   * memory usage for large datasets.
   *
   * @return the batch size, or zero for single-batch execution
   */
  public int batchSize() {
    return batchSize;
  }

  /**
   * Returns the comparison mode that governs flexible strategies.
   *
   * @return the comparison mode
   */
  public ComparisonMode comparisonMode() {
    return comparisonMode;
  }

  /**
   * Creates a new OperationDefaults with the specified preparation operation.
   *
   * @param preparation the preparation operation
   * @return a new OperationDefaults with the specified preparation operation
   */
  public OperationDefaults withPreparation(final Operation preparation) {
    return toBuilder().preparation(preparation).build();
  }

  /**
   * Creates a new OperationDefaults with the specified expectation operation.
   *
   * @param expectation the expectation operation
   * @return a new OperationDefaults with the specified expectation operation
   */
  public OperationDefaults withExpectation(final Operation expectation) {
    return toBuilder().expectation(expectation).build();
  }

  /**
   * Creates a new OperationDefaults with the specified epsilon value.
   *
   * @param floatingPointEpsilon the epsilon value
   * @return a new OperationDefaults with the specified epsilon
   */
  public OperationDefaults withFloatingPointEpsilon(final double floatingPointEpsilon) {
    return toBuilder().floatingPointEpsilon(floatingPointEpsilon).build();
  }

  /**
   * Creates a new OperationDefaults with the specified batch size.
   *
   * @param batchSize the batch size
   * @return a new OperationDefaults with the specified batch size
   */
  public OperationDefaults withBatchSize(final int batchSize) {
    return toBuilder().batchSize(batchSize).build();
  }

  /**
   * Creates a new OperationDefaults with the specified comparison mode.
   *
   * @param comparisonMode the comparison mode
   * @return a new OperationDefaults with the specified comparison mode
   */
  public OperationDefaults withComparisonMode(final ComparisonMode comparisonMode) {
    return toBuilder().comparisonMode(comparisonMode).build();
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder()
        .preparation(this.preparation)
        .expectation(this.expectation)
        .floatingPointEpsilon(this.floatingPointEpsilon)
        .batchSize(this.batchSize)
        .comparisonMode(this.comparisonMode);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OperationDefaults other)) {
      return false;
    }
    return preparation == other.preparation
        && expectation == other.expectation
        && Double.compare(floatingPointEpsilon, other.floatingPointEpsilon) == 0
        && batchSize == other.batchSize
        && comparisonMode == other.comparisonMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(preparation, expectation, floatingPointEpsilon, batchSize, comparisonMode);
  }

  @Override
  public String toString() {
    return "OperationDefaults[preparation="
        + preparation
        + ", expectation="
        + expectation
        + ", floatingPointEpsilon="
        + floatingPointEpsilon
        + ", batchSize="
        + batchSize
        + ", comparisonMode="
        + comparisonMode
        + ']';
  }

  /** Builder for constructing {@link OperationDefaults} instances. */
  public static final class Builder {

    /** The default operation executed before a test runs. */
    private Operation preparation = Operation.CLEAN_INSERT;

    /** The default operation executed after a test finishes. */
    private Operation expectation = Operation.NONE;

    /** The default epsilon value used for floating-point comparisons. */
    private double floatingPointEpsilon = DEFAULT_FLOATING_POINT_EPSILON;

    /** The number of rows per batch for INSERT operations. */
    private int batchSize;

    /** The comparison mode that governs flexible strategies. */
    private ComparisonMode comparisonMode = ComparisonMode.STRICT;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the default operation executed before a test runs.
     *
     * @param preparation the preparation operation
     * @return this builder
     */
    public Builder preparation(final Operation preparation) {
      this.preparation = Objects.requireNonNull(preparation, "preparation");
      return this;
    }

    /**
     * Sets the default operation executed after a test finishes.
     *
     * @param expectation the expectation operation
     * @return this builder
     */
    public Builder expectation(final Operation expectation) {
      this.expectation = Objects.requireNonNull(expectation, "expectation");
      return this;
    }

    /**
     * Sets the epsilon value used for floating-point comparisons.
     *
     * @param floatingPointEpsilon the epsilon value
     * @return this builder
     */
    public Builder floatingPointEpsilon(final double floatingPointEpsilon) {
      this.floatingPointEpsilon = floatingPointEpsilon;
      return this;
    }

    /**
     * Sets the number of rows per batch for INSERT operations.
     *
     * <p>A value of zero means all rows are added to a single batch (default). A positive value
     * causes the executor to flush the batch every N rows.
     *
     * @param batchSize the batch size (zero or positive)
     * @return this builder
     * @throws IllegalArgumentException if batchSize is negative
     */
    public Builder batchSize(final int batchSize) {
      if (batchSize < 0) {
        throw new IllegalArgumentException(
            String.format("batchSize must be zero or positive, but was: %d", batchSize));
      }
      this.batchSize = batchSize;
      return this;
    }

    /**
     * Sets the comparison mode that governs flexible strategies.
     *
     * @param comparisonMode the comparison mode
     * @return this builder
     */
    public Builder comparisonMode(final ComparisonMode comparisonMode) {
      this.comparisonMode = Objects.requireNonNull(comparisonMode, "comparisonMode");
      return this;
    }

    /**
     * Builds a new {@link OperationDefaults} instance with the configured values.
     *
     * @return a new OperationDefaults instance
     */
    public OperationDefaults build() {
      return new OperationDefaults(this);
    }
  }
}
