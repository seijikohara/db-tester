package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
import java.util.ServiceLoader;

/**
 * Validates expectation datasets against the live database after test execution.
 *
 * <p>This class delegates to {@link ExpectationSupport} for the actual verification logic. It
 * provides a JUnit-specific facade for framework-specific customization.
 *
 * @see ExpectationSupport
 * @see PreparationExecutor
 */
public final class ExpectationVerifier {

  /** The expectation support for database verification. */
  private final ExpectationSupport support;

  /** Creates a new expectation verifier. */
  public ExpectationVerifier() {
    this.support =
        ServiceLoader.load(ExpectationSupport.class)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No ExpectationSupport implementation found. "
                            + "Ensure db-tester-core is on the classpath."));
  }

  /**
   * Creates a new expectation verifier with the specified support.
   *
   * @param support the expectation support
   */
  ExpectationVerifier(final ExpectationSupport support) {
    this.support = support;
  }

  /**
   * Verifies the database state against expected datasets.
   *
   * <p>Loads the datasets specified in the {@link ExpectedDataSet} annotation (or resolved via
   * conventions) and compares them with the actual database state. Supports retry with configurable
   * count and delay for eventual consistency scenarios.
   *
   * @param context the test context containing configuration and registry
   * @param expectedDataSet the ExpectedDataSet annotation containing row ordering and retry
   *     settings
   * @throws AssertionError if the database state does not match the expected state after retries
   */
  public void verify(final TestContext context, final ExpectedDataSet expectedDataSet) {
    support.verify(context, expectedDataSet);
  }
}
