package io.github.seijikohara.dbtester.api.assertion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
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
 * Unit tests for {@link DatabaseAssertion}.
 *
 * <p>These tests verify that each static method correctly delegates to the underlying {@link
 * io.github.seijikohara.dbtester.api.spi.AssertionProvider} loaded via {@link
 * java.util.ServiceLoader}. A test stub ({@link TestAssertionProvider}) is registered via {@code
 * META-INF/services/} and records all invocations for inspection.
 */
@DisplayName("DatabaseAssertion")
class DatabaseAssertionTest {

  /** Tests for the DatabaseAssertion class. */
  DatabaseAssertionTest() {}

  /** Clears recorded invocations before each test. */
  @BeforeEach
  void setUp() {
    TestAssertionProvider.reset();
  }

  /** Tests for assertEquals(TableSet, TableSet) method. */
  @Nested
  @DisplayName("assertEquals(TableSet, TableSet)")
  class AssertEqualsTableSetTest {

    /** Tests for assertEquals(TableSet, TableSet) method. */
    AssertEqualsTableSetTest() {}

    /** Verifies that assertEquals delegates to provider with TableSet arguments. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with TableSet arguments")
    void shouldDelegateToProvider_withTableSetArguments() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);

      // When
      DatabaseAssertion.assertEquals(expected, actual);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEquals(TableSet,TableSet) to provider",
          () ->
              assertEquals(
                  "assertEquals(TableSet,TableSet)",
                  invocation.methodName(),
                  "should call assertEquals(TableSet,TableSet)"),
          () ->
              assertSame(
                  expected,
                  invocation.arguments().get(0),
                  "should pass expected as first argument"),
          () ->
              assertSame(
                  actual, invocation.arguments().get(1), "should pass actual as second argument"));
    }
  }

  /** Tests for assertEquals(TableSet, TableSet, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(TableSet, TableSet, AssertionFailureHandler)")
  class AssertEqualsTableSetWithHandlerTest {

    /** Tests for assertEquals(TableSet, TableSet, AssertionFailureHandler) method. */
    AssertEqualsTableSetWithHandlerTest() {}

    /** Verifies that assertEquals delegates to provider with custom failure handler. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with custom failure handler")
    void shouldDelegateToProvider_withCustomFailureHandler() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);
      final AssertionFailureHandler handler = (msg, exp, act) -> {};

      // When
      DatabaseAssertion.assertEquals(expected, actual, handler);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEquals with handler to provider",
          () ->
              assertEquals(
                  "assertEquals(TableSet,TableSet,AssertionFailureHandler)",
                  invocation.methodName(),
                  "should call assertEquals(TableSet,TableSet,AssertionFailureHandler)"),
          () ->
              assertSame(
                  handler, invocation.arguments().get(2), "should pass handler as argument"));
    }

    /** Verifies that assertEquals delegates null failure handler to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate null failure handler to provider")
    void shouldDelegateNullHandler_toProvider() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);

      // When
      DatabaseAssertion.assertEquals(expected, actual, null);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertEquals(
          "<<null>>", invocation.arguments().get(2), "should pass null sentinel for null handler");
    }
  }

  /** Tests for assertEquals(Table, Table) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table)")
  class AssertEqualsTableTest {

    /** Tests for assertEquals(Table, Table) method. */
    AssertEqualsTableTest() {}

    /** Verifies that assertEquals delegates to provider with Table arguments. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with Table arguments")
    void shouldDelegateToProvider_withTableArguments() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);

      // When
      DatabaseAssertion.assertEquals(expected, actual);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEquals(Table,Table) to provider",
          () ->
              assertEquals(
                  "assertEquals(Table,Table)",
                  invocation.methodName(),
                  "should call assertEquals(Table,Table)"),
          () ->
              assertSame(
                  expected,
                  invocation.arguments().get(0),
                  "should pass expected as first argument"),
          () ->
              assertSame(
                  actual, invocation.arguments().get(1), "should pass actual as second argument"));
    }
  }

  /** Tests for assertEquals(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table, Collection)")
  class AssertEqualsTableWithColumnsTest {

    /** Tests for assertEquals(Table, Table, Collection) method. */
    AssertEqualsTableWithColumnsTest() {}

