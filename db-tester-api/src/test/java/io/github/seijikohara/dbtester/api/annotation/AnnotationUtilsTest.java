package io.github.seijikohara.dbtester.api.annotation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnnotationUtils}. */
@DisplayName("AnnotationUtils")
class AnnotationUtilsTest {

  /** Tests for the AnnotationUtils class. */
  AnnotationUtilsTest() {}

  /** Tests for the findAnnotation(Class, Method, Class) method. */
  @Nested
  @DisplayName("findAnnotation(Class, Method, Class) method")
  class FindAnnotationMethod {

    /** Tests for the findAnnotation method. */
    FindAnnotationMethod() {}

    /**
     * Verifies that findAnnotation returns annotation from method when present.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when method has direct annotation")
    void shouldReturnAnnotation_whenMethodHasDirectAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithMethodAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("annotatedMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet annotation on method");
    }

    /**
     * Verifies that findAnnotation returns annotation from class when method has none.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when class has annotation but method does not")
    void shouldReturnAnnotation_whenClassHasAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithClassAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("unannotatedMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet annotation on class");
    }

    /**
     * Verifies that findAnnotation returns annotation from parent class.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when parent class has annotation")
    void shouldReturnAnnotation_whenParentClassHasAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ChildClassInheritsAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("childMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet annotation on parent class");
    }

    /**
     * Verifies that findAnnotation prioritizes method over class.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should prioritize method annotation over class annotation")
    void shouldPrioritizeMethodAnnotation_overClassAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithBothAnnotations.class;
      final var testMethod = testClass.getDeclaredMethod("annotatedMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find annotation");
    }

    /**
     * Verifies that findAnnotation returns empty when no annotation found.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when no annotation found anywhere")
    void shouldReturnEmpty_whenNoAnnotationFound() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithoutAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("plainMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isEmpty(), "should return empty when no annotation found");
    }

    /**
     * Verifies that findAnnotation finds meta-annotation on method.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should find meta-annotation on method")
    void shouldFindMetaAnnotation_onMethod() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithMetaAnnotatedMethod.class;
      final var testMethod = testClass.getDeclaredMethod("metaAnnotatedMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet via meta-annotation on method");
    }

    /**
     * Verifies that findAnnotation finds meta-annotation on class.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should find meta-annotation on class")
    void shouldFindMetaAnnotation_onClass() throws NoSuchMethodException {
      // Given
      final var testClass = MetaAnnotatedClass.class;
      final var testMethod = testClass.getDeclaredMethod("plainMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet via meta-annotation on class");
    }

    /**
     * Verifies that findAnnotation finds deeply nested meta-annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should find deeply nested meta-annotation")
    void shouldFindDeeplyNestedMetaAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithDeepMetaAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("deepMethod");

      // When
      final Optional<DataSet> result =
          AnnotationUtils.findAnnotation(DataSet.class, testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find DataSet via deeply nested meta-annotation");
    }

    /**
     * Verifies that findAnnotation finds ExpectedDataSet via meta-annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should find ExpectedDataSet via meta-annotation")
    void shouldFindExpectedDataSet_viaMetaAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = ClassWithMetaExpectedDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("verifyMethod");

      // When
      final Optional<ExpectedDataSet> result =
          AnnotationUtils.findAnnotation(ExpectedDataSet.class, testMethod, testClass);

      // Then
      assertAll(
          "should find ExpectedDataSet with configured attributes",
          () -> assertTrue(result.isPresent(), "should find ExpectedDataSet via meta-annotation"),
          () -> {
            final var annotation = result.orElseThrow();
            assertEquals(1, annotation.sources().length, "should have one source");
            final var source = annotation.sources()[0];
            assertEquals(2, source.excludeColumns().length, "should have two excluded columns");
          });
    }
  }

  /** Tests for the findOnElement(Class, AnnotatedElement) method. */
  @Nested
  @DisplayName("findOnElement(Class, AnnotatedElement) method")
  class FindOnElementMethod {

    /** Tests for the findOnElement method. */
    FindOnElementMethod() {}

    /**
     * Verifies that findOnElement returns direct annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return direct annotation when present")
    void shouldReturnDirectAnnotation_whenPresent() throws NoSuchMethodException {
      // Given
      final var method = ClassWithMethodAnnotation.class.getDeclaredMethod("annotatedMethod");

      // When
      final var result = AnnotationUtils.findOnElement(DataSet.class, method);

      // Then
      assertNotNull(result, "should find direct DataSet annotation");
    }

    /**
     * Verifies that findOnElement returns meta-annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return meta-annotation when present")
    void shouldReturnMetaAnnotation_whenPresent() throws NoSuchMethodException {
      // Given
      final var method =
          ClassWithMetaAnnotatedMethod.class.getDeclaredMethod("metaAnnotatedMethod");

      // When
      final var result = AnnotationUtils.findOnElement(DataSet.class, method);

      // Then
      assertNotNull(result, "should find DataSet via meta-annotation");
    }

    /**
     * Verifies that findOnElement returns null when not present.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return null when annotation not found")
    void shouldReturnNull_whenAnnotationNotFound() throws NoSuchMethodException {
      // Given
      final var method = ClassWithoutAnnotation.class.getDeclaredMethod("plainMethod");

      // When
      final var result = AnnotationUtils.findOnElement(DataSet.class, method);

      // Then
      assertNull(result, "should return null when annotation not found");
    }

    /** Verifies that findOnElement handles annotation on class. */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when class has direct annotation")
    void shouldReturnAnnotation_whenClassHasDirectAnnotation() {
      // When
      final var result =
          AnnotationUtils.findOnElement(DataSet.class, ClassWithClassAnnotation.class);

      // Then
      assertNotNull(result, "should find direct DataSet annotation on class");
    }

