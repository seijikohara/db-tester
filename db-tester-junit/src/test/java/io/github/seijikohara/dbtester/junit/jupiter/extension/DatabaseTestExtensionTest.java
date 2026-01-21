package io.github.seijikohara.dbtester.junit.jupiter.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;

/** Unit tests for {@link DatabaseTestExtension}. */
@DisplayName("DatabaseTestExtension")
class DatabaseTestExtensionTest {

  /** Tests for the DatabaseTestExtension class. */
  DatabaseTestExtensionTest() {}

  /** The extension instance under test. */
  private DatabaseTestExtension extension;

  /** Mock extension context for testing. */
  private ExtensionContext mockContext;

  /** Mock store for testing. */
  private Store mockStore;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    extension = new DatabaseTestExtension();
    mockContext = mock(ExtensionContext.class);
    mockStore = mock(Store.class);
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
      final var instance = new DatabaseTestExtension();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the getRegistry(ExtensionContext) method. */
  @Nested
  @DisplayName("getRegistry(ExtensionContext) method")
  class GetRegistryMethod {

    /** Tests for the getRegistry method. */
    GetRegistryMethod() {}

    /** Verifies that getRegistry returns existing registry from store. */
    @Test
    @Tag("normal")
    @DisplayName("should return existing registry when already in store")
    void shouldReturnExistingRegistry_whenAlreadyInStore() {
      // Given
      final var existingRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);

      when(mockContext.getTestClass()).thenReturn(Optional.of(TestClass.class));
      doReturn(TestClass.class).when(mockContext).getRequiredTestClass();
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(org.mockito.ArgumentMatchers.any(Namespace.class)))
          .thenReturn(mockStore);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(existingRegistry);

      // When
      final var result = DatabaseTestExtension.getRegistry(mockContext);

