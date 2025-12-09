package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import io.github.seijikohara.dbtester.internal.assertion.ExpectationVerifier;
import javax.sql.DataSource;

/**
 * Default implementation of {@link ExpectationProvider} that uses JDBC for database verification.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the implementation for
 * expectation verification.
 *
 * <p>The implementation delegates to {@link ExpectationVerifier} for the actual verification.
 */
public final class DefaultExpectationProvider implements ExpectationProvider {

  /** The expectation verifier for database verification. */
  private final ExpectationVerifier expectationVerifier;

  /** Creates a new instance with default expectation verifier. */
  public DefaultExpectationProvider() {
    this.expectationVerifier = new ExpectationVerifier();
  }

  /**
   * Creates a new instance with specified expectation verifier.
   *
   * @param expectationVerifier the expectation verifier to use
   */
  public DefaultExpectationProvider(final ExpectationVerifier expectationVerifier) {
    this.expectationVerifier = expectationVerifier;
  }

  @Override
  public void verifyExpectation(final DataSet expectedDataSet, final DataSource dataSource) {
    expectationVerifier.verifyExpectation(expectedDataSet, dataSource);
  }
}
