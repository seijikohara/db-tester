package io.github.seijikohara.dbtester.api.assertion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DatabaseQueryAssertion}.
 *
 * <p>These tests verify that each static method correctly delegates to the underlying {@link
 * io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider} loaded via {@link
 * java.util.ServiceLoader}. A test stub ({@link TestQueryAssertionProvider}) is registered via
 * {@code META-INF/services/} and records all invocations for inspection.
 */
@DisplayName("DatabaseQueryAssertion")
class DatabaseQueryAssertionTest {

  /** Tests for the DatabaseQueryAssertion class. */
  DatabaseQueryAssertionTest() {}

  /** Clears recorded invocations before each test. */
  @BeforeEach
  void setUp() {
    TestQueryAssertionProvider.reset();
  }

  /** Tests for assertEqualsByQuery(TableSet, ...) method. */
  @Nested
  @DisplayName("assertEqualsByQuery(TableSet)")
  class AssertEqualsByQueryTableSetTest {

    /** Tests for assertEqualsByQuery(TableSet) method. */
    AssertEqualsByQueryTableSetTest() {}

    /** Verifies that assertEqualsByQuery with TableSet delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with TableSet and query parameters")
    void shouldDelegateToProvider_withTableSetAndQueryParameters() {
      // Given
      final var expected = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var ignoreColumns = List.of("ID");

      // When
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "USERS", "SELECT * FROM USERS", ignoreColumns);

      // Then
      final var invocation = TestQueryAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEqualsByQuery(TableSet) to provider",
          () ->
              assertEquals(
                  "assertEqualsByQuery(TableSet,DataSource,String,String,Collection)",
                  invocation.methodName(),
                  "should call assertEqualsByQuery(TableSet,DataSource,String,String,Collection)"),
          () ->
              assertSame(
                  dataSource, invocation.arguments().get(1), "should pass dataSource as argument"),
          () ->
              assertEquals(
                  "SELECT * FROM USERS",
                  invocation.arguments().get(3),
                  "should pass SQL query as argument"));
    }

    /** Verifies that varargs overload converts arguments and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);

      // When
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "USERS", "SELECT * FROM USERS", "ID", "VERSION");

      // Then
      final var invocation = TestQueryAssertionProvider.getLastInvocation();
      assertEquals(
          List.of("ID", "VERSION"),
          invocation.arguments().get(4),
          "should convert varargs to list");
    }
  }

  /** Tests for assertEqualsByQuery(Table, ...) method. */
  @Nested
  @DisplayName("assertEqualsByQuery(Table)")
  class AssertEqualsByQueryTableTest {

    /** Tests for assertEqualsByQuery(Table) method. */
    AssertEqualsByQueryTableTest() {}

    /** Verifies that assertEqualsByQuery with Table delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with Table and query parameters")
    void shouldDelegateToProvider_withTableAndQueryParameters() {
      // Given
      final var expected = mock(Table.class);
      final var dataSource = mock(DataSource.class);
      final var ignoreColumns = List.of("TIMESTAMP");

      // When
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "ORDERS", "SELECT * FROM ORDERS", ignoreColumns);

      // Then
      final var invocation = TestQueryAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEqualsByQuery(Table) to provider",
          () ->
              assertEquals(
                  "assertEqualsByQuery(Table,DataSource,String,String,Collection)",
                  invocation.methodName(),
                  "should call assertEqualsByQuery(Table,DataSource,String,String,Collection)"),
          () ->
              assertSame(
                  expected,
                  invocation.arguments().get(0),
                  "should pass expected table as argument"),
          () ->
              assertEquals(
                  ignoreColumns,
                  invocation.arguments().get(4),
                  "should pass ignore columns as argument"));
    }

    /** Verifies that varargs overload converts arguments and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(Table.class);
      final var dataSource = mock(DataSource.class);

      // When
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "ORDERS", "SELECT * FROM ORDERS", "COL1", "COL2");

      // Then
      final var invocation = TestQueryAssertionProvider.getLastInvocation();
      assertEquals(
          List.of("COL1", "COL2"), invocation.arguments().get(4), "should convert varargs to list");
    }
  }

  /** Tests for ServiceLoader integration. */
  @Nested
  @DisplayName("ServiceLoader integration")
  class ServiceLoaderTest {

    /** Tests for ServiceLoader integration. */
    ServiceLoaderTest() {}

    /** Verifies that multiple calls use the same provider instance. */
    @Test
    @Tag("normal")
    @DisplayName("should use same provider instance across multiple calls")
    void shouldUseSameProviderInstance_acrossMultipleCalls() {
      // Given
      final var expected = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var ignoreColumns = List.of("ID");

      // When
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "T1", "SELECT 1", ignoreColumns);
      DatabaseQueryAssertion.assertEqualsByQuery(
          expected, dataSource, "T2", "SELECT 2", ignoreColumns);

      // Then
      final var invocations = TestQueryAssertionProvider.getInvocations();
      assertEquals(2, invocations.size(), "should record two invocations from same provider");
    }
  }
}
