package io.github.seijikohara.dbtester.spring.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

/** Unit tests for {@link DataSourceRegistrarSupport}. */
@DisplayName("DataSourceRegistrarSupport")
class DataSourceRegistrarSupportTest {

  /** Tests for DataSourceRegistrarSupport. */
  DataSourceRegistrarSupportTest() {}

  /** Mock registry for tests. */
  private DataSourceRegistry registry;

  /** Mock logger for tests. */
  private Logger logger;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    registry = mock(DataSourceRegistry.class);
    logger = mock(Logger.class);
  }

  /** Tests for the registerDataSources method. */
  @Nested
  @DisplayName("registerDataSources method")
  class RegisterDataSourcesMethod {

    /** Tests for registerDataSources. */
    RegisterDataSourcesMethod() {}

    /** Verifies single DataSource is registered as default. */
    @Test
    @Tag("normal")
    @DisplayName("should register single DataSource as default")
    void shouldRegisterSingleDataSourceAsDefault() {
      // Given
      final var dataSource = mock(DataSource.class);
      final Map<String, DataSource> dataSources = Map.of("primary", dataSource);
      final Predicate<String> neverPrimary = name -> false;

      // When
      DataSourceRegistrarSupport.registerDataSources(registry, dataSources, neverPrimary, logger);

      // Then
      verify(registry).register("primary", dataSource);
      verify(registry).registerDefault(dataSource);
    }

    /** Verifies all DataSources are registered by name. */
    @Test
    @Tag("normal")
    @DisplayName("should register all DataSources by name")
    void shouldRegisterAllDataSourcesByName() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final var ds3 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("dataSource", ds1);
      dataSources.put("secondary", ds2);
      dataSources.put("tertiary", ds3);
      final Predicate<String> neverPrimary = name -> false;

      // When
      DataSourceRegistrarSupport.registerDataSources(registry, dataSources, neverPrimary, logger);

      // Then
      verify(registry).register("dataSource", ds1);
      verify(registry).register("secondary", ds2);
      verify(registry).register("tertiary", ds3);
      verify(registry).registerDefault(ds1); // Falls back to "dataSource" name
    }

    /** Verifies primary DataSource is registered as default. */
    @Test
    @Tag("normal")
    @DisplayName("should register primary DataSource as default")
    void shouldRegisterPrimaryDataSourceAsDefault() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("primary", ds2);
      final Predicate<String> isPrimary = name -> "primary".equals(name);

      // When
      DataSourceRegistrarSupport.registerDataSources(registry, dataSources, isPrimary, logger);

      // Then
      verify(registry).register("first", ds1);
      verify(registry).register("primary", ds2);
      verify(registry).registerDefault(ds2);
    }

    /** Verifies no default is registered when no match found. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not register default when no match found")
    void shouldNotRegisterDefaultWhenNoMatchFound() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("second", ds2);
      final Predicate<String> neverPrimary = name -> false;

      // When
      DataSourceRegistrarSupport.registerDataSources(registry, dataSources, neverPrimary, logger);

      // Then
      verify(registry).register("first", ds1);
      verify(registry).register("second", ds2);
      verify(registry, never()).registerDefault(any());
    }
  }

  /** Tests for the resolveDefaultDataSource method. */
  @Nested
  @DisplayName("resolveDefaultDataSource method")
  class ResolveDefaultDataSourceMethod {

    /** Tests for resolveDefaultDataSource. */
    ResolveDefaultDataSourceMethod() {}

    /** Verifies single DataSource is returned as default. */
    @Test
    @Tag("normal")
    @DisplayName("should return single DataSource as default")
    void shouldReturnSingleDataSourceAsDefault() {
      // Given
      final var dataSource = mock(DataSource.class);
      final Map<String, DataSource> dataSources = Map.of("only", dataSource);
      final Predicate<String> neverPrimary = name -> false;

      // When
      final var result =
          DataSourceRegistrarSupport.resolveDefaultDataSource(dataSources, neverPrimary);

      // Then
      assertTrue(result.isPresent());
      assertEquals("only", result.get().getKey());
      assertEquals(dataSource, result.get().getValue());
    }

    /** Verifies primary DataSource is preferred. */
    @Test
    @Tag("normal")
    @DisplayName("should return primary DataSource when multiple exist")
    void shouldReturnPrimaryDataSourceWhenMultipleExist() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("primary", ds2);
      final Predicate<String> isPrimary = name -> "primary".equals(name);

      // When
      final var result =
          DataSourceRegistrarSupport.resolveDefaultDataSource(dataSources, isPrimary);

      // Then
      assertTrue(result.isPresent());
      assertEquals("primary", result.get().getKey());
      assertEquals(ds2, result.get().getValue());
    }

    /** Verifies fallback to "dataSource" bean name. */
    @Test
    @Tag("normal")
    @DisplayName("should fallback to dataSource bean name")
    void shouldFallbackToDataSourceBeanName() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("dataSource", ds1);
      dataSources.put("other", ds2);
      final Predicate<String> neverPrimary = name -> false;

      // When
      final var result =
          DataSourceRegistrarSupport.resolveDefaultDataSource(dataSources, neverPrimary);

      // Then
      assertTrue(result.isPresent());
      assertEquals("dataSource", result.get().getKey());
      assertEquals(ds1, result.get().getValue());
    }

    /** Verifies empty when no match. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when no default can be resolved")
    void shouldReturnEmptyWhenNoDefaultCanBeResolved() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("second", ds2);
      final Predicate<String> neverPrimary = name -> false;

      // When
      final var result =
          DataSourceRegistrarSupport.resolveDefaultDataSource(dataSources, neverPrimary);

      // Then
      assertFalse(result.isPresent());
    }
  }

  /** Tests for the findPrimaryDataSource method. */
  @Nested
  @DisplayName("findPrimaryDataSource method")
  class FindPrimaryDataSourceMethod {

    /** Tests for findPrimaryDataSource. */
    FindPrimaryDataSourceMethod() {}

    /** Verifies primary is found. */
    @Test
    @Tag("normal")
    @DisplayName("should find primary DataSource")
    void shouldFindPrimaryDataSource() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("primary", ds2);
      final Predicate<String> isPrimary = name -> "primary".equals(name);

      // When
      final var result = DataSourceRegistrarSupport.findPrimaryDataSource(dataSources, isPrimary);

      // Then
      assertTrue(result.isPresent());
      assertEquals("primary", result.get().getKey());
    }

    /** Verifies empty when no primary. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when no primary exists")
    void shouldReturnEmptyWhenNoPrimaryExists() {
      // Given
      final var ds1 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = Map.of("only", ds1);
      final Predicate<String> neverPrimary = name -> false;

      // When
      final var result = DataSourceRegistrarSupport.findPrimaryDataSource(dataSources, neverPrimary);

      // Then
      assertFalse(result.isPresent());
    }
  }

  /** Tests for the findDataSourceByName method. */
  @Nested
  @DisplayName("findDataSourceByName method")
  class FindDataSourceByNameMethod {

    /** Tests for findDataSourceByName. */
    FindDataSourceByNameMethod() {}

    /** Verifies DataSource is found by name. */
    @Test
    @Tag("normal")
    @DisplayName("should find DataSource by name")
    void shouldFindDataSourceByName() {
      // Given
      final var ds1 = mock(DataSource.class);
      final var ds2 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = new LinkedHashMap<>();
      dataSources.put("first", ds1);
      dataSources.put("target", ds2);

      // When
      final var result = DataSourceRegistrarSupport.findDataSourceByName(dataSources, "target");

      // Then
      assertTrue(result.isPresent());
      assertEquals("target", result.get().getKey());
      assertEquals(ds2, result.get().getValue());
    }

    /** Verifies empty when name not found. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when name not found")
    void shouldReturnEmptyWhenNameNotFound() {
      // Given
      final var ds1 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = Map.of("only", ds1);

      // When
      final var result = DataSourceRegistrarSupport.findDataSourceByName(dataSources, "missing");

      // Then
      assertFalse(result.isPresent());
    }

    /** Verifies empty when name is null. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when name is null")
    void shouldReturnEmptyWhenNameIsNull() {
      // Given
      final var ds1 = mock(DataSource.class);
      final Map<String, DataSource> dataSources = Map.of("only", ds1);

      // When
      final var result = DataSourceRegistrarSupport.findDataSourceByName(dataSources, null);

      // Then
      assertFalse(result.isPresent());
    }
  }
}
