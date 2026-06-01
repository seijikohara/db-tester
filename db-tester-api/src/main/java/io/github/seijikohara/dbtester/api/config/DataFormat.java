package io.github.seijikohara.dbtester.api.config;

import org.jspecify.annotations.Nullable;

/**
 * Defines the file format used for dataset files.
 *
 * <p>This enum specifies which file format should be used when loading dataset files from a
 * directory. The default format is {@link #AUTO}, which automatically detects all supported file
 * formats in the dataset directory.
 *
 * <p>When a concrete format (e.g., {@link #CSV}) is specified, only files matching that format will
 * be loaded. When {@link #AUTO} is used, files of all supported formats are loaded, with an error
 * reported if the same table name appears in multiple formats.
 *
 * @see ConventionSettings
 */
public enum DataFormat {

  /**
   * Automatic format detection mode.
   *
   * <p>Detects all supported file formats in the dataset directory. If the same table name exists
   * in multiple formats (e.g., {@code USER.csv} and {@code USER.yaml}), a {@link
   * io.github.seijikohara.dbtester.api.exception.DataSetLoadException} is thrown with details about
   * the conflicting files.
   */
  AUTO(null),

  /**
   * Comma-Separated Values format.
   *
   * <p>Files with the {@code .csv} extension. Fields are separated by commas.
   */
  CSV(".csv"),

  /**
   * Tab-Separated Values format.
   *
   * <p>Files with the {@code .tsv} extension. Fields are separated by tabs.
   */
  TSV(".tsv"),

  /**
   * JavaScript Object Notation format.
   *
   * <p>Files with the {@code .json} extension. Each file contains an array of objects representing
   * table rows.
   */
  JSON(".json"),

  /**
   * YAML Ain't Markup Language format.
   *
   * <p>Files with the {@code .yaml} extension. Each file contains a list of mappings representing
   * table rows.
   */
  YAML(".yaml");

  /** The file extension associated with this format, or null for AUTO. */
  private final @Nullable String extension;

  /**
   * Creates a new data format with the specified file extension.
   *
   * @param extension the file extension (e.g., ".csv", ".tsv"), or null for AUTO
   */
  DataFormat(final @Nullable String extension) {
    this.extension = extension;
  }

  /**
   * Returns the file extension associated with this format.
   *
   * <p>The extension includes the leading dot (e.g., ".csv", ".tsv").
   *
   * @return the file extension
   * @throws UnsupportedOperationException if this format is {@link #AUTO}
   */
  public String extension() {
    if (extension == null) {
      throw new UnsupportedOperationException(
          "AUTO format does not have a single file extension. Use hasExtension() to check first.");
    }
    return extension;
  }

  /**
   * Returns whether this format has a specific file extension.
   *
   * <p>Returns {@code false} for {@link #AUTO} (which supports all extensions) and {@code true} for
   * all concrete formats.
   *
   * @return true if this format has a specific extension, false otherwise
   */
  public boolean hasExtension() {
    return extension != null;
  }
}
