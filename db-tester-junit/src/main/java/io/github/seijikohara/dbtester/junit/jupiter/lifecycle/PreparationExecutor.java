package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.spi.PreparationSupport;
import java.util.ServiceLoader;

/**
 * Executes the preparation phase of database testing.
 *
 * <p>This class delegates to {@link PreparationSupport} for the actual preparation logic. It
 * provides a JUnit-specific facade for framework-specific customization.
 *
 * @see PreparationSupport
 */
public final class PreparationExecutor {

  /** The preparation support for database operations. */
  private final PreparationSupport support;

  /** Creates a new preparation executor. */
  public PreparationExecutor() {
    this.support =
        ServiceLoader.load(PreparationSupport.class)
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No PreparationSupport implementation found. "
                            + "Ensure db-tester-core is on the classpath."));
  }

  /**
   * Creates a new preparation executor with the specified support.
   *
   * @param support the preparation support
   */
  PreparationExecutor(final PreparationSupport support) {
    this.support = support;
  }

  /**
   * Executes the preparation phase.
   *
   * <p>Loads the datasets specified in the {@link DataSet} annotation (or resolved via conventions)
   * and applies them to the database using the configured operation.
   *
   * @param context the test context containing configuration and registry
   * @param dataSet the DataSet annotation specifying the operation to perform
   */
  public void execute(final TestContext context, final DataSet dataSet) {
    support.execute(context, dataSet);
  }
}
