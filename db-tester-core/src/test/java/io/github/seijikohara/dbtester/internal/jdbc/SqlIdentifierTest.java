package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SqlIdentifier}. */
@DisplayName("SqlIdentifier")
class SqlIdentifierTest {

  /** Tests for the SqlIdentifier class. */
  SqlIdentifierTest() {}

  /** Tests for the validate() method. */
  @Nested
  @DisplayName("validate(String) method")
  class ValidateMethod {

    /** Tests for the validate method. */
    ValidateMethod() {}

    /** Verifies that validate returns identifier when valid simple name. */
    @Test
    @Tag("normal")
    @DisplayName("should return identifier when valid simple name")
    void shouldReturnIdentifier_whenValidSimpleName() {
      // Given
      final var identifier = "USERS";

      // When
      final var result = SqlIdentifier.validate(identifier);

      // Then
      assertEquals("USERS", result, "should return the same identifier");
    }

    /** Verifies that validate accepts lowercase names. */
    @Test
    @Tag("normal")
    @DisplayName("should accept lowercase names")
    void shouldAcceptLowercaseNames() {
      // Given
      final var identifier = "user_accounts";

      // When
      final var result = SqlIdentifier.validate(identifier);

      // Then
      assertEquals("user_accounts", result, "should accept lowercase with underscores");
    }

    /** Verifies that validate accepts names starting with underscore. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept names starting with underscore")
    void shouldAcceptNamesStartingWithUnderscore() {
      // Given
      final var identifier = "_temp_table";

      // When
      final var result = SqlIdentifier.validate(identifier);

      // Then
      assertEquals("_temp_table", result, "should accept names starting with underscore");
    }

    /** Verifies that validate accepts schema-qualified names. */
    @Test
    @Tag("normal")
    @DisplayName("should accept schema-qualified names")
    void shouldAcceptSchemaQualifiedNames() {
      // Given
      final var identifier = "public.users";

      // When
      final var result = SqlIdentifier.validate(identifier);

      // Then
      assertEquals("public.users", result, "should accept schema.table format");
    }

    /** Verifies that validate accepts names with digits. */
    @Test
    @Tag("normal")
    @DisplayName("should accept names with digits")
    void shouldAcceptNamesWithDigits() {
      // Given
      final var identifier = "table123";

      // When
      final var result = SqlIdentifier.validate(identifier);

      // Then
      assertEquals("table123", result, "should accept names containing digits");
    }

    /** Verifies that validate throws when identifier is null. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier is null")
    @SuppressWarnings("NullAway")
    void shouldThrow_whenIdentifierIsNull() {
      // When/Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> SqlIdentifier.validate(null),
              "should throw for null identifier");
      assertEquals(
          "SQL identifier must not be null or empty",
          exception.getMessage(),
          "should have correct error message");
    }

    /** Verifies that validate throws when identifier is empty. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier is empty")
    void shouldThrow_whenIdentifierIsEmpty() {
      // When/Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> SqlIdentifier.validate(""),
              "should throw for empty identifier");
      assertEquals(
          "SQL identifier must not be null or empty",
          exception.getMessage(),
          "should have correct error message");
    }

    /** Verifies that validate throws when identifier starts with digit. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier starts with digit")
    void shouldThrow_whenIdentifierStartsWithDigit() {
      // Given
      final var identifier = "123table";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validate(identifier),
          "should throw for identifier starting with digit");
    }

    /** Verifies that validate throws when identifier contains hyphen. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier contains hyphen")
    void shouldThrow_whenIdentifierContainsHyphen() {
      // Given
      final var identifier = "user-accounts";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validate(identifier),
          "should throw for identifier containing hyphen");
    }

    /** Verifies that validate throws when identifier contains space. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier contains space")
    void shouldThrow_whenIdentifierContainsSpace() {
      // Given
      final var identifier = "user accounts";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validate(identifier),
          "should throw for identifier containing space");
    }

    /** Verifies that validate throws when SQL injection attempt. */
    @Test
    @Tag("error")
    @DisplayName("should throw when SQL injection attempt")
    void shouldThrow_whenSqlInjectionAttempt() {
      // Given
      final var identifier = "; DROP TABLE users; --";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validate(identifier),
          "should throw for SQL injection attempt");
    }

