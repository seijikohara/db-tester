package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
import java.util.Objects;
import java.util.ServiceLoader;
import org.jspecify.annotations.Nullable;

/**
 * Aggregates the runtime configuration consumed by the database testing extension.
 *
 * <p>A {@code Configuration} ties together five orthogonal aspects:
 *
 * <ul>
 *   <li>{@link ConventionSettings} specify how the extension resolves dataset directories.
 *   <li>{@link VerificationSettings} control expectation verification behavior (column exclusions,
 *       retry, row ordering).
 *   <li>{@link ExecutionSettings} control database operation execution behavior (query timeout,
 *       transaction mode).
 *   <li>{@link OperationDefaults} provide the default database operations for preparation and
 *       expectation phases.
 *   <li>{@link DataSetLoader} describes how datasets are materialised and filtered.
 * </ul>
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #defaults()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var config = Configuration.defaults();
 *
 * // Customizing with builder
 * var config = Configuration.builder()
 *     .conventions(ConventionSettings.builder()
 *         .dataFormat(DataFormat.TSV)
 *         .build())
 *     .verification(VerificationSettings.builder()
 *         .globalExcludeColumns(Set.of("CREATED_AT"))
 *         .retryCount(3)
 *         .build())
 *     .execution(ExecutionSettings.builder()
 *         .queryTimeout(Duration.ofSeconds(30))
 *         .build())
 *     .operations(OperationDefaults.builder()
 *         .preparation(Operation.TRUNCATE_INSERT)
 *         .build())
 *     .build();
 * }</pre>
 */
public final class Configuration {

  /** Lazy holder for the default DataSetLoader instance loaded via SPI. */
  private static final class LoaderHolder {

    /** The singleton DataSetLoader instance. */
    private static final DataSetLoader INSTANCE = loadProvider();

    /** Private constructor to prevent instantiation. */
    private LoaderHolder() {}

