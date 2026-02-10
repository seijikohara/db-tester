package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExecutionSettings}. */
@DisplayName("ExecutionSettings")
class ExecutionSettingsTest {

  /** Tests for the ExecutionSettings class. */
  ExecutionSettingsTest() {}

  /** Tests for standard() factory method. */
  @Nested
  @DisplayName("standard()")
  class StandardTest {

    /** Tests for standard() factory method. */
    StandardTest() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("returns instance with default values")
    void returnsInstanceWithDefaultValues() {
      // When
      final var settings = ExecutionSettings.standard();

      // Then
      assertAll(
          "standard settings should have default values",
          () -> assertNotNull(settings, "instance should not be null"),
          () -> assertNull(settings.queryTimeout(), "queryTimeout should be null"),
          () ->
              assertEquals(
                  TransactionMode.SINGLE_TRANSACTION,
                  settings.transactionMode(),
                  "transactionMode should be SINGLE_TRANSACTION"));
    }
  }

  /** Tests for of() factory method. */
  @Nested
  @DisplayName("of()")
  class OfTest {

    /** Tests for of() factory method. */
    OfTest() {}

    /** Verifies that of creates instance with custom values. */
    @Test
    @Tag("normal")
    @DisplayName("creates instance with custom values")
    void createsInstanceWithCustomValues() {
      // Given
      final var timeout = Duration.ofSeconds(30);

      // When
      final var settings = ExecutionSettings.of(timeout, TransactionMode.AUTO_COMMIT);

      // Then
      assertAll(
          "of() should create instance with specified values",
          () -> assertEquals(timeout, settings.queryTimeout(), "queryTimeout should be 30 seconds"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  settings.transactionMode(),
                  "transactionMode should be AUTO_COMMIT"));
    }

