package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.DataSourceName;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;
import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolverRegistry;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Convention-based dataset loader that resolves dataset files using test class and method names.
 *
 * <p>Default implementation that automatically locates data files using convention-based paths. The
 * actual file format is determined by registered {@link FormatProvider} implementations (CSV and
 * TSV by default). When scenario names are not specified, the test method name is used as the
 * scenario filter.
 *
 * @see DataSetLoader
 * @see io.github.seijikohara.dbtester.api.annotation.DataSetSource
 */
public final class TestClassNameBasedDataSetLoader implements DataSetLoader {

  /** Resolver for extracting annotation values. */
  private final AnnotationResolver annotationResolver;

  /** Factory for creating dataset instances from directories. */
  private final DataSetFactory dataSetFactory;

  /** Merger for combining multiple datasets. */
  private final DataSetMerger dataSetMerger;

  /** Creates a test class name based dataset loader. */
  public TestClassNameBasedDataSetLoader() {
    this.annotationResolver = new AnnotationResolver();
    this.dataSetFactory = new DataSetFactory();
    this.dataSetMerger = new DataSetMerger();
  }

  /**
   * {@inheritDoc}
   *
   * @param context the test execution context
   * @return immutable list of preparation datasets (may be empty)
   */
  @Override
  public List<TableSet> loadPreparationDataSets(final TestContext context) {
    final var testClass = context.testClass();
    final var testMethod = context.testMethod();
    final var conventions = context.configuration().conventions();
    final var mergeStrategy = conventions.tableMergeStrategy();

    return annotationResolver
        .findDataSet(testMethod, testClass)
        .map(
            dataSet -> {
              final var dataSetSources = dataSet.sources();
              // When sources is empty, load from convention-based location with default config
              final var loadedTableSets =
                  dataSetSources.length == 0
                      ? loadConventionBasedTableSet(context, null)
                      : loadTableSets(context, List.of(dataSetSources), null);
              // Merge datasets if there are multiple
              return mergeTableSets(loadedTableSets, mergeStrategy);
            })
        .orElse(List.of());
  }

  /**
   * {@inheritDoc}
   *
   * @param context the test execution context
   * @return immutable list of expectation datasets (may be empty)
   */
  @Override
  public List<TableSet> loadExpectationDataSets(final TestContext context) {
    final var testClass = context.testClass();
    final var testMethod = context.testMethod();
    final var conventions = context.configuration().conventions();
    final var expectFileSuffix = conventions.expectationSuffix();
    final var mergeStrategy = conventions.tableMergeStrategy();

    return annotationResolver
        .findExpectedDataSet(testMethod, testClass)
        .map(
            expectedDataSet -> {
              final var dataSetSources = expectedDataSet.sources();
              // When sources is empty, load from convention-based location with default config
              final var loadedTableSets =
                  dataSetSources.length == 0
                      ? loadConventionBasedTableSet(context, expectFileSuffix)
                      : loadTableSets(context, List.of(dataSetSources), expectFileSuffix);
              // Merge datasets if there are multiple
              return mergeTableSets(loadedTableSets, mergeStrategy);
            })
        .orElse(List.of());
  }

  /**
   * {@inheritDoc}
   *
   * @param context the test execution context
   * @return immutable list of expected table sets with column comparison configuration
   */
  @Override
  public List<ExpectedTableSet> loadExpectationDataSetsWithExclusions(final TestContext context) {
    final var testClass = context.testClass();
    final var testMethod = context.testMethod();
    final var conventions = context.configuration().conventions();
    final var expectFileSuffix = conventions.expectationSuffix();
    final var mergeStrategy = conventions.tableMergeStrategy();
    final var globalExcludeColumns = conventions.globalExcludeColumns();
    final var globalColumnStrategies = conventions.globalColumnStrategies();

    return annotationResolver
        .findExpectedDataSet(testMethod, testClass)
        .map(
            expectedDataSet -> {
              final var dataSetSources = expectedDataSet.sources();
              // When sources is empty, load from convention-based location with default config
              if (dataSetSources.length == 0) {
                final var loadedTableSets = loadConventionBasedTableSet(context, expectFileSuffix);
                final var mergedTableSets = mergeTableSets(loadedTableSets, mergeStrategy);
                // Use only global settings when no DataSetSource annotations
                return mergedTableSets.stream()
                    .map(
                        tableSet ->
                            ExpectedTableSet.of(
                                tableSet, globalExcludeColumns, globalColumnStrategies))
                    .toList();
              } else {
                // Load with annotation-level settings
                return loadExpectedTableSetsWithConfigurations(
                    context,
                    List.of(dataSetSources),
                    expectFileSuffix,
                    globalExcludeColumns,
                    globalColumnStrategies);
              }
            })
        .orElse(List.of());
  }

