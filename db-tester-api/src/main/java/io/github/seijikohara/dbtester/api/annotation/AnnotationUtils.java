package io.github.seijikohara.dbtester.api.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Provides meta-annotation traversal for database test annotations.
 *
 * <p>This utility resolves annotations that are declared directly on a method or class, or
 * transitively via meta-annotations. This enables composed annotations where users define custom
 * annotations that are themselves annotated with {@link DataSet} or {@link ExpectedDataSet}.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * // Define a composed annotation
 * @Target({ElementType.METHOD, ElementType.TYPE})
 * @Retention(RetentionPolicy.RUNTIME)
 * @ExpectedDataSet(sources = @DataSetSource(
 *     excludeColumns = {"CREATED_AT", "UPDATED_AT"}
 * ))
 * public @interface VerifyIgnoringAuditColumns { }
 *
 * // Use the composed annotation
 * @Test
 * @DataSet
 * @VerifyIgnoringAuditColumns
 * void testUserCreation() { }
 * }</pre>
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see DataSet
 * @see ExpectedDataSet
 */
public final class AnnotationUtils {

  /** Prevents instantiation. */
  private AnnotationUtils() {}

  /**
   * Finds an annotation on a method or class hierarchy, including meta-annotations.
   *
   * <p>Search order:
   *
   * <ol>
   *   <li>Direct annotation on the method
   *   <li>Meta-annotation on the method's annotations
   *   <li>Direct annotation on the test class
   *   <li>Meta-annotation on the test class's annotations
   *   <li>Annotations on parent classes (traversing up the hierarchy)
   * </ol>
   *
   * @param <T> the annotation type
   * @param annotationType the annotation class to search for
   * @param testMethod the test method
   * @param testClass the test class
   * @return Optional containing the annotation if found
   */
  public static <T extends Annotation> Optional<T> findAnnotation(
      final Class<T> annotationType, final Method testMethod, final Class<?> testClass) {
    return Optional.ofNullable(findOnElement(annotationType, testMethod))
        .or(() -> findOnClassHierarchy(annotationType, testClass));
  }

  /**
   * Finds an annotation on an element, including meta-annotations.
   *
   * <p>First checks for a direct annotation, then traverses annotations on the element looking for
   * the target annotation type used as a meta-annotation. Cycle detection prevents infinite
   * recursion.
   *
   * @param <T> the annotation type
   * @param annotationType the annotation class to search for
   * @param element the annotated element to search
   * @return the annotation if found, or null
   */
  public static <T extends Annotation> @Nullable T findOnElement(
      final Class<T> annotationType, final AnnotatedElement element) {
    return findOnElement(annotationType, element, new HashSet<>());
  }

  /**
   * Searches for an annotation in the class hierarchy, including meta-annotations.
   *
   * @param <T> the annotation type
   * @param annotationType the annotation class to search for
   * @param testClass the starting class for the search
   * @return Optional containing the annotation if found
   */
  private static <T extends Annotation> Optional<T> findOnClassHierarchy(
      final Class<T> annotationType, final Class<?> testClass) {
    return Optional.ofNullable(findOnElement(annotationType, testClass))
        .or(
            () ->
                Optional.ofNullable(testClass.getSuperclass())
                    .flatMap(parent -> findOnClassHierarchy(annotationType, parent)));
  }

  /**
   * Recursively searches for an annotation on an element and its meta-annotations.
   *
   * @param <T> the annotation type
   * @param annotationType the annotation class to search for
   * @param element the annotated element to search
   * @param visited set of visited annotation types for cycle detection
   * @return the annotation if found, or null
   */
  private static <T extends Annotation> @Nullable T findOnElement(
      final Class<T> annotationType,
      final AnnotatedElement element,
      final Set<Class<? extends Annotation>> visited) {
    // Direct annotation
    final var direct = element.getAnnotation(annotationType);
    if (direct != null) {
      return direct;
    }

    // Meta-annotation traversal
    for (final var annotation : element.getAnnotations()) {
      final var annotationTypeClass = annotation.annotationType();
      if (isJdkAnnotation(annotationTypeClass)) {
        continue;
      }
      if (!visited.add(annotationTypeClass)) {
        continue;
      }
      final var meta = findOnElement(annotationType, annotationTypeClass, visited);
      if (meta != null) {
        return meta;
      }
    }
    return null;
  }

  /**
   * Checks whether an annotation type is a JDK built-in annotation.
   *
   * <p>JDK annotations (such as {@code @Retention}, {@code @Target}, {@code @Inherited}) are
   * skipped during meta-annotation traversal.
   *
   * @param annotationType the annotation type to check
   * @return true if the annotation is a JDK built-in annotation
   */
  private static boolean isJdkAnnotation(final Class<? extends Annotation> annotationType) {
    final var name = annotationType.getName();
    return name.startsWith("java.lang.annotation.") || name.startsWith("jdk.");
  }
}
