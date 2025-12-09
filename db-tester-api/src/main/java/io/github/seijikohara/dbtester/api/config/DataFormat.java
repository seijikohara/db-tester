package io.github.seijikohara.dbtester.api.config;

/**
 * Defines the file format used for dataset files.
 *
 * <p>This enum specifies which file format should be used when loading dataset files from a
 * directory. When multiple file formats exist in the same directory (e.g., both TABLE.csv and
 * TABLE.tsv), only files matching the configured format will be loaded.
 *
 * <p>The default format is {@link #CSV}. This can be overridden in {@link ConventionSettings}.
 *
 * @see ConventionSettings
 */
public enum DataFormat {

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
  TSV(".tsv");

  /** The file extension associated with this format (including the leading dot). */
  private final String extension;

  /**
   * Creates a new data format with the specified file extension.
   *
   * @param extension the file extension (e.g., ".csv", ".tsv")
   */
  DataFormat(final String extension) {
    this.extension = extension;
  }

  /**
   * Returns the file extension associated with this format.
   *
   * <p>The extension includes the leading dot (e.g., ".csv", ".tsv").
   *
   * @return the file extension
   */
  public String getExtension() {
    return extension;
  }
}