  /**
   * Merges multiple datasets into a single dataset list.
   *
   * <p>If there are multiple datasets, they are merged according to the strategy. The result is
   * always a list containing at most one dataset.
   *
   * @param tableSets the datasets to merge
   * @param strategy the merge strategy to apply
   * @return a list containing the merged dataset, or empty list if input is empty
   */
  private List<TableSet> mergeTableSets(
      final List<TableSet> tableSets, final TableMergeStrategy strategy) {
    if (tableSets.isEmpty()) {
      return List.of();
    }
    if (tableSets.size() == 1) {
      return tableSets;
    }
    final var merged = dataSetMerger.merge(tableSets, strategy);
    return List.of(merged);
  }

  /**
   * Loads a dataset using convention-based resolution with default configuration.
   *
   * <p>This method is called when {@code @DataSet} or {@code @ExpectedDataSet} is used without
   * specifying {@code sources}. It creates a single dataset using:
   *
   * <ul>
   *   <li>Default resource location (convention-based: {@code classpath:[test-class]/})
   *   <li>Test method name as the scenario name
   *   <li>Default data source
   *   <li>Configured data format (CSV by default)
   * </ul>
   *
   * @param context the test execution context
   * @param suffix the directory suffix for dataset files, or {@code null} for no suffix
   * @return list containing a single convention-based dataset
   */
  private List<TableSet> loadConventionBasedTableSet(
      final TestContext context, final @Nullable String suffix) {
    final var testMethod = context.testMethod();
    final var testClass = context.testClass();
    final var conventions = context.configuration().conventions();

    final var directoryResolver = new DirectoryResolver(testClass);
    final var directory = directoryResolver.resolveDirectory(null, suffix);

    directoryResolver.validateDirectoryContainsSupportedFiles(directory);

    // Use scenario name resolved via SPI as scenario filter (provider-specific dataset will
    // interpret marker columns if present). Framework-specific resolvers (Spock, Kotest) can
    // provide custom resolution logic. If no resolver matches, falls back to method name.
    final var scenarioNames = List.of(ScenarioNameResolverRegistry.resolve(testMethod));
    final var scenarioMarker = new ScenarioMarker(conventions.scenarioMarker());
    final var dataFormat = conventions.dataFormat();

    // Configure default data source
    final var registry = context.registry();
    final var dataSource = registry.getDefault();
    final var tableSet =
        dataSetFactory.createTableSet(
            directory, scenarioNames, scenarioMarker, dataFormat, dataSource);

    return List.of(tableSet);
  }

  /**
   * Loads multiple datasets from annotations.
   *
   * @param context the test execution context
   * @param dataSetSourceAnnotations list of dataset source annotations to process
   * @param suffix the directory suffix for dataset files, or {@code null} for no suffix
   * @return collection of loaded datasets
   */
  private List<TableSet> loadTableSets(
      final TestContext context,
      final Collection<io.github.seijikohara.dbtester.api.annotation.DataSetSource>
          dataSetSourceAnnotations,
      final @Nullable String suffix) {
    final var processor = new DataSetProcessor(context, suffix, annotationResolver, dataSetFactory);
    return dataSetSourceAnnotations.stream().map(processor::createTableSet).toList();
  }

  /**
   * Loads expected table sets with column comparison configuration from annotations.
   *
   * <p>Each DataSetSource annotation may specify its own excludeColumns and columnStrategies, which
   * are combined with global settings. Annotation-level settings override global settings for the
   * same column.
   *
   * @param context the test execution context
   * @param dataSetSourceAnnotations list of dataset source annotations to process
   * @param suffix the directory suffix for dataset files, or {@code null} for no suffix
   * @param globalExcludeColumns column names to exclude globally
   * @param globalColumnStrategies column strategies to apply globally
   * @return list of expected table sets with comparison configuration
   */
  private List<ExpectedTableSet> loadExpectedTableSetsWithConfigurations(
      final TestContext context,
      final Collection<io.github.seijikohara.dbtester.api.annotation.DataSetSource>
          dataSetSourceAnnotations,
      final @Nullable String suffix,
      final Set<String> globalExcludeColumns,
      final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
    final var processor = new DataSetProcessor(context, suffix, annotationResolver, dataSetFactory);
    return dataSetSourceAnnotations.stream()
        .map(
            annotation -> {
              final var tableSet = processor.createTableSet(annotation);
              final var annotationExcludeColumns =
                  annotationResolver.resolveExcludeColumns(annotation);
              final var annotationColumnStrategies =
                  annotationResolver.resolveColumnStrategies(annotation);
              // Combine annotation-level and global settings
              final var combinedExcludeColumns =
                  combineExcludeColumns(annotationExcludeColumns, globalExcludeColumns);
              final var combinedColumnStrategies =
                  combineColumnStrategies(annotationColumnStrategies, globalColumnStrategies);
              return ExpectedTableSet.of(
                  tableSet, combinedExcludeColumns, combinedColumnStrategies);
            })
        .toList();
  }

