package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
import java.util.ServiceLoader;

/**
 * Aggregates the runtime configuration consumed by the database testing extension.
 *
 * <p>A {@code Configuration} ties together three orthogonal aspects:
 *
 * <ul>
 *   <li>{@link ConventionSettings} specify how the extension resolves dataset directories.
 *   <li>{@link OperationDefaults} provide the default database operations for preparation and
 *       expectation phases.
 *   <li>{@link DataSetLoader} describes how datasets are materialised and filtered.
 * </ul>
 *
 * <p>The record is immutable; once created it can safely be shared across threads and reused for
 * the entire lifecycle of a test class.
 *
 * @param conventions resolution rules for locating datasets
 * @param operations default database operations
 * @param loader strategy for constructing datasets
 */
public record Configuration(
    ConventionSettings conventions, OperationDefaults operations, DataSetLoader loader) {

  /** Lazy holder for the default DataSetLoader instance loaded via SPI. */
  private static final class LoaderHolder {
    /** The singleton DataSetLoader instance. */
    private static final DataSetLoader INSTANCE = loadProvider();

    /** Private constructor to prevent instantiation. */
    private LoaderHolder() {}

    /**
     * Loads the DataSetLoader implementation via ServiceLoader.
     *
     * @return the DataSetLoader instance
     */
    private static DataSetLoader loadProvider() {
      return ServiceLoader.load(DataSetLoaderProvider.class)
          .findFirst()
          .map(DataSetLoaderProvider::getLoader)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "No DataSetLoaderProvider implementation found. Add db-tester-core to your classpath."));
    }
  }

  /**
   * Returns a configuration that applies the framework defaults for all components.
   *
   * @return configuration initialised with standard conventions, operations, and loader
   */
  public static Configuration defaults() {
    return new Configuration(
        ConventionSettings.standard(), OperationDefaults.standard(), LoaderHolder.INSTANCE);
  }

  /**
   * Creates a configuration that overrides the convention settings while keeping other components
   * on their defaults.
   *
   * @param conventions convention settings to apply
   * @return configuration composed of the supplied conventions and default operations/loader
   */
  public static Configuration withConventions(final ConventionSettings conventions) {
    return new Configuration(conventions, OperationDefaults.standard(), LoaderHolder.INSTANCE);
  }

  /**
   * Creates a configuration that overrides the default operations while leaving other components on
   * their conventional values.
   *
   * @param operations operation defaults to apply
   * @return configuration composed of standard conventions, the supplied operations, and default
   *     loader
   */
  public static Configuration withOperations(final OperationDefaults operations) {
    return new Configuration(ConventionSettings.standard(), operations, LoaderHolder.INSTANCE);
  }

  /**
   * Creates a configuration that uses a custom dataset loader and default values for the remaining
   * components.
   *
   * @param loader custom dataset loader implementation
   * @return configuration constructed with standard conventions, standard operations, and the
   *     supplied loader
   */
  public static Configuration withLoader(final DataSetLoader loader) {
    return new Configuration(ConventionSettings.standard(), OperationDefaults.standard(), loader);
  }
}
