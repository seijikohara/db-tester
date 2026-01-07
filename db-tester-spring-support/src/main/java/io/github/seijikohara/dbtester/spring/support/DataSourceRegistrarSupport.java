package io.github.seijikohara.dbtester.spring.support;

import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Common support class for DataSource registration in Spring environments.
 *
 * <p>This utility class provides shared logic for registering Spring-managed {@link DataSource}
 * beans with a {@link DataSourceRegistry}. It is designed to be used by Spring Boot starter modules
 * to eliminate code duplication.
 *
 * <p>The class provides methods for:
 *
 * <ul>
 *   <li>Registering all DataSources by name
 *   <li>Resolving and registering the default DataSource
 *   <li>Finding primary or named DataSources
 * </ul>
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see DataSourceRegistry
 */
public final class DataSourceRegistrarSupport {

  /** Default bean name used as fallback when no primary DataSource is found. */
  public static final String DEFAULT_DATASOURCE_BEAN_NAME = "dataSource";

  /** Prevents instantiation of this utility class. */
  private DataSourceRegistrarSupport() {
    // Utility class - prevent instantiation
  }

  /**
   * Registers all DataSources with the registry.
   *
   * <p>This method performs the following:
   *
   * <ol>
   *   <li>Registers each DataSource by its bean name
   *   <li>Resolves the default DataSource using the priority rules
   *   <li>Registers the default DataSource if found
   * </ol>
   *
   * @param registry the registry to populate
   * @param dataSources the map of bean names to DataSource instances
   * @param isPrimaryPredicate predicate to check if a bean name is marked as primary
   * @param logger the logger for registration messages
   */
  public static void registerDataSources(
      final DataSourceRegistry registry,
      final Map<String, DataSource> dataSources,
      final Predicate<String> isPrimaryPredicate,
      final Logger logger) {

    logger.info("Registering {} DataSource(s) with DataSourceRegistry", dataSources.size());

    // Register each DataSource by name
    dataSources.forEach(
        (name, dataSource) -> {
          registry.register(name, dataSource);
          logger.debug("Registered DataSource '{}' with registry", name);
        });

    // Register default DataSource
    resolveDefaultDataSource(dataSources, isPrimaryPredicate)
        .ifPresent(
            entry -> {
              registry.registerDefault(entry.getValue());
              logger.info("Registered DataSource '{}' as default", entry.getKey());
            });
  }

  /**
   * Resolves the default DataSource from the discovered DataSources.
   *
   * <p>Resolution priority:
   *
   * <ol>
   *   <li>Single DataSource (automatic default)
   *   <li>Primary-annotated DataSource
   *   <li>DataSource named "dataSource"
   * </ol>
   *
   * @param dataSources the map of discovered DataSources
   * @param isPrimaryPredicate predicate to check if a bean name is marked as primary
   * @return an Optional containing the default DataSource entry, or empty if none found
   */
  public static Optional<Map.Entry<String, DataSource>> resolveDefaultDataSource(
      final Map<String, DataSource> dataSources, final Predicate<String> isPrimaryPredicate) {

    // Single DataSource is automatically the default
    if (dataSources.size() == 1) {
      return Optional.of(dataSources.entrySet().iterator().next());
    }

    // Find primary DataSource
    return findPrimaryDataSource(dataSources, isPrimaryPredicate)
        .or(() -> findDataSourceByName(dataSources, DEFAULT_DATASOURCE_BEAN_NAME));
  }

  /**
   * Finds the primary DataSource from the map.
   *
   * @param dataSources the map of discovered DataSources
   * @param isPrimaryPredicate predicate to check if a bean name is marked as primary
   * @return an Optional containing the primary DataSource entry, or empty if none found
   */
  public static Optional<Map.Entry<String, DataSource>> findPrimaryDataSource(
      final Map<String, DataSource> dataSources, final Predicate<String> isPrimaryPredicate) {

    return dataSources.entrySet().stream()
        .filter(entry -> isPrimaryPredicate.test(entry.getKey()))
        .findFirst();
  }

  /**
   * Finds a DataSource by its bean name.
   *
   * @param dataSources the map of discovered DataSources
   * @param beanName the bean name to search for
   * @return an Optional containing the matching DataSource entry, or empty if not found
   */
  public static Optional<Map.Entry<String, DataSource>> findDataSourceByName(
      final Map<String, DataSource> dataSources, final @Nullable String beanName) {

    if (beanName == null) {
      return Optional.empty();
    }

    return dataSources.entrySet().stream()
        .filter(entry -> entry.getKey().equals(beanName))
        .findFirst();
  }
}
