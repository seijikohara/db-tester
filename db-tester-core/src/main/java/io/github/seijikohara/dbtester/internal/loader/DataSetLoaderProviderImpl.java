package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.loader.DataSetLoader;
import io.github.seijikohara.dbtester.internal.spi.DataSetLoaderProvider;

/**
 * Implementation of {@link DataSetLoaderProvider} that provides the default dataset loader.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the bridge between the
 * public API and the internal dataset loader implementation.
 */
public final class DataSetLoaderProviderImpl implements DataSetLoaderProvider {

  /** The default dataset loader instance. */
  private static final DataSetLoader DEFAULT_LOADER = new TestClassNameBasedDataSetLoader();

  /** Creates a new instance of the dataset loader provider. */
  public DataSetLoaderProviderImpl() {
    // Default constructor for ServiceLoader
  }

  /**
   * {@inheritDoc}
   *
   * @return the default dataset loader, never null
   */
  @Override
  public DataSetLoader getLoader() {
    return DEFAULT_LOADER;
  }
}
