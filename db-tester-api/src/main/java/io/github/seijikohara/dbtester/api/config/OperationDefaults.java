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
 *     .preparation(Operation.TRUNCATE_INSERT)
 *     .expectation(Operation.NONE)
 *     .build();
 * }</pre>
 */
public final class OperationDefaults {

  /** The default operation executed before a test runs. */
  private final Operation preparation;

  /** The default operation executed after a test finishes. */
  private final Operation expectation;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private OperationDefaults(final Builder builder) {
    this.preparation = builder.preparation;
    this.expectation = builder.expectation;
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
   * Returns an instance initialised with {@link Operation#CLEAN_INSERT} and {@link Operation#NONE}.
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
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder().preparation(this.preparation).expectation(this.expectation);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof OperationDefaults other)) {
      return false;
    }
    return preparation == other.preparation && expectation == other.expectation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(preparation, expectation);
  }

  @Override
  public String toString() {
    return "OperationDefaults[preparation=" + preparation + ", expectation=" + expectation + ']';
  }

  /** Builder for constructing {@link OperationDefaults} instances. */
  public static final class Builder {

    /** The default operation executed before a test runs. */
    private Operation preparation = Operation.CLEAN_INSERT;

    /** The default operation executed after a test finishes. */
    private Operation expectation = Operation.NONE;

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
     * Builds a new {@link OperationDefaults} instance with the configured values.
     *
     * @return a new OperationDefaults instance
     */
    public OperationDefaults build() {
      return new OperationDefaults(this);
    }
  }
}