    /** Verifies that findOnElement finds meta-annotation on class. */
    @Test
    @Tag("normal")
    @DisplayName("should return meta-annotation when class has meta-annotation")
    void shouldReturnMetaAnnotation_whenClassHasMetaAnnotation() {
      // When
      final var result = AnnotationUtils.findOnElement(DataSet.class, MetaAnnotatedClass.class);

      // Then
      assertNotNull(result, "should find DataSet via meta-annotation on class");
    }

    /** Verifies that findOnElement handles cycle detection in meta-annotations. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle cyclic meta-annotations without infinite loop")
    void shouldHandleCyclicMetaAnnotations_withoutInfiniteLoop() {
      // When
      final var result =
          AnnotationUtils.findOnElement(DataSet.class, ClassWithCyclicAnnotation.class);

      // Then
      assertNull(result, "should return null without infinite loop for cyclic annotations");
    }
  }

  // --- Composed annotations for testing ---

  /** Composed annotation that includes {@link DataSet}. */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @DataSet
  @interface ComposedDataSet {}

  /** Deeply nested composed annotation wrapping {@link ComposedDataSet}. */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ComposedDataSet
  @interface DeeplyComposedDataSet {}

  /** Composed annotation that includes {@link ExpectedDataSet} with configuration. */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @ExpectedDataSet(sources = @DataSetSource(excludeColumns = {"CREATED_AT", "UPDATED_AT"}))
  @interface VerifyIgnoringAuditColumns {}

  /** First annotation in a cycle. */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @CyclicAnnotationB
  @interface CyclicAnnotationA {}

  /** Second annotation in a cycle. */
  @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @CyclicAnnotationA
  @interface CyclicAnnotationB {}

  // --- Test stub classes ---

  /** Test class with method-level DataSet annotation. */
  static class ClassWithMethodAnnotation {
    /** Test constructor. */
    ClassWithMethodAnnotation() {}

    /** Annotated method. */
    @DataSet
    void annotatedMethod() {}
  }

  /** Test class with class-level DataSet annotation. */
  @DataSet
  static class ClassWithClassAnnotation {
    /** Test constructor. */
    ClassWithClassAnnotation() {}

    /** Unannotated method. */
    void unannotatedMethod() {}
  }

  /** Parent class with DataSet annotation. */
  @DataSet
  static class ParentClassWithAnnotation {
    /** Test constructor. */
    ParentClassWithAnnotation() {}
  }

  /** Child class inheriting annotation from parent. */
  static class ChildClassInheritsAnnotation extends ParentClassWithAnnotation {
    /** Test constructor. */
    ChildClassInheritsAnnotation() {}

    /** Child method. */
    void childMethod() {}
  }

  /** Test class with both method and class annotations. */
  @DataSet
  static class ClassWithBothAnnotations {
    /** Test constructor. */
    ClassWithBothAnnotations() {}

    /** Method with its own annotation. */
    @DataSet
    void annotatedMethod() {}
  }

  /** Test class without any annotation. */
  static class ClassWithoutAnnotation {
    /** Test constructor. */
    ClassWithoutAnnotation() {}

    /** Plain method. */
    void plainMethod() {}
  }

  /** Test class with meta-annotated method. */
  static class ClassWithMetaAnnotatedMethod {
    /** Test constructor. */
    ClassWithMetaAnnotatedMethod() {}

    /** Method with composed annotation. */
    @ComposedDataSet
    void metaAnnotatedMethod() {}
  }

  /** Test class with meta-annotation at class level. */
  @ComposedDataSet
  static class MetaAnnotatedClass {
    /** Test constructor. */
    MetaAnnotatedClass() {}

    /** Plain method. */
    void plainMethod() {}
  }

  /** Test class with deeply nested meta-annotation. */
  static class ClassWithDeepMetaAnnotation {
    /** Test constructor. */
    ClassWithDeepMetaAnnotation() {}

    /** Method with deeply composed annotation. */
    @DeeplyComposedDataSet
    void deepMethod() {}
  }

  /** Test class with composed ExpectedDataSet. */
  static class ClassWithMetaExpectedDataSet {
    /** Test constructor. */
    ClassWithMetaExpectedDataSet() {}

    /** Method with composed verification annotation. */
    @VerifyIgnoringAuditColumns
    void verifyMethod() {}
  }

  /** Test class with cyclic annotation. */
  @CyclicAnnotationA
  static class ClassWithCyclicAnnotation {
    /** Test constructor. */
    ClassWithCyclicAnnotation() {}
  }
}
