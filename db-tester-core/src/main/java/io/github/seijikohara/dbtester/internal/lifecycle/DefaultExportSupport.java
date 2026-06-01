package io.github.seijikohara.dbtester.internal.lifecycle;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.ConfigurationException;
import io.github.seijikohara.dbtester.api.export.DataSetExporter;
import io.github.seijikohara.dbtester.api.spi.ExportSupport;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ExportSupport}.
 *
 * <p>This class provides the common export logic used by all test framework integrations (JUnit,
 * Spock, Kotest). It resolves tables from the annotation or from the test's {@link DataSet}
 * declaration, determines the output directory, and delegates to {@link DataSetExporter}.
 */
public final class DefaultExportSupport implements ExportSupport {

  /** Logger for tracking export execution. */
  private static final Logger logger = LoggerFactory.getLogger(DefaultExportSupport.class);

  /** Creates a new instance. */
  public DefaultExportSupport() {}

  @Override
  public void export(final TestContext context, final ExportDataSet exportDataSet) {
    Objects.requireNonNull(context, "context must not be null");
    Objects.requireNonNull(exportDataSet, "exportDataSet must not be null");

    final var format = exportDataSet.format();
    validateFormat(format);

    final var tables = resolveTables(context, exportDataSet);
    final var dataSource = context.registry().get(exportDataSet.dataSourceName());
    final var outputDir = resolveOutputDirectory(context, exportDataSet);

    logger.info(
        "Exporting {} tables to {} in {} format for {}.{}()",
        tables.size(),
        outputDir,
        format,
        context.testClass().getSimpleName(),
        context.testMethod().getName());

    DataSetExporter.export(dataSource, tables, outputDir, format);

    logger.info(
        "Export completed successfully for {}.{}(): {} tables",
        context.testClass().getSimpleName(),
        context.testMethod().getName(),
        tables.size());
  }

  /**
   * Validates that the export format is not AUTO.
   *
   * @param format the data format
   * @throws ConfigurationException if format is AUTO
   */
  private void validateFormat(final DataFormat format) {
    if (format == DataFormat.AUTO) {
      throw new ConfigurationException(
          "DataFormat.AUTO cannot be used for export."
              + " Specify a concrete format: CSV, TSV, JSON, or YAML.");
    }
  }

  /**
   * Resolves the table names to export.
   *
   * <p>If the annotation specifies tables, those are used directly. Otherwise, table names are
   * resolved from the {@link DataSet} annotation on the test method or class via the configuration
   * loader.
   *
   * @param context the test context
   * @param exportDataSet the export annotation
   * @return list of table names to export
   * @throws ConfigurationException if no tables can be resolved
   */
  private List<String> resolveTables(final TestContext context, final ExportDataSet exportDataSet) {
    final var annotationTables = exportDataSet.tables();
    if (annotationTables.length > 0) {
      return List.of(annotationTables);
    }

    // Try to resolve tables from @DataSet via loader
    final var dataSet = findDataSetAnnotation(context);
    if (dataSet == null) {
      throw new ConfigurationException(
          String.format(
              "@ExportDataSet on %s.%s() specifies no tables and no @DataSet is present."
                  + " Either specify tables in @ExportDataSet or add a @DataSet annotation.",
              context.testClass().getSimpleName(), context.testMethod().getName()));
    }

    final var tableSets = context.configuration().loader().loadPreparationDataSets(context);
    final var tableNames =
        tableSets.stream()
            .map(TableSet::tables)
            .flatMap(List::stream)
            .map(Table::name)
            .map(TableName::value)
            .distinct()
            .collect(Collectors.toUnmodifiableList());

    if (tableNames.isEmpty()) {
      throw new ConfigurationException(
          String.format(
              "@ExportDataSet on %s.%s() could not resolve any tables from @DataSet.",
              context.testClass().getSimpleName(), context.testMethod().getName()));
    }

    logger.debug("Resolved {} tables from @DataSet: {}", tableNames.size(), tableNames);
    return tableNames;
  }

  /**
   * Finds the {@link DataSet} annotation on the test method or class.
   *
   * @param context the test context
   * @return the DataSet annotation, or null if not present
   */
  private @Nullable DataSet findDataSetAnnotation(final TestContext context) {
    final var method = context.testMethod();
    final var methodAnnotation = method.getAnnotation(DataSet.class);
    if (methodAnnotation != null) {
      return methodAnnotation;
    }
    return context.testClass().getAnnotation(DataSet.class);
  }

  /**
   * Resolves the output directory path.
   *
   * <p>The path follows the pattern {@code <outputDirectory>/<className>/<methodName>/} to prevent
   * file collisions between test methods.
   *
   * @param context the test context
   * @param exportDataSet the export annotation
   * @return the resolved output directory path
   */
  private Path resolveOutputDirectory(
      final TestContext context, final ExportDataSet exportDataSet) {
    return Path.of(exportDataSet.outputDirectory())
        .resolve(context.testClass().getSimpleName())
        .resolve(context.testMethod().getName());
  }
}
