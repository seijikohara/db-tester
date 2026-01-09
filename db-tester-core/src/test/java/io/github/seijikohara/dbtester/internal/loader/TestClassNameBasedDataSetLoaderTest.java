package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.context.TestContext;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TestClassNameBasedDataSetLoader}. */
@DisplayName("TestClassNameBasedDataSetLoader")
class TestClassNameBasedDataSetLoaderTest {

  /** Tests for the TestClassNameBasedDataSetLoader class. */
  TestClassNameBasedDataSetLoaderTest() {}

  /** Mock data source for tests. */
  private DataSource mockDataSource;

  /** Data source registry for tests. */
  private DataSourceRegistry registry;

  /** Configuration for tests. */
  private Configuration configuration;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockDataSource = mock(DataSource.class);
    registry = new DataSourceRegistry();
    registry.registerDefault(mockDataSource);

    final var conventions =
        new ConventionSettings(
            null,
            "/expected",
            "SCENARIO",
            DataFormat.CSV,
            TableMergeStrategy.UNION_ALL,
            ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
            Set.of(),
            Map.of());
    final var operationDefaults = new OperationDefaults(Operation.CLEAN_INSERT, Operation.NONE);
    final var loader = new TestClassNameBasedDataSetLoader();
    configuration = new Configuration(conventions, operationDefaults, loader);
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var loader = new TestClassNameBasedDataSetLoader();

