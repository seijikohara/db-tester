package io.github.seijikohara.dbtester.api.domain;

import java.sql.JDBCType;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Rich domain model representing a database column with metadata.
 *
 * <p>This class provides comprehensive column information including:
 *
 * <ul>
 *   <li>Column name and ordering
 *   <li>SQL type information
 *   <li>Constraints (nullable, primary key)
 *   <li>Precision and scale for numeric types
 *   <li>Comparison strategy for assertions
 * </ul>
 *
 * @see ColumnName
 * @see ColumnMetadata
 * @see ComparisonStrategy
 */
public final class Column implements Comparable<Column> {

  /** The column name. */
  private final ColumnName name;

  /** Optional metadata for schema validation. */
  private final @Nullable ColumnMetadata metadata;

  /** Strategy for comparing values in this column. */
  private final ComparisonStrategy comparisonStrategy;

  /**
   * Creates a column with all properties.
   *
   * @param name the column name
   * @param metadata optional column metadata
   * @param comparisonStrategy the comparison strategy
   */
  private Column(
      final ColumnName name,
      final @Nullable ColumnMetadata metadata,
      final ComparisonStrategy comparisonStrategy) {
    this.name = name;
    this.metadata = metadata;
    this.comparisonStrategy = comparisonStrategy;
  }

  /**
   * Creates a column with just a name.
   *
   * <p>This factory method creates a column with default comparison strategy (STRICT) and no
   * metadata. Use {@link #builder(String)} for more control.
   *
   * @param name the column name
   * @return a new Column instance
   */
  public static Column of(final String name) {
    return new Column(new ColumnName(name), null, ComparisonStrategy.STRICT);
  }

  /**
   * Creates a column from an existing ColumnName.
   *
   * @param name the column name
   * @return a new Column instance
   */
  public static Column of(final ColumnName name) {
    return new Column(name, null, ComparisonStrategy.STRICT);
  }

  /**
   * Creates a builder for constructing a Column with custom properties.
   *
   * @param name the column name
   * @return a new Builder instance
   */
  public static Builder builder(final String name) {
    return new Builder(name);
  }

  /**
   * Creates a builder from an existing ColumnName.
   *
   * @param name the column name
   * @return a new Builder instance
   */
  public static Builder builder(final ColumnName name) {
    return new Builder(name);
  }

  /**
   * Returns the column name.
   *
   * @return the column name
   */
  public ColumnName getName() {
    return name;
  }

  /**
   * Returns the column name as a string.
   *
   * <p>Convenience method equivalent to {@code getName().value()}.
   *
   * @return the column name string
   */
  public String getNameValue() {
    return name.value();
  }

  /**
   * Returns the column metadata if available.
   *
   * @return the metadata, or empty if not set
   */
  public Optional<ColumnMetadata> getMetadata() {
    return Optional.ofNullable(metadata);
  }

  /**
   * Returns the comparison strategy for this column.
   *
   * @return the comparison strategy
   */
  public ComparisonStrategy getComparisonStrategy() {
    return comparisonStrategy;
  }

  /**
   * Checks if this column has metadata attached.
   *
   * @return {@code true} if metadata is present, {@code false} otherwise
   */
  public boolean hasMetadata() {
    return metadata != null;
  }

  /**
   * Checks if this column should be ignored during comparison.
   *
   * @return {@code true} if comparison strategy is IGNORE, {@code false} otherwise
   */
  public boolean isIgnored() {
    return comparisonStrategy.isIgnore();
  }

  /**
   * Creates a new Column with the specified comparison strategy.
   *
   * @param strategy the new comparison strategy
   * @return a new Column with the updated strategy
   */
  public Column withComparisonStrategy(final ComparisonStrategy strategy) {
    return new Column(name, metadata, strategy);
  }

  /**
   * Creates a new Column with the specified metadata.
   *
   * @param newMetadata the new metadata
   * @return a new Column with the updated metadata
   */
  public Column withMetadata(final ColumnMetadata newMetadata) {
    return new Column(name, newMetadata, comparisonStrategy);
  }