    /** Verifies that of accepts null query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("accepts null query timeout")
    void acceptsNullQueryTimeout() {
      // When
      final var settings = ExecutionSettings.of(null, TransactionMode.SINGLE_TRANSACTION);

      // Then
      assertNull(settings.queryTimeout(), "queryTimeout should be null");
    }

    /** Verifies that of throws exception for null transaction mode. */
    @Test
    @Tag("error")
    @DisplayName("throws exception for null transaction mode")
    @SuppressWarnings("NullAway")
    void throwsExceptionForNullTransactionMode() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> ExecutionSettings.of(Duration.ofSeconds(10), null),
          "should throw NullPointerException for null transactionMode");
    }
  }

  /** Tests for builder() factory method. */
  @Nested
  @DisplayName("builder()")
  class BuilderTest {

    /** Tests for builder() factory method. */
    BuilderTest() {}

    /** Verifies that builder creates builder with default values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with default values")
    void createsBuilderWithDefaultValues() {
      // When
      final var builder = ExecutionSettings.builder();

      // Then
      assertNotNull(builder, "builder should not be null");
    }

    /** Verifies that builder builds settings with custom query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom query timeout")
    void buildsSettingsWithCustomQueryTimeout() {
      // Given
      final var timeout = Duration.ofSeconds(30);

      // When
      final var settings = ExecutionSettings.builder().queryTimeout(timeout).build();

      // Then
      assertEquals(timeout, settings.queryTimeout(), "queryTimeout should be 30 seconds");
    }

    /** Verifies that builder builds settings with custom transaction mode. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom transaction mode")
    void buildsSettingsWithCustomTransactionMode() {
      // When
      final var settings =
          ExecutionSettings.builder().transactionMode(TransactionMode.AUTO_COMMIT).build();

      // Then
      assertEquals(
          TransactionMode.AUTO_COMMIT,
          settings.transactionMode(),
          "transactionMode should be AUTO_COMMIT");
    }

    /** Verifies that builder builds settings with all custom values. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with all custom values")
    void buildsSettingsWithAllCustomValues() {
      // Given
      final var timeout = Duration.ofMinutes(1);

      // When
      final var settings =
          ExecutionSettings.builder()
              .queryTimeout(timeout)
              .transactionMode(TransactionMode.NONE)
              .build();

      // Then
      assertAll(
          "settings should have all custom values",
          () -> assertEquals(timeout, settings.queryTimeout(), "queryTimeout should be 1 minute"),
          () ->
              assertEquals(
                  TransactionMode.NONE,
                  settings.transactionMode(),
                  "transactionMode should be NONE"));
    }

    /** Verifies that builder accepts null query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("accepts null query timeout")
    void acceptsNullQueryTimeout() {
      // When
      final var settings = ExecutionSettings.builder().queryTimeout(null).build();

      // Then
      assertNull(settings.queryTimeout(), "queryTimeout should be null");
    }

    /** Verifies that builder throws exception for null transaction mode. */
    @Test
    @Tag("error")
    @DisplayName("throws exception for null transaction mode")
    @SuppressWarnings("NullAway")
    void throwsExceptionForNullTransactionMode() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> ExecutionSettings.builder().transactionMode(null),
          "should throw NullPointerException for null transactionMode");
    }
  }

  /** Tests for with* methods. */
  @Nested
  @DisplayName("with* methods")
  class WithMethodsTest {

    /** Tests for with* methods. */
    WithMethodsTest() {}

    /** Verifies that withQueryTimeout creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withQueryTimeout creates new instance")
    void withQueryTimeoutCreatesNewInstance() {
      // Given
      final var original = ExecutionSettings.standard();
      final var timeout = Duration.ofMinutes(1);

      // When
      final var modified = original.withQueryTimeout(timeout);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(timeout, modified.queryTimeout(), "should have new query timeout");
      assertNull(original.queryTimeout(), "original should remain with null query timeout");
    }

    /** Verifies that withQueryTimeout accepts null. */
    @Test
    @Tag("normal")
    @DisplayName("withQueryTimeout accepts null")
    void withQueryTimeoutAcceptsNull() {
      // Given
      final var original = ExecutionSettings.builder().queryTimeout(Duration.ofSeconds(10)).build();

      // When
      final var modified = original.withQueryTimeout(null);

      // Then
      assertNull(modified.queryTimeout(), "modified should have null query timeout");
    }

    /** Verifies that withTransactionMode creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withTransactionMode creates new instance")
    void withTransactionModeCreatesNewInstance() {
      // Given
      final var original = ExecutionSettings.standard();

      // When
      final var modified = original.withTransactionMode(TransactionMode.AUTO_COMMIT);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(
          TransactionMode.AUTO_COMMIT,
          modified.transactionMode(),
          "should have AUTO_COMMIT transaction mode");
      assertEquals(
          TransactionMode.SINGLE_TRANSACTION,
          original.transactionMode(),
          "original should remain with SINGLE_TRANSACTION");
    }

    /** Verifies that withTransactionMode preserves query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("withTransactionMode preserves query timeout")
    void withTransactionModePreservesQueryTimeout() {
      // Given
      final var timeout = Duration.ofSeconds(30);
      final var original = ExecutionSettings.builder().queryTimeout(timeout).build();

      // When
      final var modified = original.withTransactionMode(TransactionMode.NONE);

      // Then
      assertEquals(timeout, modified.queryTimeout(), "queryTimeout should be preserved");
    }

    /** Verifies that withQueryTimeout preserves transaction mode. */
    @Test
    @Tag("normal")
    @DisplayName("withQueryTimeout preserves transaction mode")
    void withQueryTimeoutPreservesTransactionMode() {
      // Given
      final var original =
          ExecutionSettings.builder().transactionMode(TransactionMode.AUTO_COMMIT).build();

      // When
      final var modified = original.withQueryTimeout(Duration.ofSeconds(10));

      // Then
      assertEquals(
          TransactionMode.AUTO_COMMIT,
          modified.transactionMode(),
          "transactionMode should be preserved");
    }
  }

  /** Tests for toBuilder() method. */
  @Nested
  @DisplayName("toBuilder()")
  class ToBuilderTest {

    /** Tests for toBuilder() method. */
    ToBuilderTest() {}

    /** Verifies that toBuilder creates builder with current values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with current values")
    void createsBuilderWithCurrentValues() {
      // Given
      final var original =
          ExecutionSettings.builder()
              .queryTimeout(Duration.ofSeconds(30))
              .transactionMode(TransactionMode.AUTO_COMMIT)
              .build();

      // When
      final var rebuilt = original.toBuilder().build();

      // Then
      assertEquals(original, rebuilt, "rebuilt should equal original");
    }

    /** Verifies that toBuilder allows modification of copied values. */
    @Test
    @Tag("normal")
    @DisplayName("allows modification of copied values")
    void allowsModificationOfCopiedValues() {
      // Given
      final var original =
          ExecutionSettings.builder().transactionMode(TransactionMode.SINGLE_TRANSACTION).build();

      // When
      final var modified = original.toBuilder().transactionMode(TransactionMode.NONE).build();

      // Then
      assertEquals(
          TransactionMode.SINGLE_TRANSACTION,
          original.transactionMode(),
          "original should retain SINGLE_TRANSACTION");
      assertEquals(TransactionMode.NONE, modified.transactionMode(), "modified should have NONE");
    }

    /** Verifies that toBuilder preserves null query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("preserves null query timeout")
    void preservesNullQueryTimeout() {
      // Given
      final var original = ExecutionSettings.standard();

      // When
      final var rebuilt = original.toBuilder().build();

      // Then
      assertNull(rebuilt.queryTimeout(), "rebuilt should preserve null queryTimeout");
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsHashCodeTest {

    /** Tests for equals and hashCode. */
    EqualsHashCodeTest() {}

    /** Verifies that equals returns true for same values. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same values")
    void equalsReturnsTrueForSameValues() {
      // Given
      final var settings1 = ExecutionSettings.standard();
      final var settings2 = ExecutionSettings.standard();

      // Then
      assertEquals(settings1, settings2, "settings with same values should be equal");
    }

    /** Verifies that equals returns false for different query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different query timeout")
    void equalsReturnsFalseForDifferentQueryTimeout() {
      // Given
      final var settings1 = ExecutionSettings.standard();
      final var settings2 =
          ExecutionSettings.builder().queryTimeout(Duration.ofSeconds(10)).build();

      // Then
      assertNotEquals(
          settings1, settings2, "settings with different queryTimeout should not be equal");
    }

    /** Verifies that equals returns false for different transaction mode. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different transaction mode")
    void equalsReturnsFalseForDifferentTransactionMode() {
      // Given
      final var settings1 = ExecutionSettings.standard();
      final var settings2 =
          ExecutionSettings.builder().transactionMode(TransactionMode.AUTO_COMMIT).build();

      // Then
      assertNotEquals(
          settings1, settings2, "settings with different transactionMode should not be equal");
    }

    /** Verifies that equals returns true for same instance. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same instance")
    void equalsReturnsTrueForSameInstance() {
      // Given
      final var settings = ExecutionSettings.standard();

      // Then
      assertEquals(settings, settings, "settings should be equal to itself");
    }

    /** Verifies that equals returns false for null. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for null")
    void equalsReturnsFalseForNull() {
      // Given
      final var settings = ExecutionSettings.standard();

      // Then
      assertNotEquals(null, settings, "settings should not be equal to null");
    }

    /** Verifies that equals returns false for different type. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different type")
    void equalsReturnsFalseForDifferentType() {
      // Given
      final var settings = ExecutionSettings.standard();

      // Then
      assertNotEquals("string", settings, "settings should not be equal to String");
    }

    /** Verifies that hashCode is consistent for equal objects. */
    @Test
    @Tag("normal")
    @DisplayName("hashCode is consistent for equal objects")
    void hashCodeIsConsistentForEqualObjects() {
      // Given
      final var settings1 = ExecutionSettings.standard();
      final var settings2 = ExecutionSettings.standard();

      // Then
      assertEquals(
          settings1.hashCode(), settings2.hashCode(), "hash codes should match for equal settings");
    }
  }

  /** Tests for toString() method. */
  @Nested
  @DisplayName("toString()")
  class ToStringTest {

    /** Tests for toString() method. */
    ToStringTest() {}

    /** Verifies that toString returns string representation. */
    @Test
    @Tag("normal")
    @DisplayName("returns string representation")
    void returnsStringRepresentation() {
      // Given
      final var settings = ExecutionSettings.standard();

      // When
      final var result = settings.toString();

      // Then
      assertAll(
          "toString should contain field values",
          () -> assertNotNull(result, "toString should not return null"),
          () ->
              assertTrue(
                  result.contains("ExecutionSettings"),
                  "should contain class name 'ExecutionSettings'"),
          () ->
              assertTrue(
                  result.contains("queryTimeout"), "should contain field name 'queryTimeout'"),
          () ->
              assertTrue(
                  result.contains("transactionMode"),
                  "should contain field name 'transactionMode'"),
          () ->
              assertTrue(
                  result.contains("SINGLE_TRANSACTION"), "should contain transaction mode value"));
    }
  }
}