    /** Verifies that assertEquals with collection delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with additional column names collection")
    void shouldDelegateToProvider_withAdditionalColumnNamesCollection() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final var columns = List.of("COL_A", "COL_B");

      // When
      DatabaseAssertion.assertEquals(expected, actual, columns);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEquals(Table,Table,Collection) to provider",
          () ->
              assertEquals(
                  "assertEquals(Table,Table,Collection)",
                  invocation.methodName(),
                  "should call assertEquals(Table,Table,Collection)"),
          () ->
              assertEquals(
                  columns, invocation.arguments().get(2), "should pass column names as argument"));
    }

    /** Verifies that varargs overload converts arguments to List and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);

      // When
      DatabaseAssertion.assertEquals(expected, actual, "COL_X", "COL_Y");

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should convert varargs to collection and delegate",
          () ->
              assertEquals(
                  "assertEquals(Table,Table,Collection)",
                  invocation.methodName(),
                  "should call collection overload"),
          () ->
              assertEquals(
                  List.of("COL_X", "COL_Y"),
                  invocation.arguments().get(2),
                  "should convert varargs to list"));
    }
  }

  /** Tests for assertEquals(Table, Table, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table, AssertionFailureHandler)")
  class AssertEqualsTableWithHandlerTest {

    /** Tests for assertEquals(Table, Table, AssertionFailureHandler) method. */
    AssertEqualsTableWithHandlerTest() {}

    /** Verifies that assertEquals with handler delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with Table arguments and failure handler")
    void shouldDelegateToProvider_withTableAndHandler() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final AssertionFailureHandler handler = (msg, exp, act) -> {};

      // When
      DatabaseAssertion.assertEquals(expected, actual, handler);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEquals(Table,Table,AssertionFailureHandler) to provider",
          () ->
              assertEquals(
                  "assertEquals(Table,Table,AssertionFailureHandler)",
                  invocation.methodName(),
                  "should call assertEquals(Table,Table,AssertionFailureHandler)"),
          () ->
              assertSame(
                  handler, invocation.arguments().get(2), "should pass handler as argument"));
    }
  }

  /** Tests for assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(TableSet)")
  class AssertEqualsIgnoreColumnsTableSetTest {

    /** Tests for assertEqualsIgnoreColumns(TableSet) method. */
    AssertEqualsIgnoreColumnsTableSetTest() {}

    /** Verifies that assertEqualsIgnoreColumns with collection delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with collection of column names")
    void shouldDelegateToProvider_withCollectionOfColumnNames() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);
      final var ignoreColumns = List.of("CREATED_AT", "UPDATED_AT");

      // When
      DatabaseAssertion.assertEqualsIgnoreColumns(expected, actual, "USERS", ignoreColumns);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEqualsIgnoreColumns(TableSet) to provider",
          () ->
              assertEquals(
                  "assertEqualsIgnoreColumns(TableSet,TableSet,String,Collection)",
                  invocation.methodName(),
                  "should call assertEqualsIgnoreColumns(TableSet,TableSet,String,Collection)"),
          () ->
              assertEquals(
                  "USERS", invocation.arguments().get(2), "should pass table name as argument"),
          () ->
              assertEquals(
                  ignoreColumns,
                  invocation.arguments().get(3),
                  "should pass ignore columns as argument"));
    }

    /** Verifies that varargs overload converts arguments and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(TableSet.class);
      final var actual = mock(TableSet.class);

      // When
      DatabaseAssertion.assertEqualsIgnoreColumns(expected, actual, "ORDERS", "ID", "VERSION");

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should convert varargs to collection and delegate",
          () ->
              assertEquals(
                  "assertEqualsIgnoreColumns(TableSet,TableSet,String,Collection)",
                  invocation.methodName(),
                  "should call collection overload"),
          () ->
              assertEquals(
                  List.of("ID", "VERSION"),
                  invocation.arguments().get(3),
                  "should convert varargs to list"));
    }
  }

  /** Tests for assertEqualsIgnoreColumns(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(Table)")
  class AssertEqualsIgnoreColumnsTableTest {

    /** Tests for assertEqualsIgnoreColumns(Table) method. */
    AssertEqualsIgnoreColumnsTableTest() {}

