package io.github.seijikohara.dbtester.api.annotation;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that database state should be exported to files after test execution.
 *
 * <p>{@code @ExportDataSet} may be placed on an individual test method or at the test class level.
 * A method-level declaration takes precedence over any class-level definition. The annotation is
 * inherited by subclasses for consistency with {@link DataSet} and {@link ExpectedDataSet}.
 *
 * <p>Export runs in a finally-equivalent block, executing regardless of whether the test or {@link
 * ExpectedDataSet} verification succeeded or failed. When {@link #onFailureOnly()} is {@code true},
 * the export is performed only when the test execution or verification fails. Export errors are
 * logged but never mask test or verification failures.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Export all specified tables to CSV after every test
 * @Test
 * @DataSet
 * @ExportDataSet(tables = {"USERS", "ORDERS"})
 * void shouldProcessOrder() {
 *     orderService.process(orderId);
 * }
 *
 * // Export only on test failure for debugging
 * @Test
 * @DataSet
 * @ExportDataSet(format = DataFormat.JSON, tables = {"USERS"}, onFailureOnly = true)
 * void shouldHandleEdgeCase() {
 *     // ...
 * }
 * }</pre>
 *
 * @see DataSet
 * @see ExpectedDataSet
 * @see io.github.seijikohara.dbtester.api.export.DataSetExporter
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExportDataSet {

  /**
   * Specifies the export file format.
   *
   * <p>{@link DataFormat#AUTO} is not supported for export and causes a configuration error.
   *
   * @return the export format, defaulting to {@link DataFormat#CSV}
   */
  DataFormat format() default DataFormat.CSV;

  /**
   * Specifies the base directory for exported files.
   *
   * <p>Exported files are written to {@code <outputDirectory>/<className>/<methodName>/TABLE.ext}
   * to prevent file collisions between tests.
   *
   * @return the output directory path, defaulting to {@code "build/db-tester-export"}
   */
  String outputDirectory() default "build/db-tester-export";

  /**
   * Lists the database tables to export.
   *
   * <p>When empty, the framework resolves table names from the {@link DataSet} annotation on the
   * same test method or class. If no {@link DataSet} is present and no tables are specified, a
   * configuration error is raised.
   *
   * @return the table names to export; empty to resolve from {@link DataSet}
   */
  String[] tables() default {};

  /**
   * Specifies the name of the DataSource to use for export.
   *
   * <p>An empty string selects the default DataSource registered in the {@link
   * io.github.seijikohara.dbtester.api.config.DataSourceRegistry}.
   *
   * @return the DataSource name, defaulting to empty (default DataSource)
   */
  String dataSourceName() default "";

  /**
   * Controls whether export is performed only when the test fails.
   *
   * <p>When {@code true}, the export executes only if the test method or {@link ExpectedDataSet}
   * verification threw an exception. This is useful for debugging failed tests without generating
   * export files for every test run.
   *
   * @return {@code true} to export only on failure, {@code false} to export always
   */
  boolean onFailureOnly() default false;
}
