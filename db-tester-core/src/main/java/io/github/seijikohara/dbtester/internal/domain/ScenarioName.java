package io.github.seijikohara.dbtester.internal.domain;

/**
 * Identifies a logical scenario within a shared dataset.
 *
 * @param value scenario identifier used for filtering
 */
public record ScenarioName(String value) implements StringIdentifier<ScenarioName> {

  /** Validates and normalizes the scenario identifier. */
  public ScenarioName {
    value = validateNonBlankString(value, "Scenario name");
  }
}
