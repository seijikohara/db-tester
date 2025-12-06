package io.github.seijikohara.dbtester.internal.domain;

/**
 * Wrapper for the column name that carries scenario metadata in scenario-aware datasets.
 *
 * @param value marker column identifier
 */
public record ScenarioMarker(String value) implements StringIdentifier<ScenarioMarker> {

  /** Default scenario marker column name used in CSV files. */
  public static final ScenarioMarker DEFAULT = new ScenarioMarker("[Scenario]");

  /** Validates the marker identifier. */
  public ScenarioMarker {
    value = validateNonBlankString(value, "Scenario marker");
  }
}
