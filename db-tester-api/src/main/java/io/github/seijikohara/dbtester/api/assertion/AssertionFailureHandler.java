package io.github.seijikohara.dbtester.api.assertion;

import org.jspecify.annotations.Nullable;

/**
 * Strategy interface for reacting to assertion mismatches.
 *
 * <p>Implementations can translate individual failures into domain-specific actions&mdash;for
 * example raising custom exceptions, logging diagnostics, or aggregating differences for later
 * inspection. The framework converts this handler into the bridge-specific counterpart while
 * preserving these semantics.
 *
 * @see DatabaseAssertion
 * @see io.github.seijikohara.dbtester.api.annotation.Expectation
 */
@FunctionalInterface
public interface AssertionFailureHandler {

  /**
   * Handles a comparison failure between expected and actual values.
   *
   * <p>This method is invoked for each mismatch detected during database comparison.
   * Implementations can throw an exception immediately (fail-fast strategy), accumulate failures
   * for batch reporting, or perform custom actions such as logging or metrics collection.
   *
   * @param message a descriptive failure message including context such as table name, row number,
   *     and column name
   * @param expected the expected value; may be {@code null}
   * @param actual the actual value found in the database; may be {@code null}
   */
  void handleFailure(
      final String message, final @Nullable Object expected, final @Nullable Object actual);
}
