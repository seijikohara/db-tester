package io.github.seijikohara.dbtester.junit.jupiter.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PreparationExecutor}. */
@DisplayName("PreparationExecutor")
class PreparationExecutorTest {

  /** Tests for the PreparationExecutor class. */
  PreparationExecutorTest() {}

  /** The executor instance under test. */
  private PreparationExecutor executor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    executor = new PreparationExecutor();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new PreparationExecutor();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the execute(TestContext, Preparation) method. */
  @Nested
  @DisplayName("execute(TestContext, Preparation) method")
  class ExecuteMethod {

    /** Tests for the execute method. */
    ExecuteMethod() {}

    /**
     * Verifies that execute completes without error when no datasets found.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should complete without error when no datasets found")
    void shouldCompleteWithoutError_whenNoDatasetsFound() throws NoSuchMethodException {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = mock(DataSourceRegistry.class);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(any(TestContext.class)))
          .thenReturn(Collections.emptyList());

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, mockRegistry);

      final var dataSet = testMethod.getAnnotation(DataSet.class);

      // When & Then
      assertDoesNotThrow(
          () -> executor.execute(context, dataSet),
          "should complete without error when no datasets found");
    }

    /**
     * Verifies that execute processes datasets when found.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should execute operation when datasets found")
    void shouldExecuteOperation_whenDatasetsFound() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockOperationProvider = mock(OperationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      // Create a simple TableSet
      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("name")),
              List.of(
                  Row.of(
                      Map.of(
                          new ColumnName("id"),
                          new CellValue("1"),
                          new ColumnName("name"),
                          new CellValue("John")))));
      final var tableSet = TableSet.of(table);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadPreparationDataSets(any(TestContext.class)))
          .thenReturn(List.of(tableSet));
      when(mockConventions.transactionMode()).thenReturn(TransactionMode.SINGLE_TRANSACTION);
      when(mockConventions.queryTimeout()).thenReturn(Duration.ofSeconds(30));

      // Inject mock OperationProvider using reflection
      final Field providerField = PreparationExecutor.class.getDeclaredField("operationProvider");
      providerField.setAccessible(true);
      providerField.set(executor, mockOperationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var dataSet = testMethod.getAnnotation(DataSet.class);

      // When
      executor.execute(context, dataSet);

      // Then
      verify(mockOperationProvider)
          .execute(
              any(Operation.class),
              any(TableSet.class),
              any(DataSource.class),
              any(TableOrderingStrategy.class),
              any(TransactionMode.class),
              any(Duration.class));
    }

    /**
     * Verifies that execute handles null query timeout.
     *
     * @throws Exception if reflection or test setup fails
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle null query timeout")
    void shouldHandleNullQueryTimeout() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = mock(ConventionSettings.class);
      final var mockDataSource = mock(DataSource.class);
      final var mockOperationProvider = mock(OperationProvider.class);

      final var registry = new DataSourceRegistry();
      registry.registerDefault(mockDataSource);

      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id")),
              List.of(Row.of(Map.of(new ColumnName("id"), new CellValue("1")))));
      final var tableSet = TableSet.of(table);

      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadPreparationDataSets(any(TestContext.class)))
          .thenReturn(List.of(tableSet));
      when(mockConventions.transactionMode()).thenReturn(TransactionMode.AUTO_COMMIT);
      when(mockConventions.queryTimeout()).thenReturn(null);

      final Field providerField = PreparationExecutor.class.getDeclaredField("operationProvider");
      providerField.setAccessible(true);
      providerField.set(executor, mockOperationProvider);

      final var testClass = TestClass.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, mockConfiguration, registry);

      final var dataSet = testMethod.getAnnotation(DataSet.class);

      // When & Then
      assertDoesNotThrow(
          () -> executor.execute(context, dataSet), "should handle null query timeout");
    }
  }

  /** Test class with DataSet annotation. */
  static class TestClass {

    /** Test constructor. */
    TestClass() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void testMethod() {}
  }
}
