package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.exception.ValidationException;

/**
 * Service Provider Interface for executing database expectation verification.
 *
 * <p>This SPI abstracts expectation verification, allowing test framework modules (JUnit, Spock,
 * Kotest) to depend only on the API module. The actual implementation is provided by the core
 * module and loaded via {@link java.util.ServiceLoader}.
 *
 * <p>The verification phase loads expected datasets specified in annotations (e.g.,
 * {@code @ExpectedDataSet}) or resolved via conventions, and compares them with the actual database
 * state. The comparison supports retry with configurable count and delay for eventual consistency
 * scenarios.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock, Kotest) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 * @see ExpectationProvider
 */
public interface ExpectationSupport {

  /**
   * Verifies that the database state matches the expected datasets.
   *
   * <p>Loads expected datasets based on the test context and compares them with the actual database
   * state. Supports retry with configurable count and delay for eventual consistency scenarios.
   *
   * @param context the test context containing configuration, registry, and test metadata
   * @param expectedDataSet the ExpectedDataSet annotation containing verification settings
   * @throws ValidationException if verification fails after all retries
   */
  void verify(TestContext context, ExpectedDataSet expectedDataSet);
}
