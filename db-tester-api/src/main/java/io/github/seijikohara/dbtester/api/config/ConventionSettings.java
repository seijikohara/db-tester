package io.github.seijikohara.dbtester.api.config;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Defines the naming conventions used to locate datasets and filter scenarios.
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #standard()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var settings = ConventionSettings.standard();
 *
 * // Customizing with builder
 * var settings = ConventionSettings.builder()
 *     .expectationSuffix("/expected")
 *     .dataFormat(DataFormat.TSV)
 *     .build();
 *
 * // Modifying existing settings
 * var modified = settings.withDataFormat(DataFormat.JSON);
 * }</pre>
 */
public final class ConventionSettings {

  /**
   * Default suffix appended to the preparation directory when resolving expectation datasets.
   *
   * <p>This suffix is typically a subdirectory name that separates expected outcome data from
   * preparation data.
   */
  public static final String DEFAULT_EXPECTATION_SUFFIX = "/expected";

  /**
   * Default column name that identifies scenario markers in scenario-aware dataset formats.
   *
   * <p>Rows containing this column are filtered based on scenario names specified in test
   * annotations or derived from test method names.
   */
  public static final String DEFAULT_SCENARIO_MARKER = "[Scenario]";

  /**
   * Default file name for specifying table loading order in dataset directories.
   *
   * <p>This file contains one table name per line, specifying the order in which tables should be
   * loaded during database operations.
   */
  public static final String DEFAULT_LOAD_ORDER_FILE_NAME = "load-order.txt";

  /** The base directory for dataset resolution. */
  private final @Nullable String baseDirectory;

  /** The suffix appended to base path for expectation datasets. */
  private final String expectationSuffix;

  /** The column name used to identify scenario rows. */
  private final String scenarioMarker;

  /** The data format to use when loading dataset files. */
  private final DataFormat dataFormat;

  /** The strategy for merging tables. */
  private final TableMergeStrategy tableMergeStrategy;

  /** The file name used to specify table loading order. */
  private final String loadOrderFileName;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private ConventionSettings(final Builder builder) {
    this.baseDirectory = builder.baseDirectory;
    this.expectationSuffix = builder.expectationSuffix;
    this.scenarioMarker = builder.scenarioMarker;
    this.dataFormat = builder.dataFormat;
    this.tableMergeStrategy = builder.tableMergeStrategy;
    this.loadOrderFileName = builder.loadOrderFileName;
  }

  /**
   * Creates a new builder for constructing ConventionSettings instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a convention instance populated with the framework defaults.
   *
   * @return conventions using classpath-relative discovery, {@value #DEFAULT_EXPECTATION_SUFFIX}
   *     suffix, {@value #DEFAULT_SCENARIO_MARKER} marker, AUTO format, UNION_ALL merge strategy,
   *     and {@value #DEFAULT_LOAD_ORDER_FILE_NAME} load order file
   */
  public static ConventionSettings standard() {
    return builder().build();
  }

  /**
   * Returns the base directory for dataset resolution.
   *
   * @return the base directory path, or null for convention-based resolution
   */
  public @Nullable String baseDirectory() {
    return baseDirectory;
  }

  /**
   * Returns the suffix appended to base path for expectation datasets.
   *
   * @return the expectation suffix
   */
  public String expectationSuffix() {
    return expectationSuffix;
  }

  /**
   * Returns the column name used to identify scenario rows.
   *
   * @return the scenario marker
   */
  public String scenarioMarker() {
    return scenarioMarker;
  }

  /**
   * Returns the data format to use when loading dataset files.
   *
   * @return the data format
   */
  public DataFormat dataFormat() {
    return dataFormat;
  }

  /**
   * Returns the strategy for merging tables when multiple DataSets contain the same table.
   *
   * @return the table merge strategy
   */
  public TableMergeStrategy tableMergeStrategy() {
    return tableMergeStrategy;
  }

  /**
   * Returns the file name used to specify table loading order.
   *
   * @return the load order file name
   */
  public String loadOrderFileName() {
    return loadOrderFileName;
  }

  /**
   * Creates a new ConventionSettings with the specified base directory.
   *
   * @param baseDirectory the base directory path, or null for convention-based resolution
   * @return a new ConventionSettings with the specified base directory
   */
  public ConventionSettings withBaseDirectory(final @Nullable String baseDirectory) {
    return toBuilder().baseDirectory(baseDirectory).build();
  }

  /**
   * Creates a new ConventionSettings with the specified expectation suffix.
   *
   * @param expectationSuffix the suffix appended to base path for expectation datasets
   * @return a new ConventionSettings with the specified expectation suffix
   */
  public ConventionSettings withExpectationSuffix(final String expectationSuffix) {
    return toBuilder().expectationSuffix(expectationSuffix).build();
  }

  /**
   * Creates a new ConventionSettings with the specified scenario marker.
   *
   * @param scenarioMarker the column name used to identify scenario rows
   * @return a new ConventionSettings with the specified scenario marker
   */
  public ConventionSettings withScenarioMarker(final String scenarioMarker) {
    return toBuilder().scenarioMarker(scenarioMarker).build();
  }

