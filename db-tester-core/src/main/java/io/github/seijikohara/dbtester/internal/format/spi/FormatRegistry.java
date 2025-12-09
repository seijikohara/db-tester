package io.github.seijikohara.dbtester.internal.format.spi;

import io.github.seijikohara.dbtester.api.exception.ConfigurationException;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for format providers with automatic discovery via ServiceLoader.
 *
 * <p>This registry manages the mapping between file extensions and their corresponding {@link
 * FormatProvider} implementations. Providers are automatically discovered and registered using
 * Java's {@link java.util.ServiceLoader} mechanism, eliminating manual registration.
 *
 * <p>Format providers are discovered automatically at class loading time. Any class implementing
 * {@link FormatProvider} with proper service configuration will be registered automatically.
 *
 * <p>This class is thread-safe. All registration and lookup operations use concurrent collections.
 *
 * @see FormatProvider
 * @see java.util.ServiceLoader
 */
public final class FormatRegistry {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(FormatRegistry.class);

  /** Thread-safe map of normalized file extensions to format providers. */
  private static final ConcurrentMap<FileExtension, FormatProvider> PROVIDERS =
      new ConcurrentHashMap<>();

  static {
    loadProvidersFromServiceLoader();
  }

  /**
   * Private constructor to prevent instantiation.
   *
   * <p>This is a utility class with only static methods.
   */
  private FormatRegistry() {}

  /**
   * Discovers and registers all format providers using ServiceLoader.
   *
   * <p>Loads all {@link FormatProvider} implementations configured in service configuration files
   * and registers them by their supported file extension.
   */
  private static void loadProvidersFromServiceLoader() {
    ServiceLoader.load(FormatProvider.class).forEach(FormatRegistry::register);
    logger.debug("Loaded {} format providers: {}", PROVIDERS.size(), getSupportedExtensions());
  }

  /**
   * Registers a format provider for its supported file extension.
   *
   * <p>Associates the provider's supported file extension with the provider instance. Replaces any
   * existing provider registered for the same extension.
   *
   * @param provider the format provider to register
   */
  public static void register(final FormatProvider provider) {
    final var fileExtension = provider.supportedFileExtension();
    PROVIDERS.put(fileExtension, provider);
    logger.debug(
        "Registered format provider: {} for extension: {}",
        provider.getClass().getSimpleName(),
        fileExtension.value());
  }

  /**
   * Retrieves the format provider for the specified file extension.
   *
   * @param fileExtension the file extension
   * @return the format provider registered for the file extension
   * @throws ConfigurationException if no provider is registered for the file extension
   */
  public static FormatProvider getProvider(final FileExtension fileExtension) {
    return Optional.ofNullable(PROVIDERS.get(fileExtension))
        .orElseThrow(
            () ->
                new ConfigurationException(
                    String.format(
                        "No format provider registered for file extension: %s. Registered extensions: %s",
                        fileExtension.value(), getSupportedExtensions())));
  }

  /**
   * Retrieves the format provider for the specified file extension string.
   *
   * <p>Convenience method that accepts a string extension (with or without leading dot).
   *
   * @param extension the file extension string (e.g., "csv", ".tsv")
   * @return the format provider registered for the file extension
   * @throws ConfigurationException if no provider is registered for the file extension
   */
  public static FormatProvider getProvider(final String extension) {
    return getProvider(new FileExtension(extension));
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

  /**
   * Checks if a provider is registered for the specified file extension.
   *
   * @param fileExtension the file extension to check
   * @return true if a provider is registered, false otherwise
   */
  public static boolean hasProvider(final FileExtension fileExtension) {
    return PROVIDERS.containsKey(fileExtension);
  }
}
