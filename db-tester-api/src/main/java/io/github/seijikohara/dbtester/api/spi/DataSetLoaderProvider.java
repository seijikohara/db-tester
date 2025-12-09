package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.loader.DataSetLoader;

/**
 * Service Provider Interface for providing the default DataSetLoader implementation.
 *
 * <p>This SPI allows the API module to remain independent of specific implementations while still
 * providing a default dataset loader. The actual implementation is provided by the core module and
 * loaded via {@link java.util.ServiceLoader}.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; the framework uses it internally to
 * load the default loader when no custom loader is specified.
 *
 * @see java.util.ServiceLoader
 * @see DataSetLoader
 */
public interface DataSetLoaderProvider {

  /**
   * Returns the default DataSetLoader implementation.
   *
   * <p>This method is called by the framework when no custom loader is specified in test
   * annotations. The returned loader is responsible for loading datasets based on conventions
   * (e.g., classpath location, file naming patterns).
   *
   * @return the default dataset loader
   */
  DataSetLoader getLoader();
}
