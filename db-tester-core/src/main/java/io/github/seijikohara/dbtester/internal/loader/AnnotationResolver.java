package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.internal.domain.DataSourceName;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolverRegistry;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Resolves annotation values for database test configurations.
 *
 * <p>This class handles the extraction and resolution of annotation values from {@link Preparation}
 * and {@link Expectation} annotations, including class-level annotation inheritance. It processes
 * annotations to determine resource locations, scenario names, data source names, and database
 * operations.
 *
 * <p>The resolver searches for annotations in the following order: method-level annotation (if
 * present), class-level annotation on test class, and class-level annotation on parent classes
 * (traversing up the hierarchy).
 *
 * <p>When {@link DataSet#scenarioNames()} is empty (not specified or contains only empty strings),
 * the test method name is automatically used as the scenario name. This enables convention-based
 * testing where each test method automatically uses its own name as the scenario filter.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see DataSet
 * @see Preparation
 * @see Expectation
 */
public final class AnnotationResolver {

  /** Creates a new annotation resolver. */
  public AnnotationResolver() {}

  /**
   * Finds a {@link Preparation} annotation on a method or class hierarchy.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the Preparation annotation if found
   */
  Optional<Preparation> findPreparation(final Method testMethod, final Class<?> testClass) {
    return findAnnotation(Preparation.class, testMethod, testClass);
  }

  /**
   * Finds an {@link Expectation} annotation on a method or class hierarchy.
   *
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the Expectation annotation if found
   */
  Optional<Expectation> findExpectation(final Method testMethod, final Class<?> testClass) {
    return findAnnotation(Expectation.class, testMethod, testClass);
  }

  /**
   * Resolves scenario names from a {@link DataSet} annotation.
   *
   * <p>This method normalizes scenario names by trimming whitespace and filtering empty strings. If
   * the annotation provides no scenario names (or only empty strings), the test method name is used
   * as the default scenario.
   *
   * @param annotation the dataset annotation
   * @param testMethod the test method
   * @return list of scenario names, never empty
   */
  List<ScenarioName> resolveScenarioNames(final DataSet annotation, final Method testMethod) {
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
   * Extracts the resource location string from a {@link DataSet} annotation.
   *
   * <p>Returns the resource location string if specified in the annotation, or empty if not
   * specified, indicating that convention-based resolution should be used.
   *
   * @param annotation the dataset annotation
   * @return Optional containing the resource location string, or empty for convention-based
   *     resolution
   */
  Optional<String> extractResourceLocation(final DataSet annotation) {
    return Optional.of(annotation.resourceLocation()).filter(Predicate.not(String::isEmpty));
  }

  /**
   * Resolves the data source name from a {@link DataSet} annotation.
   *
   * <p>Returns the data source name if specified in the annotation, or empty if not specified,
   * indicating that the default data source should be used.
   *
   * @param annotation the dataset annotation
   * @return Optional containing the data source name, or empty for the default data source
   */
  Optional<DataSourceName> resolveDataSourceName(final DataSet annotation) {
    return Optional.of(annotation.dataSourceName())
        .filter(Predicate.not(String::isEmpty))
        .map(DataSourceName::new);
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