      // Then
      assertNotNull(loader, "loader should not be null");
    }
  }

  /** Tests for the loadPreparationDataSets() method. */
  @Nested
  @DisplayName("loadPreparationDataSets(TestContext) method")
  class LoadPreparationDataSetsMethod {

    /** Tests for the loadPreparationDataSets method. */
    LoadPreparationDataSetsMethod() {}

    /**
     * Verifies that loadPreparationDataSets returns data sets when Preparation annotation provided.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return data sets when Preparation annotation provided")
    void shouldReturnDataSets_whenPreparationAnnotationProvided() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      assertAll(
          "data sets should be loaded",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"),
          () -> assertEquals(1, result.get(0).getTables().size(), "should have one table"));
    }

    /**
     * Verifies that loadPreparationDataSets returns empty list when no Preparation annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty list when no Preparation annotation")
    void shouldReturnEmptyList_whenNoPreparationAnnotation() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithoutAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      assertEquals(0, result.size(), "should return empty list");
    }
  }

  /** Tests for the loadExpectationDataSets() method. */
  @Nested
  @DisplayName("loadExpectationDataSets(TestContext) method")
  class LoadExpectationDataSetsMethod {

    /** Tests for the loadExpectationDataSets method. */
    LoadExpectationDataSetsMethod() {}

    /**
     * Verifies that loadExpectationDataSets returns data sets when Expectation annotation provided.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return data sets when Expectation annotation provided")
    void shouldReturnDataSets_whenExpectationAnnotationProvided() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithExpectedDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadExpectationDataSets(context);

      // Then
      assertAll(
          "data sets should be loaded",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"),
          () -> assertEquals(1, result.get(0).getTables().size(), "should have one table"));
    }

    /**
     * Verifies that loadExpectationDataSets returns empty list when no Expectation annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty list when no Expectation annotation")
    void shouldReturnEmptyList_whenNoExpectationAnnotation() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithoutAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadExpectationDataSets(context);

      // Then
      assertEquals(0, result.size(), "should return empty list");
    }
  }

  /** Test helper class with DataSet annotation. */
  static class TestHelperWithDataSet {
    /** Test constructor. */
    TestHelperWithDataSet() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void testMethod() {}
  }

  /** Test helper class with ExpectedDataSet annotation. */
  static class TestHelperWithExpectedDataSet {
    /** Test constructor. */
    TestHelperWithExpectedDataSet() {}

    /** Test method with ExpectedDataSet annotation. */
    @ExpectedDataSet
    void testMethod() {}
  }

  /** Test helper class without any annotation. */
  static class TestHelperWithoutAnnotation {
    /** Test constructor. */
    TestHelperWithoutAnnotation() {}

    /** Test method without annotation. */
    void testMethod() {}
  }

  /** Tests for explicit @DataSetSource annotations. */
  @Nested
  @DisplayName("explicit @DataSetSource annotations")
  class ExplicitDataSetMethod {

    /** Tests for explicit DataSetSource annotations. */
    ExplicitDataSetMethod() {}

    /**
     * Verifies that loadPreparationDataSets loads data sets when explicit @DataSetSource with
     * resourceLocation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName(
        "should load data sets when DataSet has explicit @DataSetSource with resourceLocation")
    void shouldLoadDataSets_whenPreparationHasExplicitDataSet() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithExplicitPreparationDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      assertAll(
          "data sets should be loaded from explicit location",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"),
          () -> assertEquals(1, result.get(0).getTables().size(), "should have one table"));
    }

    /**
     * Verifies that loadExpectationDataSets loads data sets when explicit @DataSetSource with
     * resourceLocation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName(
        "should load data sets when ExpectedDataSet has explicit @DataSetSource with resourceLocation")
    void shouldLoadDataSets_whenExpectationHasExplicitDataSet() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithExplicitExpectationDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadExpectationDataSets(context);

      // Then
      assertAll(
          "data sets should be loaded from explicit location",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"),
          () -> assertEquals(1, result.get(0).getTables().size(), "should have one table"));
    }

    /**
     * Verifies that loadPreparationDataSets merges data sets when multiple @DataSetSource
     * annotations.
     *
     * <p>When multiple @DataSetSource annotations are specified, they are merged into a single
     * TableSet according to the configured TableMergeStrategy.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should merge data sets when DataSet has multiple @DataSetSource annotations")
    void shouldMergeDataSets_whenPreparationHasMultipleDataSets() throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithMultipleDataSets.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      // Both datasets have the same table name (users.csv), so they are merged into one table
      // with rows from both datasets combined (UNION_ALL strategy by default)
      assertAll(
          "data sets should be merged into one",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one merged data set"),
          () ->
              assertEquals(
                  1,
                  result.get(0).getTables().size(),
                  "should have one table (same name tables are merged)"));
    }

    /**
     * Verifies that loadPreparationDataSets filters by scenario names when specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should filter by scenario names when @DataSetSource specifies scenarioNames")
    void shouldFilterByScenarioNames_whenDataSetSpecifiesScenarioNames()
        throws NoSuchMethodException {
      // Given
      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithScenarioNames.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      assertAll(
          "data sets should be filtered by scenario names",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"));
    }

    /**
     * Verifies that loadPreparationDataSets uses custom data source when specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should use custom data source when @DataSetSource specifies dataSourceName")
    void shouldUseCustomDataSource_whenDataSetSpecifiesDataSourceName()
        throws NoSuchMethodException {
      // Given
      final var customDataSource = mock(DataSource.class);
      registry.register("custom", customDataSource);

      final var loader = new TestClassNameBasedDataSetLoader();
      final var testClass = TestHelperWithCustomDataSource.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");
      final var context = new TestContext(testClass, testMethod, configuration, registry);

      // When
      final var result = loader.loadPreparationDataSets(context);

      // Then
      assertAll(
          "data sets should use custom data source",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one data set"));
    }
  }

  /** Test helper class with explicit @DataSetSource in @DataSet. */
  static class TestHelperWithExplicitPreparationDataSet {
    /** Test constructor. */
    TestHelperWithExplicitPreparationDataSet() {}

    /** Test method with explicit @DataSetSource. */
    @DataSet(
        dataSets =
            @DataSetSource(
                resourceLocation =
                    "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithExplicitPreparationDataSet/custom-location"))
    void testMethod() {}
  }

  /** Test helper class with explicit @DataSetSource in @ExpectedDataSet. */
  static class TestHelperWithExplicitExpectationDataSet {
    /** Test constructor. */
    TestHelperWithExplicitExpectationDataSet() {}

    /** Test method with explicit @DataSetSource. */
    @ExpectedDataSet(
        dataSets =
            @DataSetSource(
                resourceLocation =
                    "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithExplicitExpectationDataSet/custom-location/expected"))
    void testMethod() {}
  }

  /** Test helper class with multiple @DataSetSource annotations. */
  static class TestHelperWithMultipleDataSets {
    /** Test constructor. */
    TestHelperWithMultipleDataSets() {}

    /** Test method with multiple @DataSetSource. */
    @DataSet(
        dataSets = {
          @DataSetSource(
              resourceLocation =
                  "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithMultipleDataSets/dataset1"),
          @DataSetSource(
              resourceLocation =
                  "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithMultipleDataSets/dataset2")
        })
    void testMethod() {}
  }

  /** Test helper class with @DataSetSource specifying scenarioNames. */
  static class TestHelperWithScenarioNames {
    /** Test constructor. */
    TestHelperWithScenarioNames() {}

    /** Test method with scenarioNames. */
    @DataSet(
        dataSets =
            @DataSetSource(
                resourceLocation =
                    "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithScenarioNames/",
                scenarioNames = {"scenario1"}))
    void testMethod() {}
  }

  /** Test helper class with @DataSetSource specifying dataSourceName. */
  static class TestHelperWithCustomDataSource {
    /** Test constructor. */
    TestHelperWithCustomDataSource() {}

    /** Test method with dataSourceName. */
    @DataSet(
        dataSets =
            @DataSetSource(
                resourceLocation =
                    "classpath:io/github/seijikohara/dbtester/internal/loader/TestClassNameBasedDataSetLoaderTest$TestHelperWithCustomDataSource/",
                dataSourceName = "custom"))
    void testMethod() {}
  }
}
