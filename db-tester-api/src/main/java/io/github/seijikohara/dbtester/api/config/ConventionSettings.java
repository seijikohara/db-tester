package io.github.seijikohara.dbtester.api.config;

import java.util.Set;
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
 * @param loadOrderFileName the file name used to specify table loading order in dataset directories
 * @param globalExcludeColumns column names to exclude from all expectation verifications globally
 */
public record ConventionSettings(
    @Nullable String baseDirectory,
    String expectationSuffix,
    String scenarioMarker,
    DataFormat dataFormat,
    TableMergeStrategy tableMergeStrategy,
    String loadOrderFileName,
    Set<String> globalExcludeColumns) {

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
  public static final String DEFAULT_EXPECTATION_SUFFIX = "/expected";

  /**
   * Default column name that identifies scenario markers in scenario-aware dataset formats.
   *
   * <p>Rows containing this column are filtered based on scenario names specified in test
   * annotations or derived from test method names.
   */
  public static final String DEFAULT_SCENARIO_MARKER = "[Scenario]";

  /** Default file format for dataset files. */
  private static final DataFormat DEFAULT_DATA_FORMAT = DataFormat.CSV;

  /** Default strategy for merging tables from multiple datasets. */
  private static final TableMergeStrategy DEFAULT_TABLE_MERGE_STRATEGY =
      TableMergeStrategy.UNION_ALL;

  /**
   * Default file name for specifying table loading order in dataset directories.
   *
   * <p>This file contains one table name per line, specifying the order in which tables should be
   * loaded during database operations.
   */
  public static final String DEFAULT_LOAD_ORDER_FILE_NAME = "load-order.txt";

  /** Default global exclude columns (empty set). */
  private static final Set<String> DEFAULT_GLOBAL_EXCLUDE_COLUMNS = Set.of();

  /**
   * Creates a convention instance populated with the framework defaults.
   *
   * @return conventions using classpath-relative discovery, {@value #DEFAULT_EXPECTATION_SUFFIX}
   *     suffix, {@value #DEFAULT_SCENARIO_MARKER} marker, CSV format, UNION_ALL merge strategy,
   *     {@value #DEFAULT_LOAD_ORDER_FILE_NAME} load order file, and no global exclude columns
   */
  public static ConventionSettings standard() {
    return new ConventionSettings(
        DEFAULT_BASE_DIRECTORY,
        DEFAULT_EXPECTATION_SUFFIX,
        DEFAULT_SCENARIO_MARKER,
        DEFAULT_DATA_FORMAT,
        DEFAULT_TABLE_MERGE_STRATEGY,
        DEFAULT_LOAD_ORDER_FILE_NAME,
        DEFAULT_GLOBAL_EXCLUDE_COLUMNS);
  }

  /**
   * Creates a new ConventionSettings with the specified base directory.
   *
   * <p>The base directory can be an absolute path or a path relative to the classpath. A {@code
   * null} value instructs the loader to resolve dataset locations relative to the test class
   * package on the classpath.
   *
   * @param baseDirectory the base directory path, or null for convention-based resolution
   * @return a new ConventionSettings with the specified base directory
   */
  public ConventionSettings withBaseDirectory(final @Nullable String baseDirectory) {
    return new ConventionSettings(
        baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns);
  }

  /**
   * Creates a new ConventionSettings with the specified expectation suffix.
   *
   * <p>The expectation suffix is appended to the preparation directory when resolving expectation
   * datasets. This suffix is typically a subdirectory name that separates expected outcome data
   * from preparation data.
   *
   * @param expectationSuffix the suffix appended to base path for expectation datasets
   * @return a new ConventionSettings with the specified expectation suffix
   */
  public ConventionSettings withExpectationSuffix(final String expectationSuffix) {
    return new ConventionSettings(
        this.baseDirectory,
        expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns);
  }

  /**
   * Creates a new ConventionSettings with the specified scenario marker.
   *
   * <p>The scenario marker is the column name that identifies scenario markers in scenario-aware
   * dataset formats. Rows containing this column are filtered based on scenario names specified in
   * test annotations or derived from test method names.
   *
   * @param scenarioMarker the column name used to identify scenario rows
   * @return a new ConventionSettings with the specified scenario marker
   */
  public ConventionSettings withScenarioMarker(final String scenarioMarker) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns);
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
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns);
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
        tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns);
  }

  /**
   * Creates a new ConventionSettings with the specified load order file name.
   *
   * @param loadOrderFileName the load order file name to use
   * @return a new ConventionSettings with the specified load order file name
   */
  public ConventionSettings withLoadOrderFileName(final String loadOrderFileName) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        loadOrderFileName,
        this.globalExcludeColumns);
  }

  /**
   * Creates a new ConventionSettings with the specified global exclude columns.
   *
   * <p>Columns listed here are excluded from all expectation verifications. This is useful for
   * excluding auto-generated columns (timestamps, version numbers) across all tests without
   * repeating the exclusion in each annotation.
   *
   * <p>Column name matching is case-insensitive.
   *
   * @param globalExcludeColumns the column names to exclude globally
   * @return a new ConventionSettings with the specified global exclude columns
   */
  public ConventionSettings withGlobalExcludeColumns(final Set<String> globalExcludeColumns) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        globalExcludeColumns);
  }
}
