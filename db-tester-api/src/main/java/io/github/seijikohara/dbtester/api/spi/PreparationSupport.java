package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;

/**
 * Service Provider Interface for executing database preparation operations.
 *
 * <p>This SPI abstracts preparation execution, allowing test framework modules (JUnit, Spock,
 * Kotest) to depend only on the API module. The actual implementation is provided by the core
 * module and loaded via {@link java.util.ServiceLoader}.
 *
 * <p>The preparation phase loads datasets specified in annotations (e.g., {@code @DataSet}) or
 * resolved via conventions, and applies them to the database using the configured operation.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock, Kotest) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 * @see OperationProvider
 */
public interface PreparationSupport {

  /**
   * Executes database preparation for a test.
   *
   * <p>Loads datasets based on the test context and applies them to the database. The operation
   * type, transaction mode, and other settings are determined by the test context configuration.
   *
   * @param context the test context containing configuration, registry, and test metadata
   * @param dataSet the DataSet annotation containing preparation settings
   */
  void execute(TestContext context, DataSet dataSet);
}
