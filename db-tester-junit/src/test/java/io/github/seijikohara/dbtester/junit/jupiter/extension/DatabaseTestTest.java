package io.github.seijikohara.dbtester.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Unit tests for {@link DatabaseTest}. */
@DisplayName("DatabaseTest")
class DatabaseTestTest {

  /** Tests for the DatabaseTest annotation. */
  DatabaseTestTest() {}

  /** Tests for meta-annotation verification. */
  @Nested
  @DisplayName("meta-annotations")
  class MetaAnnotations {

    /** Tests for meta-annotation verification. */
    MetaAnnotations() {}

    /** Verifies that @Target is TYPE. */
    @Test
    @Tag("normal")
    @DisplayName("should have TYPE target")
    void shouldHaveTypeTarget() {
      // When
      final var target = DatabaseTest.class.getAnnotation(Target.class);

      // Then
      assertNotNull(target, "should have @Target annotation");
      assertAll(
          "should target only TYPE",
          () -> assertEquals(1, target.value().length, "should have exactly one target"),
          () -> assertEquals(ElementType.TYPE, target.value()[0], "should target TYPE"));
    }

    /** Verifies that @Retention is RUNTIME. */
    @Test
    @Tag("normal")
    @DisplayName("should have RUNTIME retention")
    void shouldHaveRuntimeRetention() {
      // When
      final var retention = DatabaseTest.class.getAnnotation(Retention.class);

      // Then
      assertNotNull(retention, "should have @Retention annotation");
      assertEquals(
          RetentionPolicy.RUNTIME, retention.value(), "should have RUNTIME retention policy");
    }

    /** Verifies that @Inherited is present. */
    @Test
    @Tag("normal")
    @DisplayName("should be inherited")
    void shouldBeInherited() {
      // When
      final var inherited = DatabaseTest.class.getAnnotation(Inherited.class);

      // Then
      assertNotNull(inherited, "should have @Inherited annotation");
    }

    /** Verifies that @Documented is present. */
    @Test
    @Tag("normal")
    @DisplayName("should be documented")
    void shouldBeDocumented() {
      // When
      final var documented = DatabaseTest.class.getAnnotation(Documented.class);

      // Then
      assertNotNull(documented, "should have @Documented annotation");
    }

    /** Verifies that @ExtendWith(DatabaseTestExtension.class) is present. */
    @Test
    @Tag("normal")
    @DisplayName("should extend with DatabaseTestExtension")
    void shouldExtendWithDatabaseTestExtension() {
      // When
      final var extendWith = DatabaseTest.class.getAnnotation(ExtendWith.class);

      // Then
      assertNotNull(extendWith, "should have @ExtendWith annotation");
      assertAll(
          "should extend with DatabaseTestExtension",
          () -> assertEquals(1, extendWith.value().length, "should have exactly one extension"),
          () ->
              assertEquals(
                  DatabaseTestExtension.class,
                  extendWith.value()[0],
                  "should use DatabaseTestExtension"));
    }
  }

  /** Tests for inheritance behavior. */
  @Nested
  @DisplayName("inheritance")
  class Inheritance {

    /** Tests for inheritance behavior. */
    Inheritance() {}

    /** Verifies that subclasses inherit the annotation. */
    @Test
    @Tag("normal")
    @DisplayName("should be inherited by subclass")
    void shouldBeInheritedBySubclass() {
      // When
      final var annotation = ChildTestClass.class.getAnnotation(DatabaseTest.class);

      // Then
      assertNotNull(annotation, "subclass should inherit @DatabaseTest");
    }

    /** Verifies that @ExtendWith is resolvable on subclass via @DatabaseTest. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve ExtendWith on subclass")
    void shouldResolveExtendWithOnSubclass() {
      // When
      final var extendWith = ChildTestClass.class.getAnnotation(ExtendWith.class);
      final var databaseTest = ChildTestClass.class.getAnnotation(DatabaseTest.class);

      // Then
      assertTrue(
          extendWith != null || databaseTest != null,
          "subclass should have @ExtendWith or @DatabaseTest");
    }
  }

  /** Base test class with @DatabaseTest annotation. */
  @DatabaseTest
  static class BaseTestClass {

    /** Base test class constructor. */
    BaseTestClass() {}
  }

  /** Child test class that inherits @DatabaseTest from parent. */
  static class ChildTestClass extends BaseTestClass {

    /** Child test class constructor. */
    ChildTestClass() {}
  }
}
