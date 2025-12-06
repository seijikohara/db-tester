package io.github.seijikohara.dbtester.internal.domain;

import java.util.Locale;
import java.util.Optional;

/**
 * Type-safe wrapper for file extension with automatic normalization.
 *
 * <p>File extensions are normalized to lowercase using {@link Locale#ROOT} for case-insensitive
 * comparison. All extensions are stored internally with the leading dot (e.g., ".csv", ".txt").
 *
 * <p>Extensions are automatically normalized on construction by converting to lowercase and adding
 * a leading dot if missing. After normalization, extensions must have at least one character after
 * the dot.
 *
 * <p>This record is immutable and thread-safe.
 *
 * @param value the normalized file extension (e.g., ".csv", ".txt")
 */
public record FileExtension(String value) {

  /**
   * Compact constructor that normalizes and validates the file extension.
   *
   * <p>The extension is normalized to lowercase with a leading dot, then validated to ensure it has
   * at least one character after the dot.
   *
   * @throws IllegalArgumentException if the extension is invalid
   */
  public FileExtension {
    value = normalizeExtension(value);
    validateFormat(value);
  }

  /**
   * Normalizes the extension to lowercase and ensures it has a leading dot.
   *
   * <p>This method accepts extensions with or without a leading dot.
   *
   * @param extension the extension to normalize
   * @return the normalized extension (lowercase with leading dot)
   */
  private static String normalizeExtension(final String extension) {
    final var lowercaseExt = extension.toLowerCase(Locale.ROOT);
    return lowercaseExt.startsWith(".") ? lowercaseExt : String.format(".%s", lowercaseExt);
  }

  /**
   * Validates extension format.
   *
   * <p>The extension must start with a dot and have at least one character after it.
   *
   * @param extension the extension to validate (must already be normalized)
   * @throws IllegalArgumentException if extension does not start with '.' or is empty after '.'
   */
  private static void validateFormat(final String extension) {
    if (!extension.startsWith(".")) {
      throw new IllegalArgumentException(
          String.format("File extension must start with '.': %s", extension));
    }
    if (extension.length() <= 1) {
      throw new IllegalArgumentException("File extension must not be empty after '.'");
    }
  }

  /**
   * Extracts file extension from a file name.
   *
   * <p>The extension includes the leading dot and is normalized to lowercase.
   *
   * @param fileName the file name (e.g., "table.CSV", "data.txt")
   * @return Optional containing FileExtension if present (e.g., ".csv"), empty otherwise
   */
  public static Optional<FileExtension> fromFileName(final String fileName) {
    final var dotIndex = fileName.lastIndexOf('.');
    if (dotIndex <= 0) {
      return Optional.empty();
    }
    try {
      return Optional.of(new FileExtension(fileName.substring(dotIndex)));
    } catch (final IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  /**
   * Checks if a file name has this extension.
   *
   * <p>The comparison is case-insensitive.
   *
   * @param fileName the file name to check (e.g., "table.CSV")
   * @return {@code true} if the file has this extension, {@code false} otherwise
   */
  public boolean matches(final String fileName) {
    return fromFileName(fileName).map(ext -> ext.equals(this)).orElse(false);
  }
}