    /** Verifies that assertEqualsIgnoreColumns with collection delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with collection of column names")
    void shouldDelegateToProvider_withCollectionOfColumnNames() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final var ignoreColumns = List.of("TIMESTAMP");

      // When
      DatabaseAssertion.assertEqualsIgnoreColumns(expected, actual, ignoreColumns);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEqualsIgnoreColumns(Table) to provider",
          () ->
              assertEquals(
                  "assertEqualsIgnoreColumns(Table,Table,Collection)",
                  invocation.methodName(),
                  "should call assertEqualsIgnoreColumns(Table,Table,Collection)"),
          () ->
              assertEquals(
                  ignoreColumns,
                  invocation.arguments().get(2),
                  "should pass ignore columns as argument"));
    }

    /** Verifies that varargs overload converts arguments and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);

      // When
      DatabaseAssertion.assertEqualsIgnoreColumns(expected, actual, "COL_A", "COL_B");

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertEquals(
          List.of("COL_A", "COL_B"),
          invocation.arguments().get(2),
          "should convert varargs to list");
    }
  }

  /** Tests for assertEqualsWithStrategies method. */
  @Nested
  @DisplayName("assertEqualsWithStrategies")
  class AssertEqualsWithStrategiesTest {

    /** Tests for assertEqualsWithStrategies method. */
    AssertEqualsWithStrategiesTest() {}

    /** Verifies that assertEqualsWithStrategies with collection delegates to provider. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to provider with collection of strategies")
    void shouldDelegateToProvider_withCollectionOfStrategies() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final var strategies = List.of(ColumnStrategyMapping.ignore("CREATED_AT"));

      // When
      DatabaseAssertion.assertEqualsWithStrategies(expected, actual, strategies);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertAll(
          "should delegate assertEqualsWithStrategies to provider",
          () ->
              assertEquals(
                  "assertEqualsWithStrategies(Table,Table,Collection)",
                  invocation.methodName(),
                  "should call assertEqualsWithStrategies(Table,Table,Collection)"),
          () ->
              assertEquals(
                  strategies, invocation.arguments().get(2), "should pass strategies as argument"));
    }

    /** Verifies that varargs overload converts arguments and delegates. */
    @Test
    @Tag("normal")
    @DisplayName("should convert varargs to List and delegate to collection overload")
    void shouldConvertVarargs_toListAndDelegate() {
      // Given
      final var expected = mock(Table.class);
      final var actual = mock(Table.class);
      final var strategy1 = ColumnStrategyMapping.ignore("COL_A");
      final var strategy2 = ColumnStrategyMapping.strict("COL_B");

      // When
      DatabaseAssertion.assertEqualsWithStrategies(expected, actual, strategy1, strategy2);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
      assertEquals(
          List.of(strategy1, strategy2),
          invocation.arguments().get(2),
          "should convert varargs to list");
    }
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
      DatabaseAssertion.assertEqualsByQuery(
          expected, dataSource, "USERS", "SELECT * FROM USERS", ignoreColumns);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
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
      DatabaseAssertion.assertEqualsByQuery(
          expected, dataSource, "USERS", "SELECT * FROM USERS", "ID", "VERSION");

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
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
      DatabaseAssertion.assertEqualsByQuery(
          expected, dataSource, "ORDERS", "SELECT * FROM ORDERS", ignoreColumns);

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
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
      DatabaseAssertion.assertEqualsByQuery(
          expected, dataSource, "ORDERS", "SELECT * FROM ORDERS", "COL1", "COL2");

      // Then
      final var invocation = TestAssertionProvider.getLastInvocation();
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
      final var actual = mock(TableSet.class);

      // When
      DatabaseAssertion.assertEquals(expected, actual);
      DatabaseAssertion.assertEquals(expected, actual);

      // Then
      final var invocations = TestAssertionProvider.getInvocations();
      assertEquals(2, invocations.size(), "should record two invocations from same provider");
    }
  }
}
