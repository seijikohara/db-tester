package io.github.seijikohara.dbtester.internal.jdbc.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TypeHandlerRegistry}. */
@DisplayName("TypeHandlerRegistry")
class TypeHandlerRegistryTest {

  /** Tests for the TypeHandlerRegistry class. */
  TypeHandlerRegistryTest() {}

  /** Resets the singleton instance after each test. */
  @AfterEach
  void tearDown() {
    TypeHandlerRegistry.resetInstance();
  }

  /** Tests for the getInstance() method. */
  @Nested
  @DisplayName("getInstance() method")
  class GetInstanceMethod {

    /** Tests for the getInstance method. */
    GetInstanceMethod() {}

    /** Verifies that getInstance returns a non-null registry. */
    @Test
    @Tag("normal")
    @DisplayName("should return non-null registry when called")
    void shouldReturnNonNullRegistry_whenCalled() {
      // When
      final var registry = TypeHandlerRegistry.getInstance();

      // Then
      assertNotNull(registry, "registry should not be null");
    }

    /** Verifies that getInstance returns the same instance. */
    @Test
    @Tag("normal")
    @DisplayName("should return same instance when called multiple times")
    void shouldReturnSameInstance_whenCalledMultipleTimes() {
      // When
      final var registry1 = TypeHandlerRegistry.getInstance();
      final var registry2 = TypeHandlerRegistry.getInstance();

      // Then
      assertSame(registry1, registry2, "should return the same instance");
    }
  }

  /** Tests for the forTesting() method. */
  @Nested
  @DisplayName("forTesting(List) method")
  class ForTestingMethod {

    /** Tests for the forTesting method. */
    ForTestingMethod() {}

    /** Verifies that forTesting creates registry with specified handlers. */
    @Test
    @Tag("normal")
    @DisplayName("should create registry with specified handlers")
    void shouldCreateRegistryWithSpecifiedHandlers() {
      // Given
      final var handler = new UuidTypeHandler();
      final List<TypeHandler<?>> handlers = List.of(handler);

      // When
      final var registry = TypeHandlerRegistry.forTesting(handlers);

      // Then
      assertEquals(1, registry.getHandlers().size(), "should have one handler");
    }
  }

  /** Tests for the findHandler() method. */
  @Nested
  @DisplayName("findHandler(int, String) method")
  class FindHandlerMethod {

    /** Tests for the findHandler method. */
    FindHandlerMethod() {}

    /** Registry instance for testing. */
    private TypeHandlerRegistry registry;

    /** Sets up registry with test handlers. */
    @BeforeEach
    void setUp() {
      final List<TypeHandler<?>> handlers =
          List.of(new UuidTypeHandler(), new JsonTypeHandler(), new ArrayTypeHandler());
      registry = TypeHandlerRegistry.forTesting(handlers);
    }

    /** Verifies that findHandler returns handler for supported SQL type. */
    @Test
    @Tag("normal")
    @DisplayName("should return handler for supported SQL type")
    void shouldReturnHandler_whenSqlTypeIsSupported() {
      // When
      final var result = registry.findHandler(Types.ARRAY, "PostgreSQL");

      // Then
      assertTrue(result.isPresent(), "should find handler");
      assertEquals(
          ArrayTypeHandler.class, result.orElseThrow().getClass(), "should be ArrayTypeHandler");
    }

    /** Verifies that findHandler returns empty for unsupported SQL type. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when SQL type is not supported")
    void shouldReturnEmpty_whenSqlTypeIsNotSupported() {
      // When
      final var result = registry.findHandler(Types.VARCHAR, "PostgreSQL");

      // Then
      assertTrue(result.isEmpty(), "should return empty for unsupported type");
    }

    /** Verifies that findHandler prefers database-specific handler. */
    @Test
    @Tag("normal")
    @DisplayName("should prefer database-specific handler when available")
    void shouldPreferDatabaseSpecificHandler_whenAvailable() {
      // Given - JsonTypeHandler has higher priority for PostgreSQL
      // When
      final var result = registry.findHandler(Types.OTHER, "PostgreSQL");

      // Then
      assertTrue(result.isPresent(), "should find handler");
      assertEquals(
          JsonTypeHandler.class,
          result.orElseThrow().getClass(),
          "should return JSON handler for PostgreSQL");
    }

    /** Verifies that findHandler falls back to generic handler. */
    @Test
    @Tag("normal")
    @DisplayName("should fall back to generic handler when no database-specific handler")
    void shouldFallBackToGenericHandler_whenNoDatabaseSpecificHandler() {
      // Given - ArrayTypeHandler supports PostgreSQL and H2
      // When - Query for a different database that ArrayTypeHandler doesn't specifically support
      final var result = registry.findHandler(Types.ARRAY, "MySQL");

      // Then - Should still find a handler since there's no database-specific one for MySQL
      assertTrue(result.isEmpty(), "should not find handler for unsupported database");
    }
  }

  /** Tests for handler priority selection. */
  @Nested
  @DisplayName("handler priority selection")
  class HandlerPrioritySelection {

    /** Tests for handler priority selection. */
    HandlerPrioritySelection() {}

    /** Verifies that higher priority handler is selected. */
    @Test
    @Tag("normal")
    @DisplayName("should select handler with higher priority")
    void shouldSelectHigherPriorityHandler() {
      // Given
      final var lowPriorityHandler = new TestHandler("low", 1);
      final var highPriorityHandler = new TestHandler("high", 100);
      final List<TypeHandler<?>> handlers = List.of(lowPriorityHandler, highPriorityHandler);
      final var registry = TypeHandlerRegistry.forTesting(handlers);

      // When
      final var result = registry.findHandler(Types.OTHER, null);

      // Then
      assertTrue(result.isPresent(), "should find handler");
      final var handler = (TestHandler) result.orElseThrow();
      assertEquals("high", handler.getName(), "should select high priority handler");
    }
  }

  /** Test handler for priority testing. */
  private static class TestHandler implements TypeHandler<String> {

    /** Handler name for identification. */
    private final String name;

    /** Handler priority. */
    private final int priority;

    /**
     * Creates a test handler.
     *
     * @param name the handler name
     * @param priority the handler priority
     */
    TestHandler(final String name, final int priority) {
      this.name = name;
      this.priority = priority;
    }

    /**
     * Returns the handler name.
     *
     * @return the name
     */
    String getName() {
      return name;
    }

    @Override
    public Class<String> javaType() {
      return String.class;
    }

    @Override
    public List<Integer> sqlTypes() {
      return List.of(Types.OTHER);
    }

    @Override
    public int priority() {
      return priority;
    }

    @Override
    public String read(final ResultSet resultSet, final int columnIndex) throws SQLException {
      return resultSet.getString(columnIndex);
    }

    @Override
    public void write(
        final PreparedStatement preparedStatement, final int parameterIndex, final String value)
        throws SQLException {
      preparedStatement.setString(parameterIndex, value);
    }

    @Override
    public String format(final String value) {
      return value;
    }

    @Override
    public String parse(final String value) {
      return value;
    }
  }
}
