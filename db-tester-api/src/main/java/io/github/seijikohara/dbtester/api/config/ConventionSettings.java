package io.github.seijikohara.dbtester.api.config;

import org.jspecify.annotations.Nullable;

/**
 * Defines the naming conventions used to locate datasets and filter scenarios.
 *
 * @param baseDirectory optional absolute or relative directory that anchors all datasets; {@code
 *     null} instructs the loader to resolve locations from the classpath
 * @param expectationSuffix directory appended to the preparation path when resolving expectation
 *     datasets
 * @param scenarioMarker column name that denotes the scenario marker used by scenario-aware formats
 * @param dataFormat the file format to use when loading dataset files (CSV or TSV)
 * @param tableMergeStrategy the strategy for merging tables when multiple DataSets contain the same
 *     table
 */
public record ConventionSettings(
    @Nullable String baseDirectory,
    String expectationSuffix,
    String scenarioMarker,
    DataFormat dataFormat,
    TableMergeStrategy tableMergeStrategy) {

  /**
   * Default base directory for dataset resolution.
   *
   * <p>A {@code null} value instructs the loader to resolve dataset locations relative to the test
   * class package on the classpath.
   */
  private static final @Nullable String DEFAULT_BASE_DIRECTORY = null;

  /**
   * Default suffix appended to the preparation directory when resolving expectation datasets.
   *
   * <p>This suffix is typically a subdirectory name that separates expected outcome data from
   * preparation data.
   */
  private static final String DEFAULT_EXPECTATION_SUFFIX = "/expected";

  /**
   * Default column name that identifies scenario markers in scenario-aware dataset formats.
   *
   * <p>Rows containing this column are filtered based on scenario names specified in test
   * annotations or derived from test method names.
   */
  private static final String DEFAULT_SCENARIO_MARKER = "[Scenario]";

  /** Default file format for dataset files. */
  private static final DataFormat DEFAULT_DATA_FORMAT = DataFormat.CSV;

  /** Default strategy for merging tables from multiple datasets. */
  private static final TableMergeStrategy DEFAULT_TABLE_MERGE_STRATEGY =
      TableMergeStrategy.UNION_ALL;

  /**
   * Creates a convention instance populated with the framework defaults.
   *
   * @return conventions using classpath-relative discovery, "/expected" suffix, "[Scenario]"
   *     marker, CSV format, and UNION_ALL merge strategy
   */
  public static ConventionSettings standard() {
    return new ConventionSettings(
        DEFAULT_BASE_DIRECTORY,
        DEFAULT_EXPECTATION_SUFFIX,
        DEFAULT_SCENARIO_MARKER,
        DEFAULT_DATA_FORMAT,
        DEFAULT_TABLE_MERGE_STRATEGY);
  }

  /**
   * Creates a new ConventionSettings with the specified data format.
   *
   * @param dataFormat the data format to use
   * @return a new ConventionSettings with the specified data format
   */
  public ConventionSettings withDataFormat(final DataFormat dataFormat) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        dataFormat,
        this.tableMergeStrategy);
  }

  /**
   * Creates a new ConventionSettings with the specified table merge strategy.
   *
   * @param tableMergeStrategy the table merge strategy to use
   * @return a new ConventionSettings with the specified merge strategy
   */
  public ConventionSettings withTableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        tableMergeStrategy);
  }
}
