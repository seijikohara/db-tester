package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.format.spi.FormatRegistry;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves directory locations for dataset files.
 *
 * <p>This record handles the resolution of dataset directories from either custom resource
 * locations or convention-based paths. It supports both classpath and file system locations,
 * providing detailed error messages when directories cannot be found.
 *
 * <p>The resolver supports three resolution modes:
 *
 * <ul>
 *   <li><b>Custom location</b>: When a resource location is explicitly provided via annotation
 *   <li><b>Base directory</b>: When {@code baseDirectory} is configured in {@link
 *       io.github.seijikohara.dbtester.api.config.ConventionSettings}, datasets are resolved
 *       relative to that directory. If the base directory starts with {@code classpath:}, it is
 *       resolved as a classpath resource; otherwise, it is resolved as a file system path.
 *   <li><b>Convention-based</b>: When neither custom location nor base directory is provided, a
 *       path is constructed based on the test class package and name
 * </ul>
 *
 * <p>Supported location formats include classpath ({@code classpath:com/example/TestClass/}) and
 * file system ({@code /absolute/path/to/data/}).
 *
 * <p>For convention-based resolution, the path is constructed from the test class name. For
 * example, for test class {@code com.example.service.UserServiceTest}, the base path would be
 * {@code classpath:com/example/service/UserServiceTest/}.
 *
 * <p>The resolver validates that the resolved directory exists, points to a directory (not a file),
 * and contains at least one supported data file.
 *
 * <p>This record is immutable and thread-safe.
 *
 * @param testClass the test class used for convention-based path construction
 * @param testMethodName the test method name for error message context
 * @param baseDirectory the base directory for dataset resolution, or {@code null} for
 *     convention-based resolution
 * @see TestClassNameBasedDataSetLoader
 * @see io.github.seijikohara.dbtester.api.config.ConventionSettings#baseDirectory()
 */
