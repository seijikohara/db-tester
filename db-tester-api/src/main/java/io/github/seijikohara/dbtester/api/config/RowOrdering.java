package io.github.seijikohara.dbtester.api.config;

/**
 * Defines how rows should be compared during expectation verification.
 *
 * <p>This enum specifies whether row order matters when comparing expected and actual database
 * state. The default is {@link #ORDERED} for backward compatibility.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Annotation-based configuration
 * @ExpectedDataSet(rowOrdering = RowOrdering.UNORDERED)
 * void testUserQuery() {
 *   // Row order in result set does not matter
 * }
 *
 * // Programmatic configuration
 * var settings = ConventionSettings.standard()
 *     .withRowOrdering(RowOrdering.UNORDERED);
 * }</pre>
 *
 * @see ConventionSettings
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
public enum RowOrdering {

  /**
   * Compares rows in positional order (row-by-row by index).
   *
   * <p>Expected row at index N is compared with actual row at index N. This is the default behavior
   * for backward compatibility.
   *
   * <p>Use this mode when:
   *
   * <ul>
   *   <li>The query includes ORDER BY clause
   *   <li>Row order is significant for the test
   *   <li>Maximum comparison performance is required
   * </ul>
   */
  ORDERED,

  /**
   * Compares rows without considering order (set-based comparison).
   *
   * <p>Each expected row must have a matching actual row, regardless of position. Useful when the
   * database does not guarantee row ordering.
   *
   * <p>Use this mode when:
   *
   * <ul>
   *   <li>The query does not include ORDER BY clause
   *   <li>Row order is not significant for the test
   *   <li>Database may return rows in unpredictable order
   * </ul>
   *
   * <p>Note: Unordered comparison has O(n*m) complexity in the worst case and may be slower for
   * large datasets.
   */
  UNORDERED
}