  /**
   * Creates a new ConventionSettings with the specified data format.
   *
   * @param dataFormat the data format to use
   * @return a new ConventionSettings with the specified data format
   */
  public ConventionSettings withDataFormat(final DataFormat dataFormat) {
    return toBuilder().dataFormat(dataFormat).build();
  }

  /**
   * Creates a new ConventionSettings with the specified table merge strategy.
   *
   * @param tableMergeStrategy the table merge strategy to use
   * @return a new ConventionSettings with the specified merge strategy
   */
  public ConventionSettings withTableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
    return toBuilder().tableMergeStrategy(tableMergeStrategy).build();
  }

  /**
   * Creates a new ConventionSettings with the specified load order file name.
   *
   * @param loadOrderFileName the load order file name to use
   * @return a new ConventionSettings with the specified load order file name
   */
  public ConventionSettings withLoadOrderFileName(final String loadOrderFileName) {
    return toBuilder().loadOrderFileName(loadOrderFileName).build();
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder()
        .baseDirectory(this.baseDirectory)
        .expectationSuffix(this.expectationSuffix)
        .scenarioMarker(this.scenarioMarker)
        .dataFormat(this.dataFormat)
        .tableMergeStrategy(this.tableMergeStrategy)
        .loadOrderFileName(this.loadOrderFileName);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionSettings other)) {
      return false;
    }
    return Objects.equals(baseDirectory, other.baseDirectory)
        && Objects.equals(expectationSuffix, other.expectationSuffix)
        && Objects.equals(scenarioMarker, other.scenarioMarker)
        && dataFormat == other.dataFormat
        && tableMergeStrategy == other.tableMergeStrategy
        && Objects.equals(loadOrderFileName, other.loadOrderFileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseDirectory,
        expectationSuffix,
        scenarioMarker,
        dataFormat,
        tableMergeStrategy,
        loadOrderFileName);
  }

  @Override
  public String toString() {
    return "ConventionSettings["
        + "baseDirectory="
        + baseDirectory
        + ", expectationSuffix="
        + expectationSuffix
        + ", scenarioMarker="
        + scenarioMarker
        + ", dataFormat="
        + dataFormat
        + ", tableMergeStrategy="
        + tableMergeStrategy
        + ", loadOrderFileName="
        + loadOrderFileName
        + ']';
  }

  /** Builder for constructing {@link ConventionSettings} instances. */
  public static final class Builder {

    /** The base directory for dataset resolution. */
    private @Nullable String baseDirectory = null;

    /** The suffix appended to base path for expectation datasets. */
    private String expectationSuffix = DEFAULT_EXPECTATION_SUFFIX;

    /** The column name used to identify scenario rows. */
    private String scenarioMarker = DEFAULT_SCENARIO_MARKER;

    /** The data format to use when loading dataset files. */
    private DataFormat dataFormat = DataFormat.AUTO;

    /** The strategy for merging tables. */
    private TableMergeStrategy tableMergeStrategy = TableMergeStrategy.UNION_ALL;

    /** The file name used to specify table loading order. */
    private String loadOrderFileName = DEFAULT_LOAD_ORDER_FILE_NAME;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the base directory for dataset resolution.
     *
     * @param baseDirectory the base directory path, or null for convention-based resolution
     * @return this builder
     */
    public Builder baseDirectory(final @Nullable String baseDirectory) {
      this.baseDirectory = baseDirectory;
      return this;
    }

    /**
     * Sets the suffix appended to base path for expectation datasets.
     *
     * @param expectationSuffix the expectation suffix
     * @return this builder
     */
    public Builder expectationSuffix(final String expectationSuffix) {
      this.expectationSuffix = Objects.requireNonNull(expectationSuffix, "expectationSuffix");
      return this;
    }

    /**
     * Sets the column name used to identify scenario rows.
     *
     * @param scenarioMarker the scenario marker
     * @return this builder
     */
    public Builder scenarioMarker(final String scenarioMarker) {
      this.scenarioMarker = Objects.requireNonNull(scenarioMarker, "scenarioMarker");
      return this;
    }

    /**
     * Sets the data format to use when loading dataset files.
     *
     * @param dataFormat the data format
     * @return this builder
     */
    public Builder dataFormat(final DataFormat dataFormat) {
      this.dataFormat = Objects.requireNonNull(dataFormat, "dataFormat");
      return this;
    }

    /**
     * Sets the strategy for merging tables when multiple DataSets contain the same table.
     *
     * @param tableMergeStrategy the table merge strategy
     * @return this builder
     */
    public Builder tableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
      this.tableMergeStrategy = Objects.requireNonNull(tableMergeStrategy, "tableMergeStrategy");
      return this;
    }

    /**
     * Sets the file name used to specify table loading order.
     *
     * @param loadOrderFileName the load order file name
     * @return this builder
     */
    public Builder loadOrderFileName(final String loadOrderFileName) {
      this.loadOrderFileName = Objects.requireNonNull(loadOrderFileName, "loadOrderFileName");
      return this;
    }

    /**
     * Builds a new {@link ConventionSettings} instance with the configured values.
     *
     * @return a new ConventionSettings instance
     */
    public ConventionSettings build() {
      return new ConventionSettings(this);
    }
  }
}
