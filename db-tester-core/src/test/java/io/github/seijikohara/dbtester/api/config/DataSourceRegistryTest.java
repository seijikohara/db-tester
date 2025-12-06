package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.exception.DataSourceNotFoundException;
import java.util.Objects;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DataSourceRegistry}. */
@DisplayName("DataSourceRegistry")
class DataSourceRegistryTest {

  /** Tests for the DataSourceRegistry class. */
  DataSourceRegistryTest() {}

  /** Tests for the registerDefault method. */
  @Nested
  @DisplayName("registerDefault(DataSource) method")
  class RegisterDefaultMethod {

    /** Tests for the registerDefault method. */
    RegisterDefaultMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that registerDefault registers the data source as default. */
    @Test
    @Tag("normal")
    @DisplayName("should register data source as default when called")
    void should_register_data_source_as_default_when_called() {
      // Given
      final var dataSource = mock(DataSource.class);

      // When
      registry.registerDefault(dataSource);

      // Then
      assertAll(
          "should register as default",
          () -> assertTrue(registry.hasDefault(), "should have default"),
          () -> assertSame(dataSource, registry.getDefault(), "should return same instance"));
    }

    /** Verifies that registerDefault overwrites previous default. */
    @Test
    @Tag("normal")
    @DisplayName("should overwrite previous default when called twice")
    void should_overwrite_previous_default_when_called_twice() {
      // Given
      final var firstDataSource = mock(DataSource.class);
      final var secondDataSource = mock(DataSource.class);
      registry.registerDefault(firstDataSource);

      // When
      registry.registerDefault(secondDataSource);

      // Then
      assertAll(
          "should overwrite previous default",
          () -> assertSame(secondDataSource, registry.getDefault(), "should return second"),
          () -> assertNotSame(firstDataSource, registry.getDefault(), "should not return first"));
    }
  }

  /** Tests for the register method. */
  @Nested
  @DisplayName("register(String, DataSource) method")
  class RegisterMethod {

    /** Tests for the register method. */
    RegisterMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that register stores data source with the given name. */
    @Test
    @Tag("normal")
    @DisplayName("should store data source with name when called")
    void should_store_data_source_with_name_when_called() {
      // Given
      final var dataSource = mock(DataSource.class);

      // When
      registry.register("myDb", dataSource);

      // Then
      assertAll(
          "should store with name",
          () -> assertTrue(registry.has("myDb"), "should have named data source"),
          () -> assertSame(dataSource, registry.get("myDb"), "should return same instance"));
    }

    /** Verifies that register with empty name registers as default. */
    @Test
    @Tag("edge-case")
    @DisplayName("should register as default when name is empty")
    void should_register_as_default_when_name_is_empty() {
      // Given
      final var dataSource = mock(DataSource.class);

      // When
      registry.register("", dataSource);

      // Then
      assertAll(
          "should register as default",
          () -> assertTrue(registry.hasDefault(), "should have default"),
          () -> assertSame(dataSource, registry.getDefault(), "should return as default"));
    }

    /** Verifies that register with blank name registers as default. */
    @Test
    @Tag("edge-case")
    @DisplayName("should register as default when name is blank")
    void should_register_as_default_when_name_is_blank() {
      // Given
      final var dataSource = mock(DataSource.class);

      // When
      registry.register("   ", dataSource);

      // Then
      assertTrue(registry.hasDefault(), "should have default after registering with blank name");
    }
  }

  /** Tests for the getDefault method. */
  @Nested
  @DisplayName("getDefault() method")
  class GetDefaultMethod {

    /** Tests for the getDefault method. */
    GetDefaultMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that getDefault returns registered default data source. */
    @Test
    @Tag("normal")
    @DisplayName("should return default data source when registered")
    void should_return_default_data_source_when_registered() {
      // Given
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      // When
      final var result = registry.getDefault();

      // Then
      assertSame(dataSource, result, "should return registered default");
    }

    /** Verifies that getDefault throws exception when no default registered. */
    @Test
    @Tag("error")
    @DisplayName("should throw DataSourceNotFoundException when no default registered")
    void should_throw_exception_when_no_default_registered() {
      // Given - empty registry

      // When & Then
      final var exception =
          assertThrows(
              DataSourceNotFoundException.class,
              () -> registry.getDefault(),
              "should throw DataSourceNotFoundException");
      assertTrue(
          Objects.requireNonNull(exception.getMessage()).contains("No default data source"),
          "message should indicate no default");
    }
  }

  /** Tests for the get method. */
  @Nested
  @DisplayName("get(String) method")
  class GetMethod {

    /** Tests for the get method. */
    GetMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that get returns named data source when it exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return named data source when it exists")
    void should_return_named_data_source_when_it_exists() {
      // Given
      final var dataSource = mock(DataSource.class);
      registry.register("testDb", dataSource);

      // When
      final var result = registry.get("testDb");

      // Then
      assertSame(dataSource, result, "should return named data source");
    }

