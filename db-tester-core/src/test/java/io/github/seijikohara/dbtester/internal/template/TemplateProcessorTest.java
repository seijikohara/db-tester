package io.github.seijikohara.dbtester.internal.template;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TemplateProcessor}. */
@DisplayName("TemplateProcessor")
class TemplateProcessorTest {

  /** Tests for the TemplateProcessor class. */
  TemplateProcessorTest() {}

  /** The processor instance under test. */
  private TemplateProcessor processor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    processor = new TemplateProcessor();
  }

  /** Tests for the containsExpression() method. */
  @Nested
  @DisplayName("containsExpression(String) method")
  class ContainsExpressionMethod {

    /** Tests for the containsExpression method. */
    ContainsExpressionMethod() {}

    /** Verifies that containsExpression returns true when value contains expression. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value contains expression")
    void shouldReturnTrue_whenValueContainsExpression() {
      // When
      final var result = TemplateProcessor.containsExpression("${uuid}");

      // Then
      assertTrue(result, "should detect expression in value");
    }

    /** Verifies that containsExpression returns false when value has no expression. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when value has no expression")
    void shouldReturnFalse_whenValueHasNoExpression() {
      // When
      final var result = TemplateProcessor.containsExpression("plain text");

      // Then
      assertFalse(result, "should not detect expression in plain text");
    }

    /** Verifies that containsExpression returns true when value has embedded expression. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value has embedded expression")
    void shouldReturnTrue_whenValueHasEmbeddedExpression() {
      // When
      final var result = TemplateProcessor.containsExpression("prefix-${uuid}-suffix");

      // Then
      assertTrue(result, "should detect embedded expression");
    }
  }

  /** Tests for the process() method with plain values. */
  @Nested
  @DisplayName("process(String) method - plain values")
  class ProcessPlainValuesMethod {

    /** Tests for process method with plain values. */
    ProcessPlainValuesMethod() {}

    /** Verifies that process returns value unchanged when no expression present. */
    @Test
    @Tag("normal")
    @DisplayName("should return value unchanged when no expression present")
    void shouldReturnValueUnchanged_whenNoExpressionPresent() {
      // When
      final var result = processor.process("plain text");

      // Then
      assertEquals("plain text", result, "should return plain text unchanged");
    }

    /** Verifies that process returns empty string unchanged. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty string unchanged")
    void shouldReturnEmptyStringUnchanged() {
      // When
      final var result = processor.process("");

      // Then
      assertEquals("", result, "should return empty string unchanged");
    }

    /** Verifies that process returns unrecognized expression unchanged. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unrecognized expression unchanged")
    void shouldReturnUnrecognizedExpressionUnchanged() {
      // When
      final var result = processor.process("${unknown}");

      // Then
      assertEquals("${unknown}", result, "should return unrecognized expression unchanged");
    }
  }

  /** Tests for the process() method with sequence expressions. */
  @Nested
  @DisplayName("process(String) method - sequence expressions")
  class ProcessSequenceMethod {

    /** Tests for process method with sequence expressions. */
    ProcessSequenceMethod() {}

    /** Verifies that process resolves sequence with start value. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve sequence with start value")
    void shouldResolveSequence_whenStartValueProvided() {
      // When
      final var result = processor.process("${sequence:1}");

      // Then
      assertEquals("1", result, "should resolve sequence to start value");
    }

    /** Verifies that process auto-increments sequence after initial value. */
    @Test
    @Tag("normal")
    @DisplayName("should auto-increment sequence after initial value")
    void shouldAutoIncrementSequence_afterInitialValue() {
      // Given
      processor.process("${sequence:1}");

      // When
      final var second = processor.process("${sequence}");
      final var third = processor.process("${sequence}");

      // Then
      assertAll(
          "sequence should auto-increment",
          () -> assertEquals("2", second, "second call should return 2"),
          () -> assertEquals("3", third, "third call should return 3"));
    }

    /** Verifies that process resolves sequence with custom start value. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve sequence with custom start value")
    void shouldResolveSequence_whenCustomStartValueProvided() {
      // When
      final var result = processor.process("${sequence:100}");
      final var next = processor.process("${sequence}");

      // Then
      assertAll(
          "sequence should start from custom value",
          () -> assertEquals("100", result, "should start at 100"),
          () -> assertEquals("101", next, "should increment to 101"));
    }

    /** Verifies that process resolves sequence embedded in text. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve sequence embedded in text")
    void shouldResolveSequence_whenEmbeddedInText() {
      // Given
      processor.process("${sequence:1}");

      // When
      final var result = processor.process("ID-${sequence}");

      // Then
      assertEquals("ID-2", result, "should resolve sequence within text");
    }
  }

  /** Tests for the process() method with UUID expressions. */
  @Nested
  @DisplayName("process(String) method - UUID expressions")
  class ProcessUuidMethod {

    /** Tests for process method with UUID expressions. */
    ProcessUuidMethod() {}

    /** Verifies that process generates valid UUID. */
    @Test
    @Tag("normal")
    @DisplayName("should generate valid UUID")
    void shouldGenerateValidUuid() {
      // When
      final var result = processor.process("${uuid}");

      // Then
      assertAll(
          "should generate valid UUID",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertNotNull(UUID.fromString(result), "should be parseable as UUID"));
    }

    /** Verifies that process generates unique UUIDs for each call. */
    @Test
    @Tag("normal")
    @DisplayName("should generate unique UUIDs for each call")
    void shouldGenerateUniqueUuids_forEachCall() {
      // When
      final var first = processor.process("${uuid}");
      final var second = processor.process("${uuid}");

      // Then
      assertFalse(first.equals(second), "UUIDs should be unique");
    }
  }

  /** Tests for the process() method with date/time expressions. */
  @Nested
  @DisplayName("process(String) method - date/time expressions")
  class ProcessDateTimeMethod {

    /** Tests for process method with date/time expressions. */
    ProcessDateTimeMethod() {}

    /** Verifies that process resolves now to current date/time. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve now to current date/time")
    void shouldResolveNow_toCurrentDateTime() {
      // Given
      final var before = LocalDateTime.now(ZoneId.systemDefault());

      // When
      final var result = processor.process("${now}");

      // Then
      final var parsed = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      final var after = LocalDateTime.now(ZoneId.systemDefault());
      assertAll(
          "should resolve to current date/time",
          () -> assertNotNull(parsed, "should be parseable as LocalDateTime"),
          () ->
              assertFalse(
                  parsed.isBefore(before.minusSeconds(1)), "should not be before test start"),
          () -> assertFalse(parsed.isAfter(after.plusSeconds(1)), "should not be after test end"));
    }

    /** Verifies that process resolves relative date/time with days. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve relative date/time with days")
    void shouldResolveRelativeDateTime_withDays() {
      // Given
      final var expectedApprox = LocalDateTime.now(ZoneId.systemDefault()).minusDays(7);

      // When
      final var result = processor.process("${now-7d}");

      // Then
      final var parsed = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      assertAll(
          "should resolve to 7 days ago",
          () -> assertNotNull(parsed, "should be parseable"),
          () ->
              assertTrue(
                  Math.abs(Duration.between(expectedApprox, parsed).toSeconds()) < 5,
                  "should be approximately 7 days ago"));
    }

    /** Verifies that process resolves relative date/time with hours. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve relative date/time with hours")
    void shouldResolveRelativeDateTime_withHours() {
      // Given
      final var expectedApprox = LocalDateTime.now(ZoneId.systemDefault()).plusHours(2);

      // When
      final var result = processor.process("${now+2h}");

      // Then
      final var parsed = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      assertTrue(
          Math.abs(Duration.between(expectedApprox, parsed).toSeconds()) < 5,
          "should be approximately 2 hours ahead");
    }

    /** Verifies that process resolves relative date/time with minutes. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve relative date/time with minutes")
    void shouldResolveRelativeDateTime_withMinutes() {
      // Given
      final var expectedApprox = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(30);

      // When
      final var result = processor.process("${now-30m}");

      // Then
      final var parsed = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      assertTrue(
          Math.abs(Duration.between(expectedApprox, parsed).toSeconds()) < 5,
          "should be approximately 30 minutes ago");
    }

    /** Verifies that process resolves relative date/time with seconds. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve relative date/time with seconds")
    void shouldResolveRelativeDateTime_withSeconds() {
      // Given
      final var expectedApprox = LocalDateTime.now(ZoneId.systemDefault()).plusSeconds(10);

      // When
      final var result = processor.process("${now+10s}");

      // Then
      final var parsed = LocalDateTime.parse(result, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      assertTrue(
          Math.abs(Duration.between(expectedApprox, parsed).toSeconds()) < 5,
          "should be approximately 10 seconds ahead");
    }

    /** Verifies that process returns invalid relative expression unchanged. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return invalid relative expression unchanged")
    void shouldReturnInvalidRelativeExpressionUnchanged() {
      // When
      final var result = processor.process("${now-abc}");

      // Then
      assertEquals("${now-abc}", result, "should return invalid expression unchanged");
    }
  }

  /** Tests for the process() method with Faker expressions. */
  @Nested
  @DisplayName("process(String) method - Faker expressions")
  class ProcessFakerMethod {

    /** Tests for process method with Faker expressions. */
    ProcessFakerMethod() {}

    /** Verifies that process resolves faker name expression. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve faker name expression")
    void shouldResolveFakerNameExpression() {
      // When
      final var result = processor.process("${faker.name.fullName}");

      // Then
      assertAll(
          "should resolve to a non-empty name",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertFalse(result.isEmpty(), "result should not be empty"),
          () ->
              assertFalse(
                  result.startsWith("${"), "result should not contain unresolved expression"));
    }

    /** Verifies that process resolves faker internet expression. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve faker internet expression")
    void shouldResolveFakerInternetExpression() {
      // When
      final var result = processor.process("${faker.internet.emailAddress}");

      // Then
      assertAll(
          "should resolve to an email-like string",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertTrue(result.contains("@"), "result should contain @ symbol"));
    }

    /** Verifies that process returns unknown faker method unchanged. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unknown faker method unchanged")
    void shouldReturnUnknownFakerMethodUnchanged() {
      // When
      final var result = processor.process("${faker.nonexistent.method}");

      // Then
      assertEquals(
          "${faker.nonexistent.method}",
          result,
          "should return unknown faker expression unchanged");
    }
  }

  /** Tests for the process() method with multiple expressions. */
  @Nested
  @DisplayName("process(String) method - multiple expressions")
  class ProcessMultipleExpressionsMethod {

    /** Tests for process method with multiple expressions. */
    ProcessMultipleExpressionsMethod() {}

    /** Verifies that process resolves multiple expressions in one value. */
    @Test
    @Tag("normal")
    @DisplayName("should resolve multiple expressions in one value")
    void shouldResolveMultipleExpressions_inOneValue() {
      // Given
      processor.process("${sequence:1}");

      // When
      final var result = processor.process("${sequence}-${uuid}");

      // Then
      final var parts = result.split("-", 2);
      assertAll(
          "should resolve both expressions",
          () -> assertEquals("2", parts[0], "first part should be sequence value"),
          () -> assertNotNull(UUID.fromString(parts[1]), "second part should be valid UUID"));
    }

    /** Verifies that process handles mixed plain text and expressions. */
    @Test
    @Tag("normal")
    @DisplayName("should handle mixed plain text and expressions")
    void shouldHandleMixedPlainTextAndExpressions() {
      // Given
      processor.process("${sequence:10}");

      // When
      final var result = processor.process("user_${sequence}@example.com");

      // Then
      assertEquals("user_11@example.com", result, "should resolve expression within plain text");
    }
  }
}
