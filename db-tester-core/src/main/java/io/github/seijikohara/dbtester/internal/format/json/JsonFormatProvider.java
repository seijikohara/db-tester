package io.github.seijikohara.dbtester.internal.format.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.format.parser.StructuredFormatParser;
import io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;
import java.nio.file.Path;

/**
 * Format provider for JSON (JavaScript Object Notation) files.
 *
 * <p>This provider parses JSON files from a directory and converts them into a {@link TableSet}.
 * Each JSON file represents a single database table, where the filename (without extension) becomes
 * the table name. The JSON format uses an array of objects, with each object representing a row and
 * key-value pairs representing column-value mappings.
 *
 * <p>Example JSON file content:
 *
 * <pre>{@code
 * [
 *   {"ID": 1, "NAME": "Alice", "EMAIL": "alice@example.com"},
 *   {"ID": 2, "NAME": "Bob", "EMAIL": "bob@example.com"}
 * ]
 * }</pre>
 *
 * <p>Column order is determined by the first object's key order. Null values in JSON are preserved
 * as NULL database values. All non-null values are converted to strings.
 *
 * <p>Table ordering is determined by the load order file (default: {@value
 * ConventionSettings#DEFAULT_LOAD_ORDER_FILE_NAME}) if present, otherwise tables are loaded in
 * alphabetical order by filename.
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see FormatProvider
 * @see StructuredFormatParser
 */
public final class JsonFormatProvider implements FormatProvider {

  /** The file extension for JSON files. */
  private static final FileExtension FILE_EXTENSION = new FileExtension("json");

  /** The parser for JSON files. */
  private final StructuredFormatParser parser;

  /**
   * Creates a new JSON format provider.
   *
   * <p>This constructor is used by ServiceLoader for automatic discovery.
   */
  public JsonFormatProvider() {
    this.parser = new StructuredFormatParser(".json", new ObjectMapper());
  }

  /**
   * {@inheritDoc}
   *
   * @return the JSON file extension
   */
  @Override
  public FileExtension supportedFileExtension() {
    return FILE_EXTENSION;
  }

  /**
   * {@inheritDoc}
   *
   * @param directory the directory containing JSON files
   * @return the parsed dataset containing all tables
   */
  @Override
  public TableSet parse(final Path directory) {
    return parser.parse(directory);
  }
}
