package io.github.seijikohara.dbtester.internal.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Jdbc}. */
@DisplayName("Jdbc")
class JdbcTest {

  /** Tests for the Jdbc class. */
  JdbcTest() {}

  /** Tests for the run() method. */
  @Nested
  @DisplayName("run(ThrowingRunnable) method")
  class RunMethod {

    /** Tests for the run method. */
    RunMethod() {}

    /**
     * Verifies that run executes the operation successfully.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should execute operation successfully")
    void shouldExecuteOperation_whenNoException() throws SQLException {
      // Given
      final var executed = new boolean[] {false};

      // When
      Jdbc.run(() -> executed[0] = true);

      // Then
      assertEquals(true, executed[0], "operation should have been executed");
    }

    /**
     * Verifies that run wraps SQLException in DatabaseOperationException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException in DatabaseOperationException")
    void shouldWrapException_whenSqlExceptionThrown() throws SQLException {
      // Given
      final var cause = new SQLException("Connection failed");

      // When/Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () ->
                  Jdbc.run(
                      () -> {
                        throw cause;
                      }));

      assertEquals(cause, exception.getCause(), "cause should be the original SQLException");
    }
  }

  /** Tests for the get() method. */
  @Nested
  @DisplayName("get(ThrowingSupplier) method")
  class GetMethod {

    /** Tests for the get method. */
    GetMethod() {}

    /**
     * Verifies that get returns the result successfully.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should return result successfully")
    void shouldReturnResult_whenNoException() throws SQLException {
      // Given
      final var expected = "test result";

      // When
      final var result = Jdbc.get(() -> expected);

      // Then
      assertEquals(expected, result, "result should match expected value");
    }

    /**
     * Verifies that get wraps SQLException in DatabaseOperationException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException in DatabaseOperationException")
    void shouldWrapException_whenSqlExceptionThrown() throws SQLException {
      // Given
      final var cause = new SQLException("Query failed");

      // When/Then
      final var exception =
          assertThrows(
              DatabaseOperationException.class,
              () ->
                  Jdbc.get(
                      () -> {
                        throw cause;
                      }));

      assertEquals(cause, exception.getCause(), "cause should be the original SQLException");
    }
  }

  /** Tests for the wrapConsumer() method. */
  @Nested
  @DisplayName("wrapConsumer() method")
  class WrapConsumerMethod {

    /** Tests for the wrapConsumer method. */
    WrapConsumerMethod() {}

    /**
     * Verifies that wrapConsumer executes successfully.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should execute consumer successfully")
    void shouldExecuteConsumer_whenNoException() throws SQLException {
      // Given
      final List<Integer> collected = new ArrayList<>();
      final Consumer<Integer> consumer = Jdbc.wrapConsumer(collected::add);

      // When
      IntStream.range(0, 3).boxed().forEach(consumer);

      // Then
      assertEquals(List.of(0, 1, 2), collected, "all values should be collected");
    }

    /**
     * Verifies that wrapConsumer wraps SQLException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException in DatabaseOperationException")
    void shouldWrapException_whenSqlExceptionThrown() throws SQLException {
      // Given
      final var cause = new SQLException("Insert failed");
      final Consumer<Integer> consumer =
          Jdbc.wrapConsumer(
              i -> {
                throw cause;
              });

      // When/Then
      final var exception =
          assertThrows(DatabaseOperationException.class, () -> consumer.accept(5));

      assertEquals(cause, exception.getCause(), "cause should be the original SQLException");
    }
  }

  /** Tests for the wrapFunction() method. */
  @Nested
  @DisplayName("wrapFunction() method")
  class WrapFunctionMethod {

    /** Tests for the wrapFunction method. */
    WrapFunctionMethod() {}

    /**
     * Verifies that wrapFunction transforms values successfully.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should transform values successfully")
    void shouldTransformValues_whenNoException() throws SQLException {
      // Given
      final Function<Integer, String> function =
          Jdbc.wrapFunction(i -> String.format("value-%d", i));

      // When
      final var result = function.apply(42);

      // Then
      assertEquals("value-42", result, "value should be transformed");
    }

    /**
     * Verifies that wrapFunction wraps SQLException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException in DatabaseOperationException")
    void shouldWrapException_whenSqlExceptionThrown() throws SQLException {
      // Given
      final var cause = new SQLException("Lookup failed");
      final Function<String, Integer> function =
          Jdbc.wrapFunction(
              s -> {
                throw cause;
              });

      // When/Then
      final var exception =
          assertThrows(DatabaseOperationException.class, () -> function.apply("test"));

      assertEquals(cause, exception.getCause(), "cause should be the original SQLException");
    }
  }

  /** Tests for the wrapSupplier() method. */
  @Nested
  @DisplayName("wrapSupplier(ThrowingSupplier) method")
  class WrapSupplierMethod {

    /** Tests for the wrapSupplier method. */
    WrapSupplierMethod() {}

    /**
     * Verifies that wrapSupplier supplies value successfully.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should supply value successfully")
    void shouldSupplyValue_whenNoException() throws SQLException {
      // Given
      final Supplier<String> supplier = Jdbc.wrapSupplier(() -> "result");

      // When
      final var result = supplier.get();

      // Then
      assertEquals("result", result, "supplier should return value");
    }

    /**
     * Verifies that wrapSupplier wraps SQLException.
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException in DatabaseOperationException")
    void shouldWrapException_whenSqlExceptionThrown() throws SQLException {
      // Given
      final var cause = new SQLException("Cannot get value");
      final Supplier<String> supplier =
          Jdbc.wrapSupplier(
              () -> {
                throw cause;
              });

      // When/Then
      final var exception = assertThrows(DatabaseOperationException.class, supplier::get);

      assertEquals(cause, exception.getCause(), "cause should be the original SQLException");
    }
  }

  /** Tests for stream integration. */
  @Nested
  @DisplayName("Stream integration")
  class StreamIntegration {

    /** Tests for stream integration. */
    StreamIntegration() {}

    /**
     * Verifies that wrappers work with IntStream.forEach().
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should work with IntStream.forEach()")
    void shouldWorkWithIntStreamForEach() throws SQLException {
      // Given
      final List<Integer> collected = new ArrayList<>();

      // When
      IntStream.range(0, 5).forEach(i -> Jdbc.run(() -> collected.add(i)));

      // Then
      assertEquals(List.of(0, 1, 2, 3, 4), collected, "all values should be processed");
    }

    /**
     * Verifies that wrappers work with Stream.map().
     *
     * @throws SQLException if a database error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should work with Stream.map()")
    void shouldWorkWithStreamMap() throws SQLException {
      // Given
      final var input = List.of(1, 2, 3);

      // When
      final var result = input.stream().map(Jdbc.wrapFunction(i -> i * 2)).toList();

      // Then
      assertEquals(List.of(2, 4, 6), result, "values should be doubled");
    }
  }
}
