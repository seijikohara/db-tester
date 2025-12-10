package io.github.seijikohara.dbtester.internal.domain;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;

/**
 * Wrapper for the column name that carries scenario metadata in scenario-aware datasets.
 *
 * @param value marker column identifier
 */
public record ScenarioMarker(String value) implements StringIdentifier<ScenarioMarker> {

  /**
   * Default scenario marker column name ({@value ConventionSettings#DEFAULT_SCENARIO_MARKER}) used
   * in CSV files.
   */
  public static final ScenarioMarker DEFAULT =
      new ScenarioMarker(ConventionSettings.DEFAULT_SCENARIO_MARKER);

  /** Validates the marker identifier. */
  public ScenarioMarker {
    value = validateNonBlankString(value, "Scenario marker");
  }
}
