package io.github.seijikohara.dbtester.api.export;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Configuration for data export operations.
 *
 * <p>This class defines how database values are formatted when exporting to files. It includes
 * settings for null representation, date/time formatting, LOB handling, and load order file
 * generation.
 *
 * <p>Use {@link #defaults()} to obtain an instance with default values, or {@link #builder()} to
 * create a customized configuration.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var config = ExportConfiguration.defaults();
 *
 * // Custom configuration
 * var config = ExportConfiguration.builder()
 *     .nullValue("NULL")
 *     .lobHandling(LobHandling.OMIT)
 *     .writeLoadOrderFile(true)
 *     .build();
 * }</pre>
 *
 * @see DataSetExporter
 * @see LobHandling
 */
public final class ExportConfiguration {

  /** Default null value representation for delimited formats. */
  public static final String DEFAULT_NULL_VALUE = "";

  /** Default date formatter (ISO local date: yyyy-MM-dd). */
  public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  /** Default time formatter (ISO local time: HH:mm:ss). */
  public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

  /** Default timestamp formatter (JDBC timestamp: yyyy-MM-dd HH:mm:ss). */
  public static final DateTimeFormatter DEFAULT_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** The string representation for null values in delimited formats. */
  private final String nullValue;

  /** The formatter for date values. */
  private final DateTimeFormatter dateFormatter;

  /** The formatter for time values. */
  private final DateTimeFormatter timeFormatter;

  /** The formatter for timestamp values. */
  private final DateTimeFormatter timestampFormatter;

  /** The handling strategy for LOB columns. */
  private final LobHandling lobHandling;

  /** Whether to generate a load order file. */
  private final boolean writeLoadOrderFile;

  /** The name of the load order file. */
  private final String loadOrderFileName;

  /**
   * Creates a new configuration from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private ExportConfiguration(final Builder builder) {
    this.nullValue = builder.nullValue;
    this.dateFormatter = builder.dateFormatter;
    this.timeFormatter = builder.timeFormatter;
    this.timestampFormatter = builder.timestampFormatter;
    this.lobHandling = builder.lobHandling;
    this.writeLoadOrderFile = builder.writeLoadOrderFile;
    this.loadOrderFileName = builder.loadOrderFileName;
  }

  /**
   * Creates a new builder for constructing ExportConfiguration instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a configuration instance with default values.
   *
   * @return a configuration with default settings
   */
  public static ExportConfiguration defaults() {
    return builder().build();
  }

  /**
   * Returns the string representation for null values.
   *
   * <p>This setting applies only to delimited formats (CSV, TSV). JSON and YAML use their native
   * null representation.
   *
   * @return the null value representation
   */
  public String nullValue() {
    return nullValue;
  }

  /**
   * Returns the formatter for date values.
   *
   * @return the date formatter
   */
  public DateTimeFormatter dateFormatter() {
    return dateFormatter;
  }

  /**
   * Returns the formatter for time values.
   *
   * @return the time formatter
   */
  public DateTimeFormatter timeFormatter() {
    return timeFormatter;
  }

  /**
   * Returns the formatter for timestamp values.
   *
   * @return the timestamp formatter
   */
  public DateTimeFormatter timestampFormatter() {
    return timestampFormatter;
  }

  /**
   * Returns the handling strategy for LOB columns.
   *
   * @return the LOB handling strategy
   */
  public LobHandling lobHandling() {
    return lobHandling;
  }

  /**
   * Returns whether to generate a load order file.
   *
   * @return true if a load order file should be generated
   */
  public boolean writeLoadOrderFile() {
    return writeLoadOrderFile;
  }

  /**
   * Returns the name of the load order file.
   *
   * @return the load order file name
   */
  public String loadOrderFileName() {
    return loadOrderFileName;
  }

  /** Builder for constructing {@link ExportConfiguration} instances. */
  public static final class Builder {

    /** The string representation for null values. */
    private String nullValue = DEFAULT_NULL_VALUE;

    /** The formatter for date values. */
    private DateTimeFormatter dateFormatter = DEFAULT_DATE_FORMATTER;

    /** The formatter for time values. */
    private DateTimeFormatter timeFormatter = DEFAULT_TIME_FORMATTER;

    /** The formatter for timestamp values. */
    private DateTimeFormatter timestampFormatter = DEFAULT_TIMESTAMP_FORMATTER;

    /** The handling strategy for LOB columns. */
    private LobHandling lobHandling = LobHandling.BASE64;

    /** Whether to generate a load order file. */
    private boolean writeLoadOrderFile = false;

    /** The name of the load order file. */
    private String loadOrderFileName = ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the string representation for null values.
     *
     * <p>This setting applies only to delimited formats (CSV, TSV).
     *
     * @param nullValue the null value representation
     * @return this builder
     */
    public Builder nullValue(final String nullValue) {
      this.nullValue = Objects.requireNonNull(nullValue, "nullValue");
      return this;
    }

    /**
     * Sets the formatter for date values.
     *
     * @param dateFormatter the date formatter
     * @return this builder
     */
    public Builder dateFormatter(final DateTimeFormatter dateFormatter) {
      this.dateFormatter = Objects.requireNonNull(dateFormatter, "dateFormatter");
      return this;
    }

    /**
     * Sets the formatter for time values.
     *
     * @param timeFormatter the time formatter
     * @return this builder
     */
    public Builder timeFormatter(final DateTimeFormatter timeFormatter) {
      this.timeFormatter = Objects.requireNonNull(timeFormatter, "timeFormatter");
      return this;
    }

    /**
     * Sets the formatter for timestamp values.
     *
     * @param timestampFormatter the timestamp formatter
     * @return this builder
     */
    public Builder timestampFormatter(final DateTimeFormatter timestampFormatter) {
      this.timestampFormatter = Objects.requireNonNull(timestampFormatter, "timestampFormatter");
      return this;
    }

    /**
     * Sets the handling strategy for LOB columns.
     *
     * @param lobHandling the LOB handling strategy
     * @return this builder
     */
    public Builder lobHandling(final LobHandling lobHandling) {
      this.lobHandling = Objects.requireNonNull(lobHandling, "lobHandling");
      return this;
    }

    /**
     * Sets whether to generate a load order file.
     *
     * @param writeLoadOrderFile true to generate a load order file
     * @return this builder
     */
    public Builder writeLoadOrderFile(final boolean writeLoadOrderFile) {
      this.writeLoadOrderFile = writeLoadOrderFile;
      return this;
    }

    /**
     * Sets the name of the load order file.
     *
     * @param loadOrderFileName the load order file name
     * @return this builder
     */
    public Builder loadOrderFileName(final String loadOrderFileName) {
      this.loadOrderFileName = Objects.requireNonNull(loadOrderFileName, "loadOrderFileName");
      return this;
    }

    /**
     * Builds a new {@link ExportConfiguration} instance with the configured values.
     *
     * @return a new ExportConfiguration instance
     */
    public ExportConfiguration build() {
      return new ExportConfiguration(this);
    }
  }
}
