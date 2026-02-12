package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.spi.ExportSupport;
import java.util.ServiceLoader;

/**
 * Executes database export after test execution.
 *
 * <p>This class delegates to {@link ExportSupport} for the actual export logic. It provides a
 * JUnit-specific facade for framework-specific customization.
 *
 * @see ExportSupport
 * @see PreparationExecutor
 * @see ExpectationVerifier
 */
public final class ExportExecutor {

  /** The export support for database export. */
  private final ExportSupport support;

  /** Creates a new export executor. */
  public ExportExecutor() {
    this.support =
        ServiceLoader.load(ExportSupport.class)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No ExportSupport implementation found. "
                            + "Ensure db-tester-core is on the classpath."));
  }

  /**
   * Creates a new export executor with the specified support.
   *
   * @param support the export support
   */
  ExportExecutor(final ExportSupport support) {
    this.support = support;
  }

  /**
   * Exports the current database state to files.
   *
   * @param context the test context containing configuration and registry
   * @param exportDataSet the ExportDataSet annotation containing export settings
   */
  public void export(final TestContext context, final ExportDataSet exportDataSet) {
    support.export(context, exportDataSet);
  }
}