  /**
   * Combines annotation-level and global exclude columns into a single set.
   *
   * @param annotationExcludeColumns exclude columns from the annotation
   * @param globalExcludeColumns global exclude columns from convention settings
   * @return combined set of exclude columns (uppercase for case-insensitive matching)
   */
  private Set<String> combineExcludeColumns(
      final Set<String> annotationExcludeColumns, final Set<String> globalExcludeColumns) {
    if (annotationExcludeColumns.isEmpty()) {
      return globalExcludeColumns;
    }
    if (globalExcludeColumns.isEmpty()) {
      return annotationExcludeColumns;
    }
    final var combined = new HashSet<>(annotationExcludeColumns);
    // Normalize global columns to uppercase for consistent case-insensitive matching
    globalExcludeColumns.stream().map(String::toUpperCase).forEach(combined::add);
    return Set.copyOf(combined);
  }

  /**
   * Combines annotation-level and global column strategies into a single map.
   *
   * <p>Annotation-level strategies override global strategies for the same column.
   *
   * @param annotationColumnStrategies column strategies from the annotation
   * @param globalColumnStrategies global column strategies from convention settings
   * @return combined map of column strategies
   */
  private Map<String, ColumnStrategyMapping> combineColumnStrategies(
      final Map<String, ColumnStrategyMapping> annotationColumnStrategies,
      final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
    if (annotationColumnStrategies.isEmpty()) {
      return globalColumnStrategies;
    }
    if (globalColumnStrategies.isEmpty()) {
      return annotationColumnStrategies;
    }
    // Annotation-level strategies override global strategies
    final var combined = new HashMap<>(globalColumnStrategies);
    combined.putAll(annotationColumnStrategies);
    return Map.copyOf(combined);
  }

  /**
   * Processor for creating datasets from annotations.
   *
   * <p>This inner class encapsulates the logic for processing a single DataSetSource annotation,
   * resolving its directory location, loading data files, and configuring the data source.
   */
  private static final class DataSetProcessor {
    /** The test execution context. */
    private final TestContext context;

    /** The test class being executed. */
    private final Class<?> testClass;

    /** The test method being executed. */
    private final Method testMethod;

    /** The directory suffix for dataset files, or {@code null} for no suffix. */
    private final @Nullable String suffix;

    /** Resolver for annotation values. */
    private final AnnotationResolver annotationResolver;

    /** Factory for creating datasets. */
    private final DataSetFactory dataSetFactory;

    /** Resolver for directory locations. */
    private final DirectoryResolver directoryResolver;

    /**
     * Creates a new dataset processor.
     *
     * @param context the test execution context
     * @param suffix the directory suffix, or {@code null} for no suffix
     * @param annotationResolver the annotation resolver
     * @param dataSetFactory the dataset factory
     */
    private DataSetProcessor(
        final TestContext context,
        final @Nullable String suffix,
        final AnnotationResolver annotationResolver,
        final DataSetFactory dataSetFactory) {
      this.context = context;
      this.testClass = context.testClass();
      this.testMethod = context.testMethod();
      this.suffix = suffix;
      this.annotationResolver = annotationResolver;
      this.dataSetFactory = dataSetFactory;
      this.directoryResolver = new DirectoryResolver(testClass);
    }

    /**
     * Creates a dataset from a DataSetSource annotation.
     *
     * @param annotation the DataSetSource annotation
     * @return the loaded dataset
     */
    private TableSet createTableSet(
        final io.github.seijikohara.dbtester.api.annotation.DataSetSource annotation) {
      final var resourceLocation = annotationResolver.extractResourceLocation(annotation);
      final var directory = resolveDirectory(resourceLocation.orElse(null));

      directoryResolver.validateDirectoryContainsSupportedFiles(directory);

      final var conventions = context.configuration().conventions();
      final var scenarioNames = annotationResolver.resolveScenarioNames(annotation, testMethod);
      final var scenarioMarker = new ScenarioMarker(conventions.scenarioMarker());
      final var dataFormat = conventions.dataFormat();

      final var dataSourceName = annotationResolver.resolveDataSourceName(annotation);
      final var dataSource = resolveDataSource(dataSourceName.orElse(null));

      return dataSetFactory.createTableSet(
          directory, scenarioNames, scenarioMarker, dataFormat, dataSource);
    }

    /**
     * Resolves the directory location for dataset files.
     *
     * @param resourceLocation the custom resource location, or null for convention-based resolution
     * @return the resolved directory path
     */
    private Path resolveDirectory(final @Nullable String resourceLocation) {
      return directoryResolver.resolveDirectory(resourceLocation, suffix);
    }

    /**
     * Resolves the data source for a dataset.
     *
     * @param dataSourceName the data source name, or null for the default data source
     * @return the resolved data source
     */
    private DataSource resolveDataSource(final @Nullable DataSourceName dataSourceName) {
      final var registry = context.registry();
      return Optional.ofNullable(dataSourceName)
          .map(DataSourceName::value)
          .map(registry::get)
          .orElseGet(registry::getDefault);
    }
  }
}
