package io.github.seijikohara.dbtester.internal.jdbc;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.wrapFunction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ResultSetIterator}. */
@DisplayName("ResultSetIterator")
class ResultSetIteratorTest {

  /** Creates a new test instance. */
  ResultSetIteratorTest() {}

  /** Tests for the stream() method. */
  @Nested
  @DisplayName("stream(ResultSet, RowMapper) method")
  class StreamMethod {

    /** Creates a new test instance. */
    StreamMethod() {}

    /**
     * Verifies that stream returns empty when ResultSet is empty.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty stream when ResultSet is empty")
    void shouldReturnEmptyStream_whenResultSetIsEmpty() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(false);

      // When
      final var result =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")))
              .collect(Collectors.toList());

      // Then
      assertTrue(result.isEmpty(), "should return empty list");
      verify(resultSet, times(1)).next();
    }

    /**
     * Verifies that stream processes single row correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should process single row correctly")
    void shouldProcessSingleRow_whenResultSetHasOneRow() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, false);
      when(resultSet.getString("name")).thenReturn("Alice");

      // When
      final var result =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")))
              .collect(Collectors.toList());

      // Then
      assertEquals(List.of("Alice"), result, "should return single element");
    }

    /**
     * Verifies that stream processes multiple rows correctly.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should process multiple rows correctly")
    void shouldProcessMultipleRows_whenResultSetHasMultipleRows() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, true, true, false);
      when(resultSet.getString("name")).thenReturn("Alice", "Bob", "Charlie");

      // When
      final var result =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")))
              .collect(Collectors.toList());

      // Then
      assertEquals(
          List.of("Alice", "Bob", "Charlie"), result, "should return all elements in order");
    }

    /**
     * Verifies that stream supports complex mapping operations.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should support complex mapping operations")
    void shouldSupportComplexMapping_whenUsingRowMapper() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, true, false);
      when(resultSet.getString("first_name")).thenReturn("John", "Jane");
      when(resultSet.getString("last_name")).thenReturn("Doe", "Smith");

      // When
      final var result =
          ResultSetIterator.stream(
                  resultSet,
                  wrapFunction(
                      row ->
                          String.format(
                              "%s %s", row.getString("first_name"), row.getString("last_name"))))
              .collect(Collectors.toList());

      // Then
      assertEquals(List.of("John Doe", "Jane Smith"), result, "should combine columns correctly");
    }

    /**
     * Verifies that stream supports filtering.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should support stream filtering")
    void shouldSupportFiltering_whenFilterApplied() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, true, true, false);
      when(resultSet.getInt("age")).thenReturn(25, 17, 30);

      // When
      final var result =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getInt("age")))
              .filter(age -> age >= 18)
              .collect(Collectors.toList());

      // Then
      assertEquals(List.of(25, 30), result, "should filter values correctly");
    }

    /**
     * Verifies that SQLException during next() is wrapped in DatabaseTesterException.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException during cursor advance")
    void shouldWrapSqlException_whenNextFails() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenThrow(new SQLException("Connection lost"));

      // When/Then
      final var stream =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")));
      assertThrows(DatabaseTesterException.class, stream::findFirst);
    }

    /**
     * Verifies that SQLException during mapping is wrapped in DatabaseTesterException.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("error")
    @DisplayName("should wrap SQLException during row mapping")
    void shouldWrapSqlException_whenMappingFails() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true);
      when(resultSet.getString("name")).thenThrow(new SQLException("Column not found"));

      // When/Then
      final var stream =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")));
      assertThrows(DatabaseTesterException.class, stream::findFirst);
    }
  }

  /** Tests for Iterator behavior. */
  @Nested
  @DisplayName("Iterator behavior")
  class IteratorBehavior {

    /** Creates a new test instance. */
    IteratorBehavior() {}

    /**
     * Verifies that hasNext() can be called multiple times.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should allow multiple hasNext() calls without advancing cursor")
    void shouldAllowMultipleHasNextCalls_withoutAdvancingCursor() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, false);
      when(resultSet.getString("name")).thenReturn("Alice");

      final var stream =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")));
      final var iterator = stream.iterator();

      // When
      final var first = iterator.hasNext();
      final var second = iterator.hasNext();
      final var third = iterator.hasNext();

      // Then
      assertTrue(first, "first hasNext should return true");
      assertTrue(second, "second hasNext should return true");
      assertTrue(third, "third hasNext should return true");
      verify(resultSet, times(1)).next();
    }

    /**
     * Verifies that next() throws NoSuchElementException when no more elements.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should throw NoSuchElementException when no more elements")
    void shouldThrowNoSuchElementException_whenNoMoreElements() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(false);

      final var stream =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")));
      final var iterator = stream.iterator();

      // When/Then
      assertFalse(iterator.hasNext(), "hasNext should return false");
      assertThrows(
          NoSuchElementException.class, iterator::next, "should throw NoSuchElementException");
    }

    /**
     * Verifies that iterator processes elements in sequence.
     *
     * @throws SQLException if a database access error occurs
     */
    @Test
    @Tag("normal")
    @DisplayName("should process elements in sequence")
    void shouldProcessElementsInSequence() throws SQLException {
      // Given
      final var resultSet = mock(ResultSet.class);
      when(resultSet.next()).thenReturn(true, true, false);
      when(resultSet.getString("name")).thenReturn("First", "Second");

      final var stream =
          ResultSetIterator.stream(resultSet, wrapFunction(row -> row.getString("name")));
      final var iterator = stream.iterator();

      // When/Then
      assertTrue(iterator.hasNext(), "should have first element");
      assertEquals("First", iterator.next(), "first element should be 'First'");
      assertTrue(iterator.hasNext(), "should have second element");
      assertEquals("Second", iterator.next(), "second element should be 'Second'");
      assertFalse(iterator.hasNext(), "should have no more elements");
    }
  }
}
