package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
import io.github.seijikohara.dbtester.internal.assertion.ExpectationVerifier;
import java.util.Collection;
import java.util.Map;
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
  public void verifyExpectation(final TableSet expectedTableSet, final DataSource dataSource) {
    expectationVerifier.verifyExpectation(expectedTableSet, dataSource);
  }

  @Override
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final Collection<String> excludeColumns) {
    expectationVerifier.verifyExpectation(expectedTableSet, dataSource, excludeColumns);
  }

  @Override
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final Collection<String> excludeColumns,
      final Map<String, ColumnStrategyMapping> columnStrategies) {
    expectationVerifier.verifyExpectation(
        expectedTableSet, dataSource, excludeColumns, columnStrategies);
  }
}
