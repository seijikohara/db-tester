package io.github.seijikohara.dbtester.internal.format.parser;

/**
 * Configuration for parsing delimited text files.
 *
 * <p>This record encapsulates the delimiter character and file extension used by a delimited file
 * format. Instances are typically obtained from predefined constants ({@link #CSV} for
 * comma-separated values, {@link #TSV} for tab-separated values) or created for custom formats.
 *
 * @param delimiter the character used to separate values in the file
 * @param extension the file extension without leading dot (e.g., "csv", "tsv")
 */
public record DelimiterConfig(char delimiter, String extension) {

  /** Comma-separated values configuration. */
  public static final DelimiterConfig CSV = new DelimiterConfig(',', "csv");

  /** Tab-separated values configuration. */
  public static final DelimiterConfig TSV = new DelimiterConfig('\t', "tsv");

  /**
   * Creates a delimiter configuration.
   *
   * @param delimiter the character used to separate values in the file
   * @param extension the file extension without leading dot (e.g., "csv", "tsv")
   * @throws IllegalArgumentException if extension is blank
   */
  public DelimiterConfig {
    if (extension.isBlank()) {
      throw new IllegalArgumentException("extension must not be blank");
    }
  }
}