    /**
     * Loads the DataSetLoader implementation via ServiceLoader.
     *
     * @return the DataSetLoader instance
     */
    private static DataSetLoader loadProvider() {
      return ServiceLoader.load(DataSetLoaderProvider.class)
          .findFirst()
          .map(DataSetLoaderProvider::getLoader)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "No DataSetLoaderProvider implementation found. "
                          + "Add db-tester-core to your classpath."));
    }
  }

  /** The resolution rules for locating datasets. */
  private final ConventionSettings conventions;

  /** The verification behavior settings. */
  private final VerificationSettings verification;

  /** The execution behavior settings. */
  private final ExecutionSettings execution;

  /** The default database operations. */
  private final OperationDefaults operations;

  /** The strategy for constructing datasets. */
  private final DataSetLoader loader;

  /**
   * Creates a new Configuration from the builder.
   *
   * @param builder the builder containing configuration values
   */
  @SuppressWarnings("removal")
  private Configuration(final Builder builder) {
    this.conventions = builder.conventions;
    this.operations = builder.operations;
    this.loader = builder.loader != null ? builder.loader : LoaderHolder.INSTANCE;
    this.verification =
        builder.verification != null
            ? builder.verification
            : deriveVerificationFromConventions(builder.conventions);
    this.execution =
        builder.execution != null
            ? builder.execution
            : deriveExecutionFromConventions(builder.conventions);
  }

  /**
   * Derives verification settings from convention settings for backward compatibility.
   *
   * <p>When verification settings are not explicitly provided, this method extracts the relevant
   * properties from the convention settings to ensure existing code that sets verification-related
   * properties on ConventionSettings continues to work.
   *
   * @param conventions the convention settings to derive from
   * @return verification settings derived from the conventions
   */
  @SuppressWarnings("removal")
  private static VerificationSettings deriveVerificationFromConventions(
      final ConventionSettings conventions) {
    return VerificationSettings.builder()
        .globalExcludeColumns(conventions.globalExcludeColumns())
        .globalColumnStrategies(conventions.globalColumnStrategies())
        .rowOrdering(conventions.rowOrdering())
        .retryCount(conventions.retryCount())
        .retryDelay(conventions.retryDelay())
        .build();
  }

  /**
   * Derives execution settings from convention settings for backward compatibility.
   *
   * <p>When execution settings are not explicitly provided, this method extracts the relevant
   * properties from the convention settings to ensure existing code that sets execution-related
   * properties on ConventionSettings continues to work.
   *
   * @param conventions the convention settings to derive from
   * @return execution settings derived from the conventions
   */
  @SuppressWarnings("removal")
  private static ExecutionSettings deriveExecutionFromConventions(
      final ConventionSettings conventions) {
    return ExecutionSettings.builder()
        .queryTimeout(conventions.queryTimeout())
        .transactionMode(conventions.transactionMode())
        .build();
  }

  /**
   * Creates a new builder for constructing Configuration instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns a configuration that applies the framework defaults for all components.
   *
   * @return configuration initialised with standard conventions, verification, execution,
   *     operations, and loader
   */
  public static Configuration defaults() {
    return builder().build();
  }

  /**
   * Returns the resolution rules for locating datasets.
   *
   * @return the convention settings
   */
  public ConventionSettings conventions() {
    return conventions;
  }

  /**
   * Returns the verification behavior settings.
   *
   * @return the verification settings
   */
  public VerificationSettings verification() {
    return verification;
  }

  /**
   * Returns the execution behavior settings.
   *
   * @return the execution settings
   */
  public ExecutionSettings execution() {
    return execution;
  }

  /**
   * Returns the default database operations.
   *
   * @return the operation defaults
   */
  public OperationDefaults operations() {
    return operations;
  }

  /**
   * Returns the strategy for constructing datasets.
   *
   * @return the dataset loader
   */
  public DataSetLoader loader() {
    return loader;
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder()
        .conventions(this.conventions)
        .verification(this.verification)
        .execution(this.execution)
        .operations(this.operations)
        .loader(this.loader);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Configuration other)) {
      return false;
    }
    return Objects.equals(conventions, other.conventions)
        && Objects.equals(verification, other.verification)
        && Objects.equals(execution, other.execution)
        && Objects.equals(operations, other.operations)
        && Objects.equals(loader, other.loader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conventions, verification, execution, operations, loader);
  }

  @Override
  public String toString() {
    return "Configuration["
        + "conventions="
        + conventions
        + ", verification="
        + verification
        + ", execution="
        + execution
        + ", operations="
        + operations
        + ", loader="
        + loader
        + ']';
  }

  /** Builder for constructing {@link Configuration} instances. */
  public static final class Builder {

    /** The resolution rules for locating datasets. */
    private ConventionSettings conventions = ConventionSettings.standard();

    /** The verification behavior settings, or null to derive from conventions. */
    private @Nullable VerificationSettings verification = null;

    /** The execution behavior settings, or null to derive from conventions. */
    private @Nullable ExecutionSettings execution = null;

    /** The default database operations. */
    private OperationDefaults operations = OperationDefaults.standard();

    /** The strategy for constructing datasets. */
    private @Nullable DataSetLoader loader = null;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the resolution rules for locating datasets.
     *
     * @param conventions the convention settings
     * @return this builder
     */
    public Builder conventions(final ConventionSettings conventions) {
      this.conventions = Objects.requireNonNull(conventions, "conventions");
      return this;
    }

    /**
     * Sets the verification behavior settings.
     *
     * <p>If not set, verification settings are derived from the convention settings for backward
     * compatibility.
     *
     * @param verification the verification settings
     * @return this builder
     */
    public Builder verification(final VerificationSettings verification) {
      this.verification = Objects.requireNonNull(verification, "verification");
      return this;
    }

    /**
     * Sets the execution behavior settings.
     *
     * <p>If not set, execution settings are derived from the convention settings for backward
     * compatibility.
     *
     * @param execution the execution settings
     * @return this builder
     */
    public Builder execution(final ExecutionSettings execution) {
      this.execution = Objects.requireNonNull(execution, "execution");
      return this;
    }

    /**
     * Sets the default database operations.
     *
     * @param operations the operation defaults
     * @return this builder
     */
    public Builder operations(final OperationDefaults operations) {
      this.operations = Objects.requireNonNull(operations, "operations");
      return this;
    }

    /**
     * Sets the strategy for constructing datasets.
     *
     * <p>If not set, the loader is loaded via SPI.
     *
     * @param loader the dataset loader
     * @return this builder
     */
    public Builder loader(final DataSetLoader loader) {
      this.loader = Objects.requireNonNull(loader, "loader");
      return this;
    }

    /**
     * Builds a new {@link Configuration} instance with the configured values.
     *
     * <p>If {@link #verification(VerificationSettings)} or {@link #execution(ExecutionSettings)}
     * have not been called, the corresponding settings are derived from the convention settings for
     * backward compatibility.
     *
     * @return a new Configuration instance
     */
    public Configuration build() {
      return new Configuration(this);
    }
  }
}
