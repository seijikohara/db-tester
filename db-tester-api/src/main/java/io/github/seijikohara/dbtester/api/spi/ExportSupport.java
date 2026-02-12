package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;

/**
 * Service Provider Interface for executing database export after test execution.
 *
 * <p>This SPI abstracts export execution, allowing test framework modules (JUnit, Spock, Kotest) to
 * depend only on the API module. The actual implementation is provided by the core module and
 * loaded via {@link java.util.ServiceLoader}.
 *
 * <p>The export phase captures the current database state and writes it to files in the configured
 * format. This is typically used for debugging failed tests or generating test fixtures.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock, Kotest) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 * @see ExportProvider
 * @see io.github.seijikohara.dbtester.api.export.DataSetExporter
 */
public interface ExportSupport {

  /**
   * Exports the current database state to files.
   *
   * <p>Resolves the tables to export (from the annotation or from the test's {@link
   * io.github.seijikohara.dbtester.api.annotation.DataSet} declaration), determines the output
   * directory, and delegates to the export infrastructure.
   *
   * @param context the test context containing configuration, registry, and test metadata
   * @param exportDataSet the ExportDataSet annotation containing export settings
   */
  void export(TestContext context, ExportDataSet exportDataSet);
}
