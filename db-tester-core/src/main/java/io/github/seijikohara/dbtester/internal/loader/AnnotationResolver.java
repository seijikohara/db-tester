package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.domain.DataSourceName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolverRegistry;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves annotation values for database test configurations.
 *
 * <p>This class handles the extraction and resolution of annotation values from {@link DataSet} and
 * {@link ExpectedDataSet} annotations, including class-level annotation inheritance. It processes
 * annotations to determine resource locations, scenario names, data source names, and database
 * operations.
 *
 * <p>The resolver searches for annotations in the following order: method-level annotation (if
 * present), class-level annotation on test class, and class-level annotation on parent classes
 * (traversing up the hierarchy).
 *
 * <p>When {@link DataSetSource#scenarioNames()} is empty (not specified or contains only empty
 * strings), the test method name is automatically used as the scenario name. This enables
 * convention-based testing where each test method automatically uses its own name as the scenario
 * filter.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see DataSetSource
 * @see DataSet
 * @see ExpectedDataSet
 */
public final class AnnotationResolver {

  /** Creates a new annotation resolver. */
  public AnnotationResolver() {}

  /**
   * Finds a {@link DataSet} annotation on a method or class hierarchy.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the DataSet annotation if found
   */
  Optional<DataSet> findDataSet(final Method testMethod, final Class<?> testClass) {
    return findAnnotation(DataSet.class, testMethod, testClass);
  }

  /**
   * Finds an {@link ExpectedDataSet} annotation on a method or class hierarchy.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the ExpectedDataSet annotation if found
   */
  Optional<ExpectedDataSet> findExpectedDataSet(final Method testMethod, final Class<?> testClass) {
    return findAnnotation(ExpectedDataSet.class, testMethod, testClass);
  }

  /**
   * Resolves scenario names from a {@link DataSetSource} annotation.
   *
   * <p>This method normalizes scenario names by trimming whitespace and filtering empty strings. If
   * the annotation provides no scenario names (or only empty strings), the test method name is used
   * as the default scenario.
   *
   * @param annotation the dataset source annotation
   * @param testMethod the test method
   * @return list of scenario names, never empty
   */
  List<ScenarioName> resolveScenarioNames(final DataSetSource annotation, final Method testMethod) {
    final var scenarioNames =
        Stream.of(annotation.scenarioNames())
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty))
            .map(ScenarioName::new)
            .toList();

    return scenarioNames.isEmpty()
        ? List.of(ScenarioNameResolverRegistry.resolve(testMethod))
        : scenarioNames;
  }

  /**
   * Extracts the resource location string from a {@link DataSetSource} annotation.
   *
   * <p>Returns the resource location string if specified in the annotation, or empty if not
   * specified, indicating that convention-based resolution should be used.
   *
   * @param annotation the dataset source annotation
   * @return Optional containing the resource location string, or empty for convention-based
   *     resolution
   */
  Optional<String> extractResourceLocation(final DataSetSource annotation) {
    return Optional.of(annotation.resourceLocation()).filter(Predicate.not(String::isEmpty));
  }

  /**
   * Resolves the data source name from a {@link DataSetSource} annotation.
   *
   * <p>Returns the data source name if specified in the annotation, or empty if not specified,
   * indicating that the default data source should be used.
   *
   * @param annotation the dataset source annotation
   * @return Optional containing the data source name, or empty for the default data source
   */
  Optional<DataSourceName> resolveDataSourceName(final DataSetSource annotation) {
    return Optional.of(annotation.dataSourceName())
        .filter(Predicate.not(String::isEmpty))
        .map(DataSourceName::new);
  }

  /**
   * Resolves exclude columns from a {@link DataSetSource} annotation.
   *
   * <p>This method extracts column names to exclude from expectation verification. Column names are
   * normalized by trimming whitespace and filtering empty strings. The returned set uses
   * case-insensitive matching by converting column names to uppercase.
   *
   * @param annotation the dataset source annotation
   * @return set of column names to exclude (uppercase for case-insensitive matching)
   */
  Set<String> resolveExcludeColumns(final DataSetSource annotation) {
    return Stream.of(annotation.excludeColumns())
        .map(String::trim)
        .filter(Predicate.not(String::isEmpty))
        .map(String::toUpperCase)
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Finds an annotation on a method or class hierarchy.
   *
   * <p>Search order:
   *
   * <ol>
   *   <li>Method-level annotation (if present)
   *   <li>Class-level annotation on test class
   *   <li>Class-level annotation on parent classes (traversing up the hierarchy)
   * </ol>
   *
   * @param <T> the annotation type
   * @param annotationClass the annotation class to search for
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the annotation if found, empty otherwise
   */
  private <T extends Annotation> Optional<T> findAnnotation(
      final Class<T> annotationClass, final Method testMethod, final Class<?> testClass) {

    // First, check method-level annotation
    return Optional.ofNullable(testMethod.getAnnotation(annotationClass))
        .or(() -> findClassAnnotation(annotationClass, testClass));
  }

  /**
   * Searches for an annotation in the class hierarchy.
   *
   * <p>Recursively searches up the class hierarchy until the annotation is found or the top of the
   * hierarchy is reached.
   *
   * @param <T> the annotation type
   * @param annotationClass the annotation class to search for
   * @param testClass the starting class for the search
   * @return Optional containing the annotation if found in the hierarchy
   */
  private <T extends Annotation> Optional<T> findClassAnnotation(
      final Class<T> annotationClass, final Class<?> testClass) {
    return Optional.ofNullable(testClass.getAnnotation(annotationClass))
        .or(
            () ->
                Optional.ofNullable(testClass.getSuperclass())
                    .flatMap(parent -> findClassAnnotation(annotationClass, parent)));
  }
}
