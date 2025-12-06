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
 */
public record ConventionSettings(
    @Nullable String baseDirectory, String expectationSuffix, String scenarioMarker) {

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

  /**
   * Creates a convention instance populated with the framework defaults.
   *
   * @return conventions using classpath-relative discovery, "/expected" suffix, and "[Scenario]"
   *     marker
   */
  public static ConventionSettings standard() {
    return new ConventionSettings(
        DEFAULT_BASE_DIRECTORY, DEFAULT_EXPECTATION_SUFFIX, DEFAULT_SCENARIO_MARKER);
  }
}