      // Then
      assertSame(existingRegistry, result, "should return existing registry");
    }

    /** Verifies that getRegistry creates new registry when not in store. */
    @Test
    @Tag("normal")
    @DisplayName("should create new registry when not in store")
    void shouldCreateNewRegistry_whenNotInStore() {
      // Given
      final var rootContext = mock(ExtensionContext.class);

      when(mockContext.getTestClass()).thenReturn(Optional.of(TestClass.class));
      doReturn(TestClass.class).when(mockContext).getRequiredTestClass();
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(org.mockito.ArgumentMatchers.any(Namespace.class)))
          .thenReturn(mockStore);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(null);

      // When
      final var result = DatabaseTestExtension.getRegistry(mockContext);

      // Then
      assertNotNull(result, "should return new registry");
    }
  }

  /** Tests for the setConfiguration(ExtensionContext, Configuration) method. */
  @Nested
  @DisplayName("setConfiguration(ExtensionContext, Configuration) method")
  class SetConfigurationMethod {

    /** Tests for the setConfiguration method. */
    SetConfigurationMethod() {}

    /** Verifies that setConfiguration stores configuration in store. */
    @Test
    @Tag("normal")
    @DisplayName("should store configuration when called")
    void shouldStoreConfiguration_whenCalled() {
      // Given
      final var customConfig =
          Configuration.builder()
              .conventions(
                  ConventionSettings.standard()
                      .withBaseDirectory("/custom")
                      .withExpectationSuffix("/verify")
                      .withScenarioMarker("[Test]"))
              .build();
      final var rootContext = mock(ExtensionContext.class);

      when(mockContext.getTestClass()).thenReturn(Optional.of(TestClass.class));
      doReturn(TestClass.class).when(mockContext).getRequiredTestClass();
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(org.mockito.ArgumentMatchers.any(Namespace.class)))
          .thenReturn(mockStore);

      // When
      DatabaseTestExtension.setConfiguration(mockContext, customConfig);

      // Then
      org.mockito.Mockito.verify(mockStore).put("configuration", customConfig);
    }
  }

  /** Tests for the supportsParameter(ParameterContext, ExtensionContext) method. */
  @Nested
  @DisplayName("supportsParameter(ParameterContext, ExtensionContext) method")
  class SupportsParameterMethod {

    /** Tests for the supportsParameter method. */
    SupportsParameterMethod() {}

    /**
     * Verifies that supportsParameter returns true for ExtensionContext parameter.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return true when parameter is ExtensionContext")
    void shouldReturnTrue_whenParameterIsExtensionContext() throws NoSuchMethodException {
      // Given
      final var parameterContext = mock(ParameterContext.class);
      final var method =
          TestClassWithExtensionContextParam.class.getDeclaredMethod(
              "testMethod", ExtensionContext.class);
      final var parameter = method.getParameters()[0];
      when(parameterContext.getParameter()).thenReturn(parameter);

      // When
      final var result = extension.supportsParameter(parameterContext, mockContext);

      // Then
      assertTrue(result, "should support ExtensionContext parameter");
    }

    /**
     * Verifies that supportsParameter returns false for non-ExtensionContext parameter.
     *
     * @throws NoSuchMethodException if the test method is not found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return false when parameter is not ExtensionContext")
    void shouldReturnFalse_whenParameterIsNotExtensionContext() throws NoSuchMethodException {
      // Given
      final var parameterContext = mock(ParameterContext.class);
      final var method =
          TestClassWithStringParam.class.getDeclaredMethod("testMethod", String.class);
      final var parameter = method.getParameters()[0];
      when(parameterContext.getParameter()).thenReturn(parameter);

      // When
      final var result = extension.supportsParameter(parameterContext, mockContext);

      // Then
      assertEquals(false, result, "should not support String parameter");
    }
  }

  /** Tests for the resolveParameter(ParameterContext, ExtensionContext) method. */
  @Nested
  @DisplayName("resolveParameter(ParameterContext, ExtensionContext) method")
  class ResolveParameterMethod {

    /** Tests for the resolveParameter method. */
    ResolveParameterMethod() {}

    /** Verifies that resolveParameter returns extension context. */
    @Test
    @Tag("normal")
    @DisplayName("should return extension context when called")
    void shouldReturnExtensionContext_whenCalled() {
      // Given
      final var parameterContext = mock(ParameterContext.class);

      // When
      final var result = extension.resolveParameter(parameterContext, mockContext);

      // Then
      assertSame(mockContext, result, "should return the extension context");
    }
  }

  /** Tests for the beforeEach(ExtensionContext) method. */
  @Nested
  @DisplayName("beforeEach(ExtensionContext) method")
  class BeforeEachMethod {

    /** Tests for the beforeEach method. */
    BeforeEachMethod() {}

    /**
     * Verifies that beforeEach completes without error when no DataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should complete without error when no DataSet annotation")
    void shouldCompleteWithoutError_whenNoDataSetAnnotation() throws Exception {
      // Given
      final var testClass = TestClassWithoutAnnotations.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();

      // When & Then
      assertDoesNotThrow(
          () -> extension.beforeEach(mockContext),
          "should complete without error when no DataSet annotation");
    }

    /**
     * Verifies that beforeEach processes method-level DataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should process method-level DataSet annotation")
    void shouldProcessMethodLevelDataSetAnnotation() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);
      final var testClass = TestClassWithMethodDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("annotatedMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();
      when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(any(Namespace.class))).thenReturn(mockStore);
      when(mockStore.get("configuration", Configuration.class)).thenReturn(mockConfiguration);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(mockRegistry);
      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(any())).thenReturn(Collections.emptyList());

      // When & Then
      assertDoesNotThrow(
          () -> extension.beforeEach(mockContext),
          "should process method-level DataSet annotation");
    }

    /**
     * Verifies that beforeEach processes class-level DataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should process class-level DataSet annotation")
    void shouldProcessClassLevelDataSetAnnotation() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);
      final var testClass = TestClassWithClassDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();
      when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(any(Namespace.class))).thenReturn(mockStore);
      when(mockStore.get("configuration", Configuration.class)).thenReturn(mockConfiguration);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(mockRegistry);
      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockLoader.loadPreparationDataSets(any())).thenReturn(Collections.emptyList());

      // When & Then
      assertDoesNotThrow(
          () -> extension.beforeEach(mockContext), "should process class-level DataSet annotation");
    }
  }

  /** Tests for the afterEach(ExtensionContext) method. */
  @Nested
  @DisplayName("afterEach(ExtensionContext) method")
  class AfterEachMethod {

    /** Tests for the afterEach method. */
    AfterEachMethod() {}

    /**
     * Verifies that afterEach completes without error when no ExpectedDataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should complete without error when no ExpectedDataSet annotation")
    void shouldCompleteWithoutError_whenNoExpectedDataSetAnnotation() throws Exception {
      // Given
      final var testClass = TestClassWithoutAnnotations.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();

      // When & Then
      assertDoesNotThrow(
          () -> extension.afterEach(mockContext),
          "should complete without error when no ExpectedDataSet annotation");
    }

    /**
     * Verifies that afterEach processes method-level ExpectedDataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should process method-level ExpectedDataSet annotation")
    void shouldProcessMethodLevelExpectedDataSetAnnotation() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = ConventionSettings.standard();
      final var mockRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);
      final var testClass = TestClassWithMethodExpectedDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("annotatedMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();
      when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(any(Namespace.class))).thenReturn(mockStore);
      when(mockStore.get("configuration", Configuration.class)).thenReturn(mockConfiguration);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(mockRegistry);
      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any()))
          .thenReturn(Collections.emptyList());

      // When & Then
      assertDoesNotThrow(
          () -> extension.afterEach(mockContext),
          "should process method-level ExpectedDataSet annotation");
    }

    /**
     * Verifies that afterEach processes class-level ExpectedDataSet annotation.
     *
     * @throws Exception if reflection fails
     */
    @Test
    @Tag("normal")
    @DisplayName("should process class-level ExpectedDataSet annotation")
    void shouldProcessClassLevelExpectedDataSetAnnotation() throws Exception {
      // Given
      final var mockConfiguration = mock(Configuration.class);
      final var mockLoader = mock(DataSetLoader.class);
      final var mockConventions = ConventionSettings.standard();
      final var mockRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);
      final var testClass = TestClassWithClassExpectedDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      doReturn(testClass).when(mockContext).getRequiredTestClass();
      doReturn(testMethod).when(mockContext).getRequiredTestMethod();
      when(mockContext.getTestClass()).thenReturn(Optional.of(testClass));
      when(mockContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(any(Namespace.class))).thenReturn(mockStore);
      when(mockStore.get("configuration", Configuration.class)).thenReturn(mockConfiguration);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(mockRegistry);
      when(mockConfiguration.loader()).thenReturn(mockLoader);
      when(mockConfiguration.conventions()).thenReturn(mockConventions);
      when(mockLoader.loadExpectationDataSetsWithExclusions(any()))
          .thenReturn(Collections.emptyList());

      // When & Then
      assertDoesNotThrow(
          () -> extension.afterEach(mockContext),
          "should process class-level ExpectedDataSet annotation");
    }
  }

  /** Tests for nested test class context hierarchy. */
  @Nested
  @DisplayName("nested test class context hierarchy")
  class NestedTestClassContextHierarchy {

    /** Tests for nested test class context hierarchy. */
    NestedTestClassContextHierarchy() {}

    /** Verifies that getRegistry finds top-level class through parent hierarchy. */
    @Test
    @Tag("normal")
    @DisplayName("should find top-level class through parent hierarchy")
    void shouldFindTopLevelClassThroughParentHierarchy() {
      // Given
      final var existingRegistry = new DataSourceRegistry();
      final var rootContext = mock(ExtensionContext.class);
      final var parentContext = mock(ExtensionContext.class);

      when(mockContext.getTestClass()).thenReturn(Optional.of(NestedTestClass.class));
      doReturn(NestedTestClass.class).when(mockContext).getRequiredTestClass();
      when(mockContext.getParent()).thenReturn(Optional.of(parentContext));
      when(parentContext.getTestClass()).thenReturn(Optional.of(OuterTestClass.class));
      doReturn(OuterTestClass.class).when(parentContext).getRequiredTestClass();
      when(parentContext.getParent()).thenReturn(Optional.empty());
      when(mockContext.getRoot()).thenReturn(rootContext);
      when(rootContext.getStore(any(Namespace.class))).thenReturn(mockStore);
      when(mockStore.get("registry", DataSourceRegistry.class)).thenReturn(existingRegistry);

      // When
      final var result = DatabaseTestExtension.getRegistry(mockContext);

      // Then
      assertSame(existingRegistry, result, "should return registry from top-level class");
    }
  }

  /** Test class for testing. */
  static class TestClass {
    /** Test constructor. */
    TestClass() {}
  }

  /** Test class without annotations. */
  static class TestClassWithoutAnnotations {
    /** Test constructor. */
    TestClassWithoutAnnotations() {}

    /** Test method without annotations. */
    void testMethod() {}
  }

  /** Test class with method-level DataSet annotation. */
  static class TestClassWithMethodDataSet {
    /** Test constructor. */
    TestClassWithMethodDataSet() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void annotatedMethod() {}
  }

  /** Test class with class-level DataSet annotation. */
  @DataSet
  static class TestClassWithClassDataSet {
    /** Test constructor. */
    TestClassWithClassDataSet() {}

    /** Test method without annotation. */
    void testMethod() {}
  }

  /** Test class with method-level ExpectedDataSet annotation. */
  static class TestClassWithMethodExpectedDataSet {
    /** Test constructor. */
    TestClassWithMethodExpectedDataSet() {}

    /** Test method with ExpectedDataSet annotation. */
    @ExpectedDataSet
    void annotatedMethod() {}
  }

  /** Test class with class-level ExpectedDataSet annotation. */
  @ExpectedDataSet
  static class TestClassWithClassExpectedDataSet {
    /** Test constructor. */
    TestClassWithClassExpectedDataSet() {}

    /** Test method without annotation. */
    void testMethod() {}
  }

  /** Outer test class for nested test scenarios. */
  static class OuterTestClass {
    /** Test constructor. */
    OuterTestClass() {}
  }

  /** Nested test class for testing context hierarchy. */
  static class NestedTestClass {
    /** Test constructor. */
    NestedTestClass() {}
  }

  /** Test class with ExtensionContext parameter. */
  static class TestClassWithExtensionContextParam {
    /** Test constructor. */
    TestClassWithExtensionContextParam() {}

    /**
     * Test method with ExtensionContext parameter.
     *
     * @param context the extension context
     */
    void testMethod(final ExtensionContext context) {}
  }

  /** Test class with String parameter. */
  static class TestClassWithStringParam {
    /** Test constructor. */
    TestClassWithStringParam() {}

    /**
     * Test method with String parameter.
     *
     * @param value the string value
     */
    void testMethod(final String value) {}
  }
}