record DirectoryResolver(
    Class<?> testClass, String testMethodName, @Nullable String baseDirectory) {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DirectoryResolver.class);

  /** Prefix for classpath-based resource locations. */
  private static final String CLASSPATH_PREFIX = "classpath:";

  /** Documentation URL for dataset file setup guidance. */
  private static final String DATASET_DOCS_URL =
      "https://seijikohara.github.io/db-tester/getting-started";

  /**
   * Resolves a directory from a resource location or convention-based path.
   *
   * <p>If a custom resource location is provided (non-null), it is used. Otherwise, a
   * convention-based path is constructed from the test class name and suffix.
   *
   * @param resourceLocation the custom resource location, or {@code null} for convention-based
   *     resolution
   * @param suffix the directory suffix to append, or {@code null} for no suffix
   * @return the resolved directory path containing dataset files
   * @throws DataSetLoadException if the directory cannot be found or is invalid
   */
  Path resolveDirectory(final @Nullable String resourceLocation, final @Nullable String suffix) {
    logger.debug("Resolving dataset directory");
    logger.debug("  Test class: {}", testClass.getName());
    logger.debug("  Base directory: {}", baseDirectory);
    logger.debug("  Resource location: {}", resourceLocation);
    logger.debug("  Suffix: {}", suffix);
    final var effectiveLocation = determineEffectiveLocation(resourceLocation, suffix);
    final var directory = createDirectoryFromLocation(effectiveLocation);
    logger.debug("  Resolved path: {}", directory);
    return directory;
  }

  /**
   * Validates that a directory contains at least one supported dataset file.
   *
   * @param directory the directory path to validate
   * @throws IllegalStateException if the directory contains no supported files
   */
  void validateDirectoryContainsSupportedFiles(final Path directory) {
    final var supportedFileExtensions = FormatRegistry.getSupportedExtensions();
    if (supportedFileExtensions.isEmpty()) {
      throw new IllegalStateException(
          """
          No format providers are registered. Register a FormatProvider implementation before executing database tests.""");
    }

    try (final var files = Files.list(directory)) {
      final var hasSupportedFiles =
          files
              .filter(Files::isRegularFile)
              .anyMatch(path -> hasSupportedFileExtension(path, supportedFileExtensions));

      if (!hasSupportedFiles) {
        final var exampleExtension = supportedFileExtensions.stream().findFirst().orElse("");
        final var message =
            String.format(
                """
                Dataset directory exists but contains no supported data files for '%s.%s': '%s'

                Supported file extensions: %s

                To fix:
                  1. Add at least one data file (e.g., TABLE_NAME%s) to this directory
                  2. Or register a format provider for the desired file extension

                See: %s""",
                testClass.getSimpleName(),
                testMethodName,
                directory.toAbsolutePath(),
                supportedFileExtensions,
                exampleExtension,
                DATASET_DOCS_URL);
        throw new IllegalStateException(message);
      }
    } catch (final IOException e) {
      throw new DataSetLoadException(
          String.format("Failed to list files in directory: %s", directory), e);
    }
  }

  /**
   * Determines the effective location to use for dataset resolution.
   *
   * <p>If a custom resource location is provided (non-null and non-empty), it is used. Otherwise, a
   * convention-based path is constructed from the test class name.
   *
   * @param resourceLocation the custom resource location from the annotation
   * @param suffix the directory suffix, or {@code null} for no suffix
   * @return the effective location string
   */
  private String determineEffectiveLocation(
      final @Nullable String resourceLocation, final @Nullable String suffix) {
    return Optional.ofNullable(resourceLocation)
        .filter(Predicate.not(String::isEmpty))
        .orElseGet(() -> createConventionBasedPath(suffix));
  }

  /**
   * Creates a Path object from a location string.
   *
   * <p>This method determines whether the location is a classpath resource or a file system path
   * based on the "classpath:" prefix, and delegates to the appropriate resolution method.
   *
   * @param location the location string (either classpath or file system)
   * @return the resolved directory Path object
   * @throws DataSetLoadException if the directory cannot be found or is invalid
   */
  private Path createDirectoryFromLocation(final String location) {
    return location.startsWith(CLASSPATH_PREFIX)
        ? resolveClasspathDirectory(location)
        : resolveFileSystemDirectory(location);
  }

  /**
   * Resolves a directory from a classpath resource location.
   *
   * <p>This method strips the "classpath:" prefix and uses the class loader to locate the resource
   * on the classpath.
   *
   * @param location the classpath location (e.g., "classpath:com/example/Test/")
   * @return the resolved directory Path object
   * @throws DataSetLoadException if the resource cannot be found on the classpath
   */
  private Path resolveClasspathDirectory(final String location) {
    final var resourcePath = location.substring(CLASSPATH_PREFIX.length());
    return Optional.ofNullable(testClass().getClassLoader().getResource(resourcePath))
        .map(
            resourceUrl -> {
              try {
                return Path.of(resourceUrl.toURI());
              } catch (final URISyntaxException e) {
                throw new DataSetLoadException(
                    String.format("Failed to convert classpath resource to Path: %s", resourceUrl),
                    e);
              }
            })
        .orElseThrow(
            () -> {
              final var expectedLocation = String.format("src/test/resources/%s", resourcePath);
              final var message =
                  String.format(
                      """
                      Dataset directory not found for test method '%s.%s'

                      Searched location: classpath:%s
                      Expected location: %s

                      To fix:
                        1. Create the directory: %s
                        2. Add dataset files (e.g., TABLE_NAME.csv) with table data
                        3. Or specify an explicit location: @DataSetSource(resourceLocation = "...")

                      See: %s""",
                      testClass.getSimpleName(),
                      testMethodName,
                      resourcePath,
                      expectedLocation,
                      expectedLocation,
                      DATASET_DOCS_URL);
              return new DataSetLoadException(message);
            });
  }

  /**
   * Resolves a directory from a file system path.
   *
   * <p>This method verifies that the specified path exists and is a directory.
   *
   * @param location the file system path (absolute or relative)
   * @return the directory Path object
   * @throws DataSetLoadException if the path does not exist or is not a directory
   */
  private Path resolveFileSystemDirectory(final String location) {
    final var path = Path.of(location);

    if (!Files.exists(path)) {
      final var message =
          String.format(
              """
              Dataset directory does not exist for '%s.%s': '%s'

              To fix:
                1. Create the directory and add dataset files
                2. Or verify the path is correct

              See: %s""",
              testClass.getSimpleName(), testMethodName, location, DATASET_DOCS_URL);
      throw new DataSetLoadException(message);
    }

    if (!Files.isDirectory(path)) {
      final var message =
          String.format(
              """
              Path exists but is not a directory: '%s'

              Hint: Ensure the path points to a directory, not a file.""",
              location);
      throw new DataSetLoadException(message);
    }

    return path;
  }

  /**
   * Creates a convention-based path from the test class name or base directory.
   *
   * <p>When {@code baseDirectory} is configured, the path is constructed using that directory as
   * the root. If the base directory starts with {@code classpath:}, the suffix is appended
   * directly. Otherwise, the suffix is appended to the file system path.
   *
   * <p>When {@code baseDirectory} is not configured (null or empty), the path is constructed from
   * the test class name: {@code classpath:[package]/[ClassName][suffix]}
   *
   * <p>Examples:
   *
   * <ul>
   *   <li>Base directory {@code "classpath:testdata"} with suffix {@code "/expected"} produces
   *       {@code "classpath:testdata/expected"}
   *   <li>Base directory {@code "/data/test"} with suffix {@code "/expected"} produces {@code
   *       "/data/test/expected"}
   *   <li>No base directory, test class {@code com.example.UserServiceTest} with suffix {@code
   *       "/expected"} produces {@code "classpath:com/example/UserServiceTest/expected"}
   * </ul>
   *
   * @param suffix the directory suffix to append, or {@code null} for no suffix
   * @return the resolved location string
   */
  private String createConventionBasedPath(final @Nullable String suffix) {
    final var normalizedSuffix = Optional.ofNullable(suffix).orElse("");
    return Optional.ofNullable(baseDirectory)
        .filter(Predicate.not(String::isEmpty))
        .map(dir -> String.format("%s%s", dir, normalizedSuffix))
        .orElseGet(
            () ->
                String.format(
                    "%s%s%s",
                    CLASSPATH_PREFIX, testClass().getName().replace('.', '/'), normalizedSuffix));
  }

  /**
   * Checks whether the given path has one of the supported file extensions.
   *
   * @param path the file path to inspect
   * @param supportedFileExtensions supported file extension strings (with leading dot)
   * @return true if the path ends with a supported file extension
   */
  private boolean hasSupportedFileExtension(
      final Path path, final Set<String> supportedFileExtensions) {
    final var fileName = path.getFileName().toString();
    return FileExtension.fromFileName(fileName)
        .map(FileExtension::value)
        .map(supportedFileExtensions::contains)
        .orElse(false);
  }
}
