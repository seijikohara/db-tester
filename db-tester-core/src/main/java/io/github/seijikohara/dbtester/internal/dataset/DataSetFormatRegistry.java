package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.api.exception.ConfigurationException;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Registry for dataset format providers with automatic discovery via ServiceLoader.
 *
 * <p>This registry manages the mapping between file extensions and their corresponding {@link
 * DataSetFormatProvider} implementations. Providers are automatically discovered and registered
 * using Java's {@link java.util.ServiceLoader} mechanism, eliminating manual registration.
 *
 * <p>Format providers are discovered automatically at startup through ServiceLoader. Any class
 * implementing {@link DataSetFormatProvider} with proper service configuration will be registered
 * automatically.
 *
 * <p>This class is thread-safe. All registration and lookup operations are protected by concurrent
 * collections.
 *
 * @see DataSetFormatProvider
 * @see java.util.ServiceLoader
 */
public final class DataSetFormatRegistry {

  /** Thread-safe map of normalized file extensions to format providers. */
  private static final ConcurrentMap<FileExtension, DataSetFormatProvider> PROVIDERS =
      new ConcurrentHashMap<>();

  static {
    loadProvidersFromServiceLoader();
  }

  /** Private constructor to prevent instantiation. */
  private DataSetFormatRegistry() {}

  /**
   * Discovers and registers all format providers using ServiceLoader.
   *
   * <p>Loads all {@link DataSetFormatProvider} implementations configured in service configuration
   * files and registers them by their supported file extension.
   */
  private static void loadProvidersFromServiceLoader() {
    ServiceLoader.load(DataSetFormatProvider.class).forEach(DataSetFormatRegistry::register);
  }

  /**
   * Registers a format provider for its supported file extension.
   *
   * <p>Associates the provider's supported file extension with the provider instance. Replaces any
   * existing provider registered for the same extension.
   *
   * @param provider the format provider to register
   * @throws IllegalArgumentException if the file extension format is invalid
   */
  public static void register(final DataSetFormatProvider provider) {
    final var fileExtension = provider.supportedFileExtension();

    PROVIDERS.put(fileExtension, provider);
  }

  /**
   * Retrieves the format provider for the specified file extension.
   *
   * @param fileExtension the file extension
   * @return the format provider registered for the file extension
   * @throws ConfigurationException if no provider is registered for the file extension
   */
  public static DataSetFormatProvider getProvider(final FileExtension fileExtension) {
    return Optional.ofNullable(PROVIDERS.get(fileExtension))
        .orElseThrow(
            () ->
                new ConfigurationException(
                    String.format(
                        "No format provider registered for file extension: %s. Registered file extensions: %s",
                        fileExtension.value(), getSupportedExtensions())));
  }

  /**
   * Retrieves all registered file extensions.
   *
   * <p>Returns file extensions in normalized form (lowercase with leading dot, e.g., ".csv",
   * ".tsv"). Useful for discovering supported file formats.
   *
   * @return immutable set of registered file extensions (normalized with leading dot)
   */
  public static Set<String> getSupportedExtensions() {
    return PROVIDERS.keySet().stream()
        .map(FileExtension::value)
        .collect(Collectors.toUnmodifiableSet());
  }
}
