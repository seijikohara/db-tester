package io.github.seijikohara.dbtester.internal.jdbc.type;

import io.github.seijikohara.dbtester.api.spi.TypeHandler;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for custom type handlers discovered via ServiceLoader.
 *
 * <p>This registry discovers and manages {@link TypeHandler} implementations, selecting the
 * appropriate handler based on SQL type and database product name.
 *
 * <p>Handler selection follows these rules:
 *
 * <ol>
 *   <li>Database-specific handlers are preferred over generic handlers
 *   <li>Among handlers for the same database, higher priority handlers are preferred
 *   <li>If no specific handler is found, falls back to default type conversion
 * </ol>
 *
 * <p>This class is thread-safe and uses lazy initialization with caching.
 */
public final class TypeHandlerRegistry {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TypeHandlerRegistry.class);

  /** All discovered handlers. */
  private final List<TypeHandler<?>> handlers;

  /** Cache for handler lookups by SQL type and database product name. */
  private final Map<CacheKey, Optional<TypeHandler<?>>> cache = new ConcurrentHashMap<>();

  /** Singleton instance. */
  private static volatile @Nullable TypeHandlerRegistry instance;

  /**
   * Creates a new registry and discovers handlers via ServiceLoader.
   *
   * @param handlers the type handlers to register
   */
  TypeHandlerRegistry(final List<TypeHandler<?>> handlers) {
    this.handlers = List.copyOf(handlers);
    logger.debug("Registered {} type handlers", this.handlers.size());
  }

  /**
   * Returns the singleton instance, discovering handlers on first access.
   *
   * @return the registry instance
   */
  public static TypeHandlerRegistry getInstance() {
    var result = instance;
    if (result == null) {
      synchronized (TypeHandlerRegistry.class) {
        result = instance;
        if (result == null) {
          final var discoveredHandlers = discoverHandlers();
          result = new TypeHandlerRegistry(discoveredHandlers);
          instance = result;
        }
      }
    }
    return result;
  }

  /**
   * Discovers type handlers via ServiceLoader.
   *
   * @return list of discovered handlers
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private static List<TypeHandler<?>> discoverHandlers() {
    final ServiceLoader<TypeHandler> loader = ServiceLoader.load(TypeHandler.class);
    final List<TypeHandler<?>> discovered =
        (List<TypeHandler<?>>) (List) loader.stream().map(ServiceLoader.Provider::get).toList();
    logger.debug("Discovered {} type handlers via ServiceLoader", discovered.size());
    return discovered;
  }

  /**
   * Finds the best matching handler for the given SQL type and database.
   *
   * @param sqlType the SQL type code from {@link java.sql.Types}
   * @param databaseProductName the database product name, or null for generic lookup
   * @return the best matching handler, or empty if none found
   */
  public Optional<TypeHandler<?>> findHandler(
      final int sqlType, final @Nullable String databaseProductName) {
    final var key = new CacheKey(sqlType, databaseProductName);
    return cache.computeIfAbsent(
        key, k -> findHandlerInternal(k.sqlType(), k.databaseProductName()));
  }

  /**
   * Internal handler lookup with priority-based selection.
   *
   * @param sqlType the SQL type code
   * @param databaseProductName the database product name
   * @return the best matching handler
   */
  private Optional<TypeHandler<?>> findHandlerInternal(
      final int sqlType, final @Nullable String databaseProductName) {
    // Find all handlers that support this SQL type
    final var candidates = handlers.stream().filter(h -> h.sqlTypes().contains(sqlType)).toList();

    if (candidates.isEmpty()) {
      return Optional.empty();
    }

    // If database product name is specified, prefer database-specific handlers
    if (databaseProductName != null) {
      final var dbSpecificHandlers =
          candidates.stream()
              .filter(h -> h.supportedDatabases().contains(databaseProductName))
              .max(Comparator.comparingInt(TypeHandler::priority));

      if (dbSpecificHandlers.isPresent()) {
        return dbSpecificHandlers.map(h -> (TypeHandler<?>) h);
      }
    }

    // Fall back to generic handlers (those with empty database list)
    return candidates.stream()
        .filter(h -> h.supportedDatabases().isEmpty())
        .max(Comparator.comparingInt(TypeHandler::priority))
        .map(h -> (TypeHandler<?>) h);
  }

  /**
   * Returns all registered handlers.
   *
   * @return immutable list of handlers
   */
  public List<TypeHandler<?>> getHandlers() {
    return handlers;
  }

  /**
   * Resets the singleton instance.
   *
   * <p>This is primarily useful for testing.
   */
  static void resetInstance() {
    synchronized (TypeHandlerRegistry.class) {
      instance = null;
    }
  }

  /**
   * Creates a registry with explicit handlers for testing.
   *
   * @param handlers the handlers to register
   * @return a new registry instance
   */
  public static TypeHandlerRegistry forTesting(final List<TypeHandler<?>> handlers) {
    return new TypeHandlerRegistry(handlers);
  }

  /**
   * Cache key combining SQL type and database product name.
   *
   * @param sqlType the SQL type code
   * @param databaseProductName the database product name (nullable)
   */
  private record CacheKey(int sqlType, @Nullable String databaseProductName) {}
}