    /** Verifies that get returns default when name is null. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return default when name is null")
    void should_return_default_when_name_is_null() {
      // Given
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      // When
      final var result = registry.get(null);

      // Then
      assertSame(dataSource, result, "should return default for null name");
    }

    /** Verifies that get returns default when name is empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return default when name is empty")
    void should_return_default_when_name_is_empty() {
      // Given
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      // When
      final var result = registry.get("");

      // Then
      assertSame(dataSource, result, "should return default for empty name");
    }

    /** Verifies that get returns default when named data source not found. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return default when named data source not found")
    void should_return_default_when_named_data_source_not_found() {
      // Given
      final var defaultDs = mock(DataSource.class);
      registry.registerDefault(defaultDs);

      // When
      final var result = registry.get("nonExistent");

      // Then
      assertSame(defaultDs, result, "should fall back to default");
    }

    /** Verifies that get throws exception when no data source found. */
    @Test
    @Tag("error")
    @DisplayName("should throw DataSourceNotFoundException when no data source found")
    void should_throw_exception_when_no_data_source_found() {
      // Given - empty registry

      // When & Then
      final var exception =
          assertThrows(
              DataSourceNotFoundException.class,
              () -> registry.get("nonExistent"),
              "should throw DataSourceNotFoundException");
      assertTrue(
          Objects.requireNonNull(exception.getMessage()).contains("nonExistent"),
          "message should contain the name");
    }
  }

  /** Tests for the find method. */
  @Nested
  @DisplayName("find(String) method")
  class FindMethod {

    /** Tests for the find method. */
    FindMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that find returns Optional with data source when it exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return Optional with data source when it exists")
    void should_return_optional_with_data_source_when_it_exists() {
      // Given
      final var dataSource = mock(DataSource.class);
      registry.register("testDb", dataSource);

      // When
      final var result = registry.find("testDb");

      // Then
      assertAll(
          "should return present Optional",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertSame(dataSource, result.orElseThrow(), "should contain data source"));
    }

    /** Verifies that find returns empty Optional when data source not found. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty Optional when data source not found")
    void should_return_empty_optional_when_data_source_not_found() {
      // Given - empty registry

      // When
      final var result = registry.find("nonExistent");

      // Then
      assertFalse(result.isPresent(), "should return empty Optional");
    }
  }

  /** Tests for the hasDefault method. */
  @Nested
  @DisplayName("hasDefault() method")
  class HasDefaultMethod {

    /** Tests for the hasDefault method. */
    HasDefaultMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that hasDefault returns true when default is registered. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when default is registered")
    void should_return_true_when_default_is_registered() {
      // Given
      registry.registerDefault(mock(DataSource.class));

      // When
      final var result = registry.hasDefault();

      // Then
      assertTrue(result, "should return true");
    }

    /** Verifies that hasDefault returns false when no default registered. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when no default registered")
    void should_return_false_when_no_default_registered() {
      // Given - empty registry

      // When
      final var result = registry.hasDefault();

      // Then
      assertFalse(result, "should return false");
    }
  }

  /** Tests for the has method. */
  @Nested
  @DisplayName("has(String) method")
  class HasMethod {

    /** Tests for the has method. */
    HasMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that has returns true when named data source exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when named data source exists")
    void should_return_true_when_named_data_source_exists() {
      // Given
      registry.register("testDb", mock(DataSource.class));

      // When
      final var result = registry.has("testDb");

      // Then
      assertTrue(result, "should return true");
    }

    /** Verifies that has returns false when named data source does not exist. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when named data source does not exist")
    void should_return_false_when_named_data_source_does_not_exist() {
      // Given - empty registry

      // When
      final var result = registry.has("nonExistent");

      // Then
      assertFalse(result, "should return false");
    }
  }

  /** Tests for the clear method. */
  @Nested
  @DisplayName("clear() method")
  class ClearMethod {

    /** Tests for the clear method. */
    ClearMethod() {}

    /** The registry under test. */
    private DataSourceRegistry registry;

    /** Sets up test fixtures. */
    @BeforeEach
    void setUp() {
      registry = new DataSourceRegistry();
    }

    /** Verifies that clear removes all registered data sources. */
    @Test
    @Tag("normal")
    @DisplayName("should remove all data sources when called")
    void should_remove_all_data_sources_when_called() {
      // Given
      registry.registerDefault(mock(DataSource.class));
      registry.register("db1", mock(DataSource.class));
      registry.register("db2", mock(DataSource.class));

      // When
      registry.clear();

      // Then
      assertAll(
          "should remove all data sources",
          () -> assertFalse(registry.hasDefault(), "should not have default"),
          () -> assertFalse(registry.has("db1"), "should not have db1"),
          () -> assertFalse(registry.has("db2"), "should not have db2"));
    }

    /** Verifies that clear is idempotent on empty registry. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not throw when called on empty registry")
    void should_not_throw_when_called_on_empty_registry() {
      // Given - empty registry

      // When & Then (no exception)
      registry.clear();
      assertFalse(registry.hasDefault(), "should still have no default");
    }
  }
}
