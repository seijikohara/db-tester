package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy;
import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.annotation.DataSetSource;
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
import io.github.seijikohara.dbtester.api.annotation.Strategy;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import io.github.seijikohara.dbtester.api.domain.DataSourceName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnnotationResolver}. */
@DisplayName("AnnotationResolver")
class AnnotationResolverTest {

  /** Tests for the AnnotationResolver class. */
  AnnotationResolverTest() {}

  /** The annotation resolver instance to test. */
  private AnnotationResolver resolver;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    resolver = new AnnotationResolver();
  }

  /** Tests for the findDataSet() method. */
  @Nested
  @DisplayName("findDataSet(Method, Class<?>) method")
  class FindPreparationMethod {

    /** Tests for the findPreparation method. */
    FindPreparationMethod() {}

    /**
     * Verifies that findPreparation returns annotation when method has Preparation annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when method has Preparation annotation")
    void shouldReturnAnnotation_whenMethodHasPreparationAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = TestClassWithMethodAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      // When
      final var result = resolver.findDataSet(testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find Preparation annotation on method");
    }

    /**
     * Verifies that findPreparation returns annotation when class has Preparation annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when class has Preparation annotation")
    void shouldReturnAnnotation_whenClassHasPreparationAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = TestClassWithClassAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      // When
      final var result = resolver.findDataSet(testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find Preparation annotation on class");
    }

    /**
     * Verifies that findPreparation returns annotation when parent class has Preparation
     * annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when parent class has Preparation annotation")
    void shouldReturnAnnotation_whenParentClassHasPreparationAnnotation()
        throws NoSuchMethodException {
      // Given
      final var testClass = TestClassInheritsAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      // When
      final var result = resolver.findDataSet(testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find Preparation annotation on parent class");
    }

    /**
     * Verifies that findPreparation returns empty when no annotation found.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when no annotation found")
    void shouldReturnEmpty_whenNoAnnotationFound() throws NoSuchMethodException {
      // Given
      final var testClass = TestClassWithoutAnnotation.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      // When
      final var result = resolver.findDataSet(testMethod, testClass);

      // Then
      assertFalse(result.isPresent(), "should return empty when no Preparation annotation found");
    }

    /**
     * Verifies that findPreparation prioritizes method annotation over class annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should prioritize method annotation over class annotation")
    void shouldPrioritizeMethodAnnotation_overClassAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = TestClassWithBothAnnotations.class;
      final var testMethod = testClass.getDeclaredMethod("testMethodWithPreparation");

      // When
      final var result = resolver.findDataSet(testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find method annotation");
    }
  }

  /** Tests for the findExpectedDataSet() method. */
  @Nested
  @DisplayName("findExpectedDataSet(Method, Class<?>) method")
  class FindExpectationMethod {

    /** Tests for the findExpectation method. */
    FindExpectationMethod() {}

    /**
     * Verifies that findExpectation returns annotation when method has Expectation annotation.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return annotation when method has Expectation annotation")
    void shouldReturnAnnotation_whenMethodHasExpectationAnnotation() throws NoSuchMethodException {
      // Given
      final var testClass = TestClassWithExpectedDataSet.class;
      final var testMethod = testClass.getDeclaredMethod("testMethod");

      // When
      final var result = resolver.findExpectedDataSet(testMethod, testClass);

      // Then
      assertTrue(result.isPresent(), "should find Expectation annotation on method");
    }
  }

  /** Tests for the resolveScenarioNames() method. */
  @Nested
  @DisplayName("resolveScenarioNames(TableSet, Method) method")
  class ResolveScenarioNamesMethod {

    /** Tests for the resolveScenarioNames method. */
    ResolveScenarioNamesMethod() {}

    /**
     * Verifies that resolveScenarioNames returns specified scenario names when provided.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return specified scenario names when provided")
    void shouldReturnSpecifiedScenarioNames_whenProvided() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithScenarioNames.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveScenarioNames(annotation, testMethod);

      // Then
      assertEquals(
          List.of(new ScenarioName("scenario1"), new ScenarioName("scenario2")),
          result,
          "should return specified scenario names");
    }

    /**
     * Verifies that resolveScenarioNames returns method name when scenario names not specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return method name when scenario names not specified")
    void shouldReturnMethodName_whenScenarioNamesNotSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithoutScenarioNames.class.getDeclaredMethod("myTestMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveScenarioNames(annotation, testMethod);

      // Then
      assertEquals(
          List.of(new ScenarioName("myTestMethod")),
          result,
          "should return method name as scenario name");
    }

    /**
     * Verifies that resolveScenarioNames trims and filters empty scenario names.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should trim and filter empty scenario names")
    void shouldTrimAndFilterEmpty_scenarioNames() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithEmptyScenarioNames.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveScenarioNames(annotation, testMethod);

      // Then
      assertEquals(
          List.of(new ScenarioName("testMethod")),
          result,
          "should use method name when only empty strings provided");
    }

    /**
     * Verifies that resolveScenarioNames trims whitespace from scenario names.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should trim whitespace from scenario names")
    void shouldTrimWhitespace_fromScenarioNames() throws NoSuchMethodException {
      // Given
      final var testMethod =
          TestClassWithWhitespaceScenarioNames.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveScenarioNames(annotation, testMethod);

      // Then
      assertEquals(
          List.of(new ScenarioName("scenario1")),
          result,
          "should trim whitespace from scenario names");
    }
  }

  /** Tests for the extractResourceLocation() method. */
  @Nested
  @DisplayName("extractResourceLocation(TableSet) method")
  class ExtractResourceLocationMethod {

    /** Tests for the extractResourceLocation method. */
    ExtractResourceLocationMethod() {}

    /**
     * Verifies that extractResourceLocation returns location when specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return location when specified")
    void shouldReturnLocation_whenSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithResourceLocation.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.extractResourceLocation(annotation);

      // Then
      assertTrue(result.isPresent(), "should be present");
      final var location = result.orElseThrow();
      assertEquals("custom/path", location, "should return custom/path");
    }

    /**
     * Verifies that extractResourceLocation returns empty when not specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty when not specified")
    void shouldReturnEmpty_whenNotSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithoutResourceLocation.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.extractResourceLocation(annotation);

      // Then
      assertFalse(result.isPresent(), "should return empty when resource location not specified");
    }
  }

  /** Tests for the resolveDataSourceName() method. */
  @Nested
  @DisplayName("resolveDataSourceName(TableSet) method")
  class ResolveDataSourceNameMethod {

    /** Tests for the resolveDataSourceName method. */
    ResolveDataSourceNameMethod() {}

    /**
     * Verifies that resolveDataSourceName returns name when specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return name when specified")
    void shouldReturnName_whenSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithDataSourceName.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveDataSourceName(annotation);

      // Then
      assertTrue(result.isPresent(), "should be present");
      final var dataSourceName = result.orElseThrow();
      assertEquals(
          new DataSourceName("customDataSource"), dataSourceName, "should return customDataSource");
    }

    /**
     * Verifies that resolveDataSourceName returns empty when not specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty when not specified")
    void shouldReturnEmpty_whenNotSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithoutDataSourceName.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(DataSet.class).sources()[0];

      // When
      final var result = resolver.resolveDataSourceName(annotation);

      // Then
      assertFalse(result.isPresent(), "should return empty when data source name not specified");
    }
  }

  /** Test class with method-level DataSet annotation. */
  static class TestClassWithMethodAnnotation {
    /** Test constructor. */
    TestClassWithMethodAnnotation() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void testMethod() {}
  }

  /** Test class with class-level DataSet annotation. */
  @DataSet
  static class TestClassWithClassAnnotation {
    /** Test constructor. */
    TestClassWithClassAnnotation() {}

    /** Test method. */
    void testMethod() {}
  }

  /** Parent class with DataSet annotation. */
  @DataSet
  static class ParentClassWithAnnotation {
    /** Test constructor. */
    ParentClassWithAnnotation() {}
  }

  /** Test class that inherits DataSet annotation from parent. */
  static class TestClassInheritsAnnotation extends ParentClassWithAnnotation {
    /** Test constructor. */
    TestClassInheritsAnnotation() {}

    /** Test method. */
    void testMethod() {}
  }

  /** Test class without any annotation. */
  static class TestClassWithoutAnnotation {
    /** Test constructor. */
    TestClassWithoutAnnotation() {}

    /** Test method. */
    void testMethod() {}
  }

  /** Test class with both class and method annotations. */
  @DataSet
  static class TestClassWithBothAnnotations {
    /** Test constructor. */
    TestClassWithBothAnnotations() {}

    /** Test method with DataSet annotation. */
    @DataSet
    void testMethodWithPreparation() {}
  }

  /** Test class with ExpectedDataSet annotation. */
  static class TestClassWithExpectedDataSet {
    /** Test constructor. */
    TestClassWithExpectedDataSet() {}

    /** Test method with ExpectedDataSet annotation. */
    @ExpectedDataSet
    void testMethod() {}
  }

  /** Test class with scenario names specified. */
  static class TestClassWithScenarioNames {
    /** Test constructor. */
    TestClassWithScenarioNames() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
    void testMethod() {}
  }

  /** Test class without scenario names. */
  static class TestClassWithoutScenarioNames {
    /** Test constructor. */
    TestClassWithoutScenarioNames() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource)
    void myTestMethod() {}
  }

  /** Test class with empty scenario names. */
  static class TestClassWithEmptyScenarioNames {
    /** Test constructor. */
    TestClassWithEmptyScenarioNames() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource(scenarioNames = {"", " "}))
    void testMethod() {}
  }

  /** Test class with whitespace in scenario names. */
  static class TestClassWithWhitespaceScenarioNames {
    /** Test constructor. */
    TestClassWithWhitespaceScenarioNames() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource(scenarioNames = {" scenario1 "}))
    void testMethod() {}
  }

  /** Test class with resource location specified. */
  static class TestClassWithResourceLocation {
    /** Test constructor. */
    TestClassWithResourceLocation() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource(resourceLocation = "custom/path"))
    void testMethod() {}
  }

  /** Test class without resource location. */
  static class TestClassWithoutResourceLocation {
    /** Test constructor. */
    TestClassWithoutResourceLocation() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource)
    void testMethod() {}
  }

  /** Test class with data source name specified. */
  static class TestClassWithDataSourceName {
    /** Test constructor. */
    TestClassWithDataSourceName() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource(dataSourceName = "customDataSource"))
    void testMethod() {}
  }

  /** Test class without data source name. */
  static class TestClassWithoutDataSourceName {
    /** Test constructor. */
    TestClassWithoutDataSourceName() {}

    /** Test method. */
    @DataSet(sources = @DataSetSource)
    void testMethod() {}
  }

  /** Tests for the resolveExcludeColumns() method. */
  @Nested
  @DisplayName("resolveExcludeColumns(DataSetSource) method")
  class ResolveExcludeColumnsMethod {

    /** Tests for the resolveExcludeColumns method. */
    ResolveExcludeColumnsMethod() {}

    /**
     * Verifies that resolveExcludeColumns returns empty set when not specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty set when excludeColumns not specified")
    void shouldReturnEmptySet_whenExcludeColumnsNotSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithoutExcludeColumns.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveExcludeColumns(annotation);

      // Then
      assertTrue(result.isEmpty(), "should return empty set");
    }

    /**
     * Verifies that resolveExcludeColumns returns uppercase columns when specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return uppercase columns when excludeColumns specified")
    void shouldReturnUppercaseColumns_whenExcludeColumnsSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithExcludeColumns.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveExcludeColumns(annotation);

      // Then
      assertEquals(
          Set.of("CREATED_AT", "UPDATED_AT"), result, "should return uppercase column names");
    }

    /**
     * Verifies that resolveExcludeColumns filters empty strings.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should filter empty strings from excludeColumns")
    void shouldFilterEmptyStrings_fromExcludeColumns() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithEmptyExcludeColumns.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveExcludeColumns(annotation);

      // Then
      assertEquals(Set.of("COLUMN1"), result, "should filter empty strings");
    }
  }

  /** Tests for the resolveColumnStrategies() method. */
  @Nested
  @DisplayName("resolveColumnStrategies(DataSetSource) method")
  class ResolveColumnStrategiesMethod {

    /** Tests for the resolveColumnStrategies method. */
    ResolveColumnStrategiesMethod() {}

    /**
     * Verifies that resolveColumnStrategies returns empty map when not specified.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should return empty map when columnStrategies not specified")
    void shouldReturnEmptyMap_whenColumnStrategiesNotSpecified() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithoutColumnStrategies.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveColumnStrategies(annotation);

      // Then
      assertTrue(result.isEmpty(), "should return empty map");
    }

    /**
     * Verifies that resolveColumnStrategies converts annotations to mappings.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should convert annotations to mappings when columnStrategies specified")
    void shouldConvertAnnotationsToMappings_whenColumnStrategiesSpecified()
        throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithColumnStrategies.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveColumnStrategies(annotation);

      // Then
      assertAll(
          "should have correct mappings",
          () -> assertEquals(2, result.size(), "should have 2 mappings"),
          () -> assertTrue(result.containsKey("EMAIL"), "should contain EMAIL key"),
          () -> assertTrue(result.containsKey("CREATED_AT"), "should contain CREATED_AT key"),
          () -> {
            final var emailMapping = result.get("EMAIL");
            assertNotNull(emailMapping, "EMAIL mapping should not be null");
            assertEquals(
                ComparisonStrategy.CASE_INSENSITIVE,
                emailMapping.strategy(),
                "EMAIL should have CASE_INSENSITIVE strategy");
          },
          () -> {
            final var createdAtMapping = result.get("CREATED_AT");
            assertNotNull(createdAtMapping, "CREATED_AT mapping should not be null");
            assertEquals(
                ComparisonStrategy.IGNORE,
                createdAtMapping.strategy(),
                "CREATED_AT should have IGNORE strategy");
          });
    }

    /**
     * Verifies that resolveColumnStrategies normalizes column names to uppercase.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should normalize column names to uppercase")
    void shouldNormalizeColumnNames_toUppercase() throws NoSuchMethodException {
      // Given
      final var testMethod =
          TestClassWithLowercaseColumnStrategy.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveColumnStrategies(annotation);

      // Then
      assertTrue(
          result.containsKey("LOWERCASE_COLUMN"),
          "should normalize lowercase column name to uppercase");
    }

    /**
     * Verifies that resolveColumnStrategies handles REGEX strategy with pattern.
     *
     * @throws NoSuchMethodException if method cannot be found
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle REGEX strategy with pattern")
    void shouldHandleRegexStrategy_withPattern() throws NoSuchMethodException {
      // Given
      final var testMethod = TestClassWithRegexStrategy.class.getDeclaredMethod("testMethod");
      final var annotation = testMethod.getAnnotation(ExpectedDataSet.class).sources()[0];

      // When
      final var result = resolver.resolveColumnStrategies(annotation);

      // Then
      assertAll(
          "should have correct REGEX mapping",
          () -> assertTrue(result.containsKey("UUID"), "should contain UUID key"),
          () -> {
            final var uuidMapping = result.get("UUID");
            assertNotNull(uuidMapping, "UUID mapping should not be null");
            assertEquals(
                ComparisonStrategy.Type.REGEX,
                uuidMapping.strategy().getType(),
                "should be REGEX type");
          },
          () -> {
            final var uuidMapping = result.get("UUID");
            assertNotNull(uuidMapping, "UUID mapping should not be null");
            assertTrue(uuidMapping.strategy().getPattern().isPresent(), "should have pattern");
          });
    }
  }

  /** Test class without exclude columns. */
  static class TestClassWithoutExcludeColumns {
    /** Test constructor. */
    TestClassWithoutExcludeColumns() {}

    /** Test method. */
    @ExpectedDataSet(sources = @DataSetSource)
    void testMethod() {}
  }

  /** Test class with exclude columns. */
  static class TestClassWithExcludeColumns {
    /** Test constructor. */
    TestClassWithExcludeColumns() {}

    /** Test method. */
    @ExpectedDataSet(sources = @DataSetSource(excludeColumns = {"created_at", "updated_at"}))
    void testMethod() {}
  }

  /** Test class with empty exclude columns. */
  static class TestClassWithEmptyExcludeColumns {
    /** Test constructor. */
    TestClassWithEmptyExcludeColumns() {}

    /** Test method. */
    @ExpectedDataSet(sources = @DataSetSource(excludeColumns = {"column1", "", "  "}))
    void testMethod() {}
  }

  /** Test class without column strategies. */
  static class TestClassWithoutColumnStrategies {
    /** Test constructor. */
    TestClassWithoutColumnStrategies() {}

    /** Test method. */
    @ExpectedDataSet(sources = @DataSetSource)
    void testMethod() {}
  }

  /** Test class with column strategies. */
  static class TestClassWithColumnStrategies {
    /** Test constructor. */
    TestClassWithColumnStrategies() {}

    /** Test method. */
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
                  @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE)
                }))
    void testMethod() {}
  }

  /** Test class with lowercase column strategy. */
  static class TestClassWithLowercaseColumnStrategy {
    /** Test constructor. */
    TestClassWithLowercaseColumnStrategy() {}

    /** Test method. */
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(name = "lowercase_column", strategy = Strategy.STRICT)
                }))
    void testMethod() {}
  }

  /** Test class with REGEX strategy. */
  static class TestClassWithRegexStrategy {
    /** Test constructor. */
    TestClassWithRegexStrategy() {}

    /** Test method. */
    @ExpectedDataSet(
        sources =
            @DataSetSource(
                columnStrategies = {
                  @ColumnStrategy(
                      name = "uuid",
                      strategy = Strategy.REGEX,
                      pattern = "[a-f0-9-]{36}")
                }))
    void testMethod() {}
  }
}
