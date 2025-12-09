package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.domain.DataSourceName;
import io.github.seijikohara.dbtester.api.exception.DataSourceNotFoundException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Mutable registry used to resolve {@link javax.sql.DataSource} instances at runtime.
 *
 * <p>Test suites typically initialise the registry in {@code @BeforeAll} methods by registering the
 * default data source and any named alternatives. Concurrent reads are serviced through a {@link
 * ConcurrentHashMap}, while the default data source is published via a {@code volatile} field.
 */
public final class DataSourceRegistry {

  /** Internal name used for the default data source in the named map. */
  private static final DataSourceName DEFAULT_DATA_SOURCE_NAME =
      new DataSourceName("_defaultDataSource_");

  /** Thread-safe map of named data sources. */
  private final ConcurrentMap<DataSourceName, DataSource> dataSources = new ConcurrentHashMap<>();

  /** The default data source (volatile for thread-safe visibility). */
  private volatile @Nullable DataSource defaultDataSource;

  /**
   * Creates an empty data source registry with no registered data sources.
   *
   * <p>Data sources must be explicitly registered using {@link #registerDefault(DataSource)} or
   * {@link #register(String, DataSource)} before they can be retrieved.
   */
  public DataSourceRegistry() {}

  /**
   * Registers the default data source.
   *
   * <p>This method is a convenience for single-database test scenarios. The specified data source
   * becomes the default and is also registered internally for framework use.
   *
   * <p>This method is thread-safe and atomically updates both the default data source field and the
   * internal registry map.
   *
   * @param dataSource the data source to register as the default
   */
  public synchronized void registerDefault(final DataSource dataSource) {
    this.defaultDataSource = dataSource;
    dataSources.put(DEFAULT_DATA_SOURCE_NAME, dataSource);
  }

  /**
   * Registers a data source with the specified name.
   *
   * <p>If the name is empty or matches the internal default marker, the data source is registered
   * as the default instead of as a named data source.
   *
   * @param name the data source name
   * @param dataSource the data source to register
   */
  public void register(final String name, final DataSource dataSource) {
    if (name.trim().isEmpty() || DEFAULT_DATA_SOURCE_NAME.value().equals(name)) {
      registerDefault(dataSource);
    } else {
      dataSources.put(new DataSourceName(name), dataSource);
    }
  }

  /**
   * Retrieves the default data source.
   *
   * @return the default data source
   * @throws DataSourceNotFoundException if no default data source is registered
   */
  public DataSource getDefault() {
    return Optional.ofNullable(defaultDataSource)
        .orElseThrow(() -> new DataSourceNotFoundException("No default data source registered"));
  }

  /**
   * Retrieves a data source by name, falling back to the default if the name is empty.
   *
   * @param name the data source name, or empty string for default
   * @return the data source
   * @throws DataSourceNotFoundException if the named data source is not found and no default is
   *     registered
   */
  public DataSource get(final @Nullable String name) {
    final var namedDataSource =
        Optional.ofNullable(name)
            .filter(Predicate.not(String::isEmpty))
            .map(DataSourceName::new)
            .flatMap(dataSourceName -> Optional.ofNullable(dataSources.get(dataSourceName)));

    return namedDataSource
        .or(() -> Optional.ofNullable(defaultDataSource))
        .orElseThrow(() -> new DataSourceNotFoundException(buildMissingDataSourceMessage(name)));
  }

  /**
   * Retrieves a data source by name as an Optional.
   *
   * @param name the data source name
   * @return an Optional containing the data source, or empty if not found
   */
  public Optional<DataSource> find(final String name) {
    return Optional.ofNullable(dataSources.get(new DataSourceName(name)));
  }

  /**
   * Checks if a default data source is registered.
   *
   * @return {@code true} if a default data source exists, {@code false} otherwise
   */
  public boolean hasDefault() {
    return Optional.ofNullable(defaultDataSource).isPresent();
  }

  /**
   * Checks if a named data source is registered.
   *
   * @param name the data source name
   * @return {@code true} if the named data source exists, {@code false} otherwise
   */
  public boolean has(final String name) {
    return dataSources.containsKey(new DataSourceName(name));
  }

  /**
   * Removes all registered data sources including the default.
   *
   * <p>This method is useful for test cleanup or resetting the registry state.
   *
   * <p>This method is thread-safe and atomically clears both the registry map and the default data
   * source field.
   */
  public synchronized void clear() {
    dataSources.clear();
    defaultDataSource = null;
  }

  /**
   * Builds an error message for missing data source scenarios.
   *
   * @param name the data source name that was not found (nullable)
   * @return the formatted error message
   */
  private String buildMissingDataSourceMessage(final @Nullable String name) {
    return Optional.ofNullable(name)
        .filter(Predicate.not(String::isEmpty))
        .map(
            dataSourceName ->
                String.format("No data source registered for name: %s", dataSourceName))
        .orElse("No default data source registered");
  }
}