    /** Verifies that validate throws for another SQL injection pattern. */
    @Test
    @Tag("error")
    @DisplayName("should throw for UNION injection attempt")
    void shouldThrow_whenUnionInjectionAttempt() {
      // Given
      final var identifier = "users UNION SELECT * FROM passwords";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validate(identifier),
          "should throw for UNION injection attempt");
    }
  }

  /** Tests for the quote() method. */
  @Nested
  @DisplayName("quote(String) method")
  class QuoteMethod {

    /** Tests for the quote method. */
    QuoteMethod() {}

    /** Verifies that quote wraps identifier in double quotes. */
    @Test
    @Tag("normal")
    @DisplayName("should wrap identifier in double quotes")
    void shouldWrapInDoubleQuotes() {
      // Given
      final var identifier = "USERS";

      // When
      final var result = SqlIdentifier.quote(identifier);

      // Then
      assertEquals("\"USERS\"", result, "should wrap in double quotes");
    }

    /** Verifies that quote escapes embedded double quotes. */
    @Test
    @Tag("edge-case")
    @DisplayName("should escape embedded double quotes")
    void shouldEscapeEmbeddedDoubleQuotes() {
      // Given
      final var identifier = "table\"name";

      // When
      final var result = SqlIdentifier.quote(identifier);

      // Then
      assertEquals("\"table\"\"name\"", result, "should double embedded quotes");
    }

    /** Verifies that quote handles identifier with spaces. */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle identifier with spaces")
    void shouldHandleIdentifierWithSpaces() {
      // Given
      final var identifier = "user name";

      // When
      final var result = SqlIdentifier.quote(identifier);

      // Then
      assertEquals("\"user name\"", result, "should preserve spaces within quotes");
    }

    /** Verifies that quote throws when identifier is null. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier is null")
    @SuppressWarnings("NullAway")
    void shouldThrow_whenIdentifierIsNull() {
      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.quote(null),
          "should throw for null identifier");
    }

    /** Verifies that quote throws when identifier is empty. */
    @Test
    @Tag("error")
    @DisplayName("should throw when identifier is empty")
    void shouldThrow_whenIdentifierIsEmpty() {
      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.quote(""),
          "should throw for empty identifier");
    }
  }

  /** Tests for the validateAndQuote() method. */
  @Nested
  @DisplayName("validateAndQuote(String) method")
  class ValidateAndQuoteMethod {

    /** Tests for the validateAndQuote method. */
    ValidateAndQuoteMethod() {}

    /** Verifies that validateAndQuote validates and quotes valid identifier. */
    @Test
    @Tag("normal")
    @DisplayName("should validate and quote valid identifier")
    void shouldValidateAndQuote_whenValidIdentifier() {
      // Given
      final var identifier = "USERS";

      // When
      final var result = SqlIdentifier.validateAndQuote(identifier);

      // Then
      assertEquals("\"USERS\"", result, "should validate and quote");
    }

    /** Verifies that validateAndQuote throws for invalid identifier. */
    @Test
    @Tag("error")
    @DisplayName("should throw for invalid identifier")
    void shouldThrow_whenInvalidIdentifier() {
      // Given
      final var identifier = "user-name";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validateAndQuote(identifier),
          "should throw for invalid identifier");
    }

    /** Verifies that validateAndQuote prevents SQL injection. */
    @Test
    @Tag("error")
    @DisplayName("should prevent SQL injection")
    void shouldPreventSqlInjection() {
      // Given
      final var maliciousInput = "'; DROP TABLE users; --";

      // When/Then
      assertThrows(
          IllegalArgumentException.class,
          () -> SqlIdentifier.validateAndQuote(maliciousInput),
          "should reject SQL injection attempt");
    }
  }
}
