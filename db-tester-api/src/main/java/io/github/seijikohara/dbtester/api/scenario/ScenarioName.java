package io.github.seijikohara.dbtester.api.scenario;

/**
 * Identifies a logical scenario within a shared dataset.
 *
 * <p>Scenario names are used to filter rows in shared CSV/TSV files that contain data for multiple
 * test scenarios. When a test method name (or Spock feature name) matches the scenario column
 * value, only those rows are used for the test.
 *
 * <p>This record is immutable and validated on construction. Blank scenario names are not allowed.
 *
 * @param value scenario identifier used for filtering, must not be blank
 */
public record ScenarioName(String value) implements Comparable<ScenarioName> {

  /**
   * Creates a new scenario name with the given value.
   *
   * @param value the scenario name value
   * @throws IllegalArgumentException if value is null or blank
   */
  public ScenarioName {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Scenario name must not be blank");
    }
    value = value.trim();
  }

  /**
   * Compares this scenario name with another for natural ordering.
   *
   * @param other the other scenario name to compare to
   * @return negative if this &lt; other, zero if equal, positive if this &gt; other
   */
  @Override
  public int compareTo(final ScenarioName other) {
    return this.value.compareTo(other.value);
  }
}
