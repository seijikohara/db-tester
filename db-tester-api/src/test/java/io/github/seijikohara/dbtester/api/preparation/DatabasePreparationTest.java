package io.github.seijikohara.dbtester.api.preparation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.time.Duration;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DatabasePreparation}.
 *
 * <p>These tests verify that each static method correctly delegates to the underlying {@link
 * io.github.seijikohara.dbtester.api.spi.OperationProvider} loaded via {@link
 * java.util.ServiceLoader}. A test stub ({@link TestOperationProvider}) is registered via {@code
 * META-INF/services/} and records all invocations for inspection.
 */
@DisplayName("DatabasePreparation")
class DatabasePreparationTest {

  /** Tests for the DatabasePreparation class. */
  DatabasePreparationTest() {}

  /** Clears recorded invocations before each test. */
  @BeforeEach
  void setUp() {
    TestOperationProvider.reset();
  }

  /** Tests for the cleanInsert(DataSource, TableSet) method. */
  @Nested
  @DisplayName("cleanInsert(DataSource, TableSet) method")
  class CleanInsertMethod {

    /** Tests for the cleanInsert method. */
    CleanInsertMethod() {}

    /** Verifies that cleanInsert delegates CLEAN_INSERT with standard defaults. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate CLEAN_INSERT operation with standard defaults")
    void shouldDelegateCleanInsert_withStandardDefaults() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet);

      // Then
      final var invocation = TestOperationProvider.getLastInvocation();
      assertAll(
          "should delegate cleanInsert with standard defaults",
          () ->
              assertEquals("execute(7-arg)", invocation.methodName(), "should call 7-arg execute"),
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT,
                  invocation.arguments().get(0),
                  "should use CLEAN_INSERT operation"),
          () ->
              assertSame(
                  tableSet, invocation.arguments().get(1), "should pass tableSet as argument"),
          () ->
              assertSame(
                  dataSource, invocation.arguments().get(2), "should pass dataSource as argument"),
          () ->
              assertEquals(
                  TableOrderingStrategy.AUTO,
                  invocation.arguments().get(3),
                  "should use AUTO table ordering"),
          () ->
              assertEquals(
                  TransactionMode.SINGLE_TRANSACTION,
                  invocation.arguments().get(4),
                  "should use SINGLE_TRANSACTION mode"),
          () ->
              assertEquals(
                  "<<null>>", invocation.arguments().get(5), "should have no query timeout"),
          () ->
              assertEquals(0, invocation.arguments().get(6), "should use single-batch execution"));
    }
  }

  /** Tests for the cleanInsert(DataSource, TableSet, PreparationConfig) method. */
  @Nested
  @DisplayName("cleanInsert(DataSource, TableSet, PreparationConfig) method")
  class CleanInsertWithConfigMethod {

    /** Tests for the cleanInsert with config method. */
    CleanInsertWithConfigMethod() {}

    /** Verifies that cleanInsert with config delegates CLEAN_INSERT with custom settings. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate CLEAN_INSERT operation with custom config")
    void shouldDelegateCleanInsert_withCustomConfig() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);
      final var config =
          PreparationConfig.standard()
              .withTransactionMode(TransactionMode.AUTO_COMMIT)
              .withBatchSize(500);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet, config);

      // Then
      final var invocation = TestOperationProvider.getLastInvocation();
      assertAll(
          "should delegate cleanInsert with custom config",
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT,
                  invocation.arguments().get(0),
                  "should use CLEAN_INSERT operation"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  invocation.arguments().get(4),
                  "should use AUTO_COMMIT mode from config"),
          () ->
              assertEquals(
                  500, invocation.arguments().get(6), "should use batch size from config"));
    }
  }

  /** Tests for the execute(DataSource, TableSet, Operation) method. */
  @Nested
  @DisplayName("execute(DataSource, TableSet, Operation) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /** Verifies that execute delegates the specified operation with standard defaults. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate specified operation with standard defaults")
    void shouldDelegateOperation_withStandardDefaults() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);

      // When
      DatabasePreparation.execute(dataSource, tableSet, Operation.INSERT);

      // Then
      final var invocation = TestOperationProvider.getLastInvocation();
      assertAll(
          "should delegate execute with standard defaults",
          () ->
              assertEquals("execute(7-arg)", invocation.methodName(), "should call 7-arg execute"),
          () ->
              assertEquals(
                  Operation.INSERT, invocation.arguments().get(0), "should use INSERT operation"),
          () ->
              assertSame(
                  tableSet, invocation.arguments().get(1), "should pass tableSet as argument"),
          () ->
              assertSame(
                  dataSource, invocation.arguments().get(2), "should pass dataSource as argument"));
    }

    /** Verifies that execute supports all operation types. */
    @Test
    @Tag("normal")
    @DisplayName("should support TRUNCATE_INSERT operation")
    void shouldSupportTruncateInsertOperation() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);

      // When
      DatabasePreparation.execute(dataSource, tableSet, Operation.TRUNCATE_INSERT);

      // Then
      final var invocation = TestOperationProvider.getLastInvocation();
      assertEquals(
          Operation.TRUNCATE_INSERT,
          invocation.arguments().get(0),
          "should use TRUNCATE_INSERT operation");
    }
  }

  /** Tests for the execute(DataSource, TableSet, Operation, PreparationConfig) method. */
  @Nested
  @DisplayName("execute(DataSource, TableSet, Operation, PreparationConfig) method")
  class ExecuteWithConfigMethod {

    /** Tests for the execute with config method. */
    ExecuteWithConfigMethod() {}

    /** Verifies that execute with config delegates with all custom settings. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate with all custom configuration settings")
    void shouldDelegateWithAllCustomSettings() {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);
      final var timeout = Duration.ofSeconds(30);
      final var config =
          new PreparationConfig(
              TableOrderingStrategy.FOREIGN_KEY, TransactionMode.AUTO_COMMIT, timeout, 1000);

      // When
      DatabasePreparation.execute(dataSource, tableSet, Operation.UPSERT, config);

      // Then
      final var invocation = TestOperationProvider.getLastInvocation();
      assertAll(
          "should delegate with all custom config settings",
          () ->
              assertEquals(
                  Operation.UPSERT, invocation.arguments().get(0), "should use UPSERT operation"),
          () ->
              assertEquals(
                  TableOrderingStrategy.FOREIGN_KEY,
                  invocation.arguments().get(3),
                  "should use FOREIGN_KEY strategy"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  invocation.arguments().get(4),
                  "should use AUTO_COMMIT mode"),
          () -> assertEquals(timeout, invocation.arguments().get(5), "should pass query timeout"),
          () -> assertEquals(1000, invocation.arguments().get(6), "should use batch size of 1000"));
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
      final var dataSource = mock(DataSource.class);
      final var tableSet = mock(TableSet.class);

      // When
      DatabasePreparation.cleanInsert(dataSource, tableSet);
      DatabasePreparation.execute(dataSource, tableSet, Operation.INSERT);

      // Then
      final var invocations = TestOperationProvider.getInvocations();
      assertEquals(2, invocations.size(), "should record two invocations from same provider");
    }
  }
}
