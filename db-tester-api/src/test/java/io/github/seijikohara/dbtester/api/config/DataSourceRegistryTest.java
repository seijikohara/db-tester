package io.github.seijikohara.dbtester.api.config;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.exception.DataSourceNotFoundException;
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

  /** The registry under test. */
  private DataSourceRegistry registry;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    registry = new DataSourceRegistry();
  }

  /** Tests for registerDefault method. */
  @Nested
  @DisplayName("registerDefault")
  class RegisterDefaultTest {

    /** Tests for registerDefault method. */
    RegisterDefaultTest() {}

    /** Verifies that registerDefault registers default data source. */
    @Test
    @Tag("normal")
    @DisplayName("registers default data source")
    void registersDefaultDataSource() {
      final var dataSource = mock(DataSource.class);

      registry.registerDefault(dataSource);

      assertTrue(registry.hasDefault());
      assertEquals(dataSource, registry.getDefault());
    }

    /** Verifies that registerDefault replaces existing default data source. */
    @Test
    @Tag("normal")
    @DisplayName("replaces existing default data source")
    void replacesExistingDefaultDataSource() {
      final var dataSource1 = mock(DataSource.class);
      final var dataSource2 = mock(DataSource.class);

      registry.registerDefault(dataSource1);
      registry.registerDefault(dataSource2);

      assertEquals(dataSource2, registry.getDefault());
    }
  }

  /** Tests for register(String, DataSource) method. */
  @Nested
  @DisplayName("register(String, DataSource)")
  class RegisterNamedTest {

    /** Tests for register(String, DataSource) method. */
    RegisterNamedTest() {}

    /** Verifies that register registers named data source. */
    @Test
    @Tag("normal")
    @DisplayName("registers named data source")
    void registersNamedDataSource() {
      final var dataSource = mock(DataSource.class);

      registry.register("primary", dataSource);

      assertTrue(registry.has("primary"));
      assertEquals(dataSource, registry.get("primary"));
    }

    /** Verifies that register registers as default when name is empty. */
    @Test
    @Tag("normal")
    @DisplayName("registers as default when name is empty")
    void registersAsDefaultWhenNameIsEmpty() {
      final var dataSource = mock(DataSource.class);

      registry.register("", dataSource);

      assertTrue(registry.hasDefault());
      assertEquals(dataSource, registry.getDefault());
    }

    /** Verifies that register registers as default when name is blank. */
    @Test
    @Tag("normal")
    @DisplayName("registers as default when name is blank")
    void registersAsDefaultWhenNameIsBlank() {
      final var dataSource = mock(DataSource.class);

      registry.register("   ", dataSource);

      assertTrue(registry.hasDefault());
    }

    /** Verifies that register replaces existing named data source. */
    @Test
    @Tag("normal")
    @DisplayName("replaces existing named data source")
    void replacesExistingNamedDataSource() {
      final var dataSource1 = mock(DataSource.class);
      final var dataSource2 = mock(DataSource.class);

      registry.register("db", dataSource1);
      registry.register("db", dataSource2);

      assertEquals(dataSource2, registry.get("db"));
    }
  }

  /** Tests for getDefault method. */
  @Nested
  @DisplayName("getDefault")
  class GetDefaultTest {

    /** Tests for getDefault method. */
    GetDefaultTest() {}

    /** Verifies that getDefault returns default data source when registered. */
    @Test
    @Tag("normal")
    @DisplayName("returns default data source when registered")
    void returnsDefaultDataSourceWhenRegistered() {
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      final var result = registry.getDefault();

      assertEquals(dataSource, result);
    }

    /** Verifies that getDefault throws exception when no default registered. */
    @Test
    @Tag("exceptional")
    @DisplayName("throws exception when no default registered")
    void throwsExceptionWhenNoDefaultRegistered() {
      final var exception =
          assertThrows(DataSourceNotFoundException.class, () -> registry.getDefault());

      assertTrue(requireNonNull(exception.getMessage()).contains("No default data source"));
    }
  }

  /** Tests for get(String) method. */
  @Nested
  @DisplayName("get(String)")
  class GetByNameTest {

    /** Tests for get(String) method. */
    GetByNameTest() {}

    /** Verifies that get returns named data source when found. */
    @Test
    @Tag("normal")
    @DisplayName("returns named data source when found")
    void returnsNamedDataSourceWhenFound() {
      final var dataSource = mock(DataSource.class);
      registry.register("mydb", dataSource);

      final var result = registry.get("mydb");

      assertEquals(dataSource, result);
    }

    /** Verifies that get returns default when name is null. */
    @Test
    @Tag("normal")
    @DisplayName("returns default when name is null")
    void returnsDefaultWhenNameIsNull() {
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      final var result = registry.get(null);

      assertEquals(dataSource, result);
    }

    /** Verifies that get returns default when name is empty. */
    @Test
    @Tag("normal")
    @DisplayName("returns default when name is empty")
    void returnsDefaultWhenNameIsEmpty() {
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(dataSource);

      final var result = registry.get("");

      assertEquals(dataSource, result);
    }

    /** Verifies that get throws exception when named not found and no default. */
    @Test
    @Tag("exceptional")
    @DisplayName("throws exception when named not found and no default")
    void throwsExceptionWhenNamedNotFoundAndNoDefault() {
      final var exception =
          assertThrows(DataSourceNotFoundException.class, () -> registry.get("nonexistent"));

      assertTrue(requireNonNull(exception.getMessage()).contains("nonexistent"));
    }

    /** Verifies that get throws exception when null name and no default. */
    @Test
    @Tag("exceptional")
    @DisplayName("throws exception when null name and no default")
    void throwsExceptionWhenNullNameAndNoDefault() {
      final var exception =
          assertThrows(DataSourceNotFoundException.class, () -> registry.get(null));

      assertTrue(requireNonNull(exception.getMessage()).contains("No default data source"));
    }
  }

  /** Tests for find(String) method. */
  @Nested
  @DisplayName("find(String)")
  class FindTest {

    /** Tests for find(String) method. */
    FindTest() {}

    /** Verifies that find returns Optional with data source when found. */
    @Test
    @Tag("normal")
    @DisplayName("returns Optional with data source when found")
    void returnsOptionalWithDataSourceWhenFound() {
      final var dataSource = mock(DataSource.class);
      registry.register("testdb", dataSource);

      final var result = registry.find("testdb");

      assertTrue(result.isPresent());
      assertEquals(dataSource, result.get());
    }

    /** Verifies that find returns empty Optional when not found. */
    @Test
    @Tag("normal")
    @DisplayName("returns empty Optional when not found")
    void returnsEmptyOptionalWhenNotFound() {
      final var result = registry.find("nonexistent");

      assertTrue(result.isEmpty());
    }
  }

  /** Tests for hasDefault method. */
  @Nested
  @DisplayName("hasDefault")
  class HasDefaultTest {

    /** Tests for hasDefault method. */
    HasDefaultTest() {}

    /** Verifies that hasDefault returns true when default exists. */
    @Test
    @Tag("normal")
    @DisplayName("returns true when default exists")
    void returnsTrueWhenDefaultExists() {
      registry.registerDefault(mock(DataSource.class));

      assertTrue(registry.hasDefault());
    }

    /** Verifies that hasDefault returns false when no default. */
    @Test
    @Tag("normal")
    @DisplayName("returns false when no default")
    void returnsFalseWhenNoDefault() {
      assertFalse(registry.hasDefault());
    }
  }

  /** Tests for has(String) method. */
  @Nested
  @DisplayName("has(String)")
  class HasNamedTest {

    /** Tests for has(String) method. */
    HasNamedTest() {}

    /** Verifies that has returns true when named data source exists. */
    @Test
    @Tag("normal")
    @DisplayName("returns true when named data source exists")
    void returnsTrueWhenNamedDataSourceExists() {
      registry.register("db1", mock(DataSource.class));

      assertTrue(registry.has("db1"));
    }

    /** Verifies that has returns false when named data source does not exist. */
    @Test
    @Tag("normal")
    @DisplayName("returns false when named data source does not exist")
    void returnsFalseWhenNamedDataSourceDoesNotExist() {
      assertFalse(registry.has("nonexistent"));
    }
  }

  /** Tests for clear method. */
  @Nested
  @DisplayName("clear")
  class ClearTest {

    /** Tests for clear method. */
    ClearTest() {}

    /** Verifies that clear removes all registered data sources. */
    @Test
    @Tag("normal")
    @DisplayName("removes all registered data sources")
    void removesAllRegisteredDataSources() {
      registry.registerDefault(mock(DataSource.class));
      registry.register("db1", mock(DataSource.class));
      registry.register("db2", mock(DataSource.class));

      registry.clear();

      assertFalse(registry.hasDefault());
      assertFalse(registry.has("db1"));
      assertFalse(registry.has("db2"));
    }

    /** Verifies that clear allows registering after clear. */
    @Test
    @Tag("normal")
    @DisplayName("can register after clear")
    void canRegisterAfterClear() {
      final var dataSource = mock(DataSource.class);
      registry.registerDefault(mock(DataSource.class));
      registry.clear();

      registry.registerDefault(dataSource);

      assertTrue(registry.hasDefault());
      assertEquals(dataSource, registry.getDefault());
    }
  }

  /** Tests for constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorTest {

    /** Tests for constructor. */
    ConstructorTest() {}

    /** Verifies that constructor creates empty registry. */
    @Test
    @Tag("normal")
    @DisplayName("creates empty registry")
    void createsEmptyRegistry() {
      final var newRegistry = new DataSourceRegistry();

      assertNotNull(newRegistry);
      assertFalse(newRegistry.hasDefault());
    }
  }
}
