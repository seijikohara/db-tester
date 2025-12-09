package io.github.seijikohara.dbtester.api.exception;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DatabaseTesterException}. */
@DisplayName("DatabaseTesterException")
class DatabaseTesterExceptionTest {

  /** Tests for the DatabaseTesterException class. */
  DatabaseTesterExceptionTest() {}

  /** Tests for the constructor with message. */
  @Nested
  @DisplayName("constructor(String) method")
  class ConstructorWithMessageMethod {

    /** Tests for the constructor with message. */
    ConstructorWithMessageMethod() {}

    /** Verifies that constructor creates exception with message. */
    @Test
    @Tag("normal")
    @DisplayName("should create exception with message when message provided")
    void shouldCreateException_whenMessageProvided() {
      // Given
      final var message = "Test error message";

      // When
      final var exception = new DatabaseTesterException(message);

      // Then
      assertTrue(message.equals(exception.getMessage()), "should have expected message");
    }

    /** Verifies that constructor creates exception with null cause. */
    @Test
    @Tag("normal")
    @DisplayName("should have null cause when only message provided")
    void shouldHaveNullCause_whenOnlyMessageProvided() {
      // Given
      final var message = "Test error message";

      // When
      final var exception = new DatabaseTesterException(message);

      // Then
      assertNull(exception.getCause(), "cause should be null");
    }
  }

  /** Tests for the constructor with message and cause. */
  @Nested
  @DisplayName("constructor(String, Throwable) method")
  class ConstructorWithMessageAndCauseMethod {

    /** Tests for the constructor with message and cause. */
    ConstructorWithMessageAndCauseMethod() {}

    /** Verifies that constructor creates exception with message and cause. */
    @Test
    @Tag("normal")
    @DisplayName("should create exception with message and cause when both provided")
    void shouldCreateException_whenMessageAndCauseProvided() {
      // Given
      final var message = "Test error message";
      final var cause = new RuntimeException("Root cause");

      // When
      final var exception = new DatabaseTesterException(message, cause);

      // Then
      assertTrue(message.equals(exception.getMessage()), "should have expected message");
      assertSame(cause, exception.getCause(), "should have expected cause");
    }
  }

  /** Tests for the constructor with cause. */
  @Nested
  @DisplayName("constructor(Throwable) method")
  class ConstructorWithCauseMethod {

    /** Tests for the constructor with cause. */
    ConstructorWithCauseMethod() {}

    /** Verifies that constructor creates exception with cause. */
    @Test
    @Tag("normal")
    @DisplayName("should create exception with cause when cause provided")
    void shouldCreateException_whenCauseProvided() {
      // Given
      final var cause = new RuntimeException("Root cause");

      // When
      final var exception = new DatabaseTesterException(cause);

      // Then
      assertSame(cause, exception.getCause(), "should have expected cause");
      assertNotNull(exception.getMessage(), "message should not be null");
    }
  }

  /** Tests for RuntimeException hierarchy. */
  @Nested
  @DisplayName("exception hierarchy")
  class ExceptionHierarchy {

    /** Tests for exception hierarchy. */
    ExceptionHierarchy() {}

    /** Verifies that exception extends RuntimeException. */
    @Test
    @Tag("normal")
    @DisplayName("should extend RuntimeException")
    void shouldExtendRuntimeException() {
      // Then
      assertTrue(
          RuntimeException.class.isAssignableFrom(DatabaseTesterException.class),
          "should extend RuntimeException");
    }
  }
}
