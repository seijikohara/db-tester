package io.github.seijikohara.dbtester.internal.spi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.internal.jdbc.write.OperationExecutor;
import java.time.Duration;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultOperationProvider}. */
@DisplayName("DefaultOperationProvider")
class DefaultOperationProviderTest {

  /** Tests for the DefaultOperationProvider class. */
  DefaultOperationProviderTest() {}

  /** Mock operation executor. */
  private OperationExecutor mockOperationExecutor;

  /** The provider instance under test. */
  private DefaultOperationProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockOperationExecutor = mock(OperationExecutor.class);
    provider = new DefaultOperationProvider(mockOperationExecutor);
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that default constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when default constructor called")
    void shouldCreateInstance_whenDefaultConstructorCalled() {
      // When
      final var instance = new DefaultOperationProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }

    /** Verifies that constructor with dependencies creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when dependencies provided")
    void shouldCreateInstance_whenDependenciesProvided() {
      // When
      final var instance = new DefaultOperationProvider(mockOperationExecutor);

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute() method. */
  @Nested
  @DisplayName(
      "execute(Operation, TableSet, DataSource, TableOrderingStrategy, TransactionMode, Duration)"
          + " method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /** Verifies that execute delegates to operation executor. */
    @Test
    @Tag("normal")
    @DisplayName("should delegate to operation executor when called")
    void shouldDelegateToOperationExecutor_whenCalled() {
      // Given
      final var operation = Operation.CLEAN_INSERT;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.SINGLE_TRANSACTION;
      final Duration queryTimeout = null;
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              isNull());

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }

    /** Verifies that execute handles INSERT operation. */
    @Test
    @Tag("normal")
    @DisplayName("should handle INSERT operation when called")
    void shouldHandleInsertOperation_whenCalled() {
      // Given
      final var operation = Operation.INSERT;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.SINGLE_TRANSACTION;
      final Duration queryTimeout = null;
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              isNull());

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }

    /** Verifies that execute handles DELETE_ALL operation. */
    @Test
    @Tag("normal")
    @DisplayName("should handle DELETE_ALL operation when called")
    void shouldHandleDeleteAllOperation_whenCalled() {
      // Given
      final var operation = Operation.DELETE_ALL;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.SINGLE_TRANSACTION;
      final Duration queryTimeout = null;
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              isNull());

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }

    /** Verifies that execute handles TRUNCATE_TABLE operation. */
    @Test
    @Tag("normal")
    @DisplayName("should handle TRUNCATE_TABLE operation when called")
    void shouldHandleTruncateTableOperation_whenCalled() {
      // Given
      final var operation = Operation.TRUNCATE_TABLE;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.SINGLE_TRANSACTION;
      final Duration queryTimeout = null;
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              isNull());

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }

    /** Verifies that execute passes query timeout to operation executor. */
    @Test
    @Tag("normal")
    @DisplayName("should pass query timeout to operation executor when specified")
    void shouldPassQueryTimeout_whenSpecified() {
      // Given
      final var operation = Operation.INSERT;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.AUTO_COMMIT;
      final var queryTimeout = Duration.ofSeconds(30);
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              any(Duration.class));

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }

    /** Verifies that execute handles NONE transaction mode. */
    @Test
    @Tag("normal")
    @DisplayName("should handle NONE transaction mode when specified")
    void shouldHandleNoneTransactionMode_whenSpecified() {
      // Given
      final var operation = Operation.INSERT;
      final var dataSet = mock(TableSet.class);
      final var dataSource = mock(DataSource.class);
      final var strategy = TableOrderingStrategy.AUTO;
      final var transactionMode = TransactionMode.NONE;
      final Duration queryTimeout = null;
      doNothing()
          .when(mockOperationExecutor)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              isNull());

      // When
      provider.execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);

      // Then
      verify(mockOperationExecutor)
          .execute(operation, dataSet, dataSource, strategy, transactionMode, queryTimeout);
    }
  }
}
