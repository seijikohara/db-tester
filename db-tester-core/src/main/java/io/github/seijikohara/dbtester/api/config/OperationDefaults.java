package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.operation.Operation;

/**
 * Encapsulates the default {@link Operation} values applied to the preparation and expectation
 * phases.
 *
 * @param preparation default operation executed before a test runs
 * @param expectation default operation executed after a test finishes
 */
public record OperationDefaults(Operation preparation, Operation expectation) {

  /**
   * Returns an instance initialised with {@link Operation#CLEAN_INSERT} and {@link Operation#NONE}.
   *
   * @return defaults using {@code CLEAN_INSERT} for preparation and {@code NONE} for verification
   */
  public static OperationDefaults standard() {
    return new OperationDefaults(Operation.CLEAN_INSERT, Operation.NONE);
  }
}