  @Override
  public int compareTo(final Column other) {
    return this.name.compareTo(other.name);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Column other)) {
      return false;
    }
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return String.format("Column[%s]", name.value());
  }

  /** Builder for constructing Column instances with custom properties. */
  public static final class Builder {
    /** The column name. */
    private final ColumnName name;

    /** The JDBC type. */
    private @Nullable JDBCType jdbcType;

    /** Whether NULL is allowed. */
    private boolean nullable = true;

    /** Whether this is a primary key. */
    private boolean primaryKey = false;

    /** The ordinal position. */
    private int ordinalPosition = 0;

    /** The precision for numeric types. */
    private int precision = 0;

    /** The scale for numeric types. */
    private int scale = 0;

    /** The default value. */
    private @Nullable String defaultValue;

    /** The comparison strategy. */
    private ComparisonStrategy comparisonStrategy = ComparisonStrategy.STRICT;

    /** The regex pattern for REGEX strategy. */
    private @Nullable String regexPattern;

    /**
     * Creates a builder with the specified column name.
     *
     * @param name the column name string
     */
    private Builder(final String name) {
      this.name = new ColumnName(name);
    }

    /**
     * Creates a builder with the specified ColumnName.
     *
     * @param name the column name
     */
    private Builder(final ColumnName name) {
      this.name = name;
    }

    /**
     * Sets the JDBC type for this column.
     *
     * @param type the JDBC type
     * @return this builder
     */
    public Builder jdbcType(final JDBCType type) {
      this.jdbcType = type;
      return this;
    }

    /**
     * Sets whether this column allows NULL values.
     *
     * @param isNullable true if NULL is allowed
     * @return this builder
     */
    public Builder nullable(final boolean isNullable) {
      this.nullable = isNullable;
      return this;
    }

    /**
     * Sets whether this column is a primary key.
     *
     * @param isPrimaryKey true if this is a primary key column
     * @return this builder
     */
    public Builder primaryKey(final boolean isPrimaryKey) {
      this.primaryKey = isPrimaryKey;
      return this;
    }

    /**
     * Sets the ordinal position (1-based) of this column in the table.
     *
     * @param position the ordinal position
     * @return this builder
     */
    public Builder ordinalPosition(final int position) {
      this.ordinalPosition = position;
      return this;
    }

    /**
     * Sets the precision for numeric types.
     *
     * @param columnPrecision the precision
     * @return this builder
     */
    public Builder precision(final int columnPrecision) {
      this.precision = columnPrecision;
      return this;
    }

    /**
     * Sets the scale for numeric types.
     *
     * @param columnScale the scale
     * @return this builder
     */
    public Builder scale(final int columnScale) {
      this.scale = columnScale;
      return this;
    }

    /**
     * Sets the default value for this column.
     *
     * @param value the default value as a string
     * @return this builder
     */
    public Builder defaultValue(final @Nullable String value) {
      this.defaultValue = value;
      return this;
    }

    /**
     * Sets the comparison strategy for this column.
     *
     * @param strategy the comparison strategy
     * @return this builder
     */
    public Builder comparisonStrategy(final ComparisonStrategy strategy) {
      this.comparisonStrategy = strategy;
      return this;
    }

    /**
     * Sets a regex pattern for REGEX comparison strategy.
     *
     * <p>This stores the pattern for use when building. The comparison strategy will be set to
     * REGEX automatically when build() is called.
     *
     * @param pattern the regex pattern
     * @return this builder
     */
    public Builder regexPattern(final String pattern) {
      this.regexPattern = pattern;
      return this;
    }

    /**
     * Builds the Column instance.
     *
     * @return a new Column with the configured properties
     */
    public Column build() {
      final ColumnMetadata metadata;
      if (jdbcType != null || primaryKey || ordinalPosition > 0) {
        metadata =
            new ColumnMetadata(
                jdbcType, nullable, primaryKey, ordinalPosition, precision, scale, defaultValue);
      } else {
        metadata = null;
      }

      final var strategy =
          Optional.ofNullable(regexPattern)
              .map(ComparisonStrategy::regex)
              .orElse(comparisonStrategy);

      return new Column(name, metadata, strategy);
    }
  }
}
