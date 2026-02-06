package io.github.seijikohara.dbtester.internal.format.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.format.parser.StructuredFormatParser;
import io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;
import java.nio.file.Path;

/**
 * Format provider for YAML (YAML Ain't Markup Language) files.
 *
 * <p>This provider parses YAML files from a directory and converts them into a {@link TableSet}.
 * Each YAML file represents a single database table, where the filename (without extension) becomes
 * the table name. The YAML format uses a list of mappings, with each mapping representing a row and
 * key-value pairs representing column-value mappings.
 *
 * <p>Example YAML file content:
 *
 * <pre>{@code
 * - ID: 1
 *   NAME: Alice
 *   EMAIL: alice@example.com
 * - ID: 2
 *   NAME: Bob
 *   EMAIL: bob@example.com
 * }</pre>
 *
 * <p>Column order is determined by the first mapping's key order. Null values in YAML (represented
 * by {@code null} or {@code ~}) are preserved as NULL database values. All non-null values are
 * converted to strings.
 *
 * <p>YAML comments are supported and ignored during parsing.
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
public final class YamlFormatProvider implements FormatProvider {

  /** The file extension for YAML files. */
  private static final FileExtension FILE_EXTENSION = new FileExtension("yaml");

  /** The parser for YAML files. */
  private final StructuredFormatParser parser;

  /**
   * Creates a new YAML format provider.
   *
   * <p>This constructor is used by ServiceLoader for automatic discovery.
   */
  public YamlFormatProvider() {
    this.parser = new StructuredFormatParser(".yaml", new YAMLMapper(new YAMLFactory()));
  }

  /**
   * {@inheritDoc}
   *
   * @return the YAML file extension
   */
  @Override
  public FileExtension supportedFileExtension() {
    return FILE_EXTENSION;
  }

  /**
   * {@inheritDoc}
   *
   * @param directory the directory containing YAML files
   * @return the parsed dataset containing all tables
   */
  @Override
  public TableSet parse(final Path directory) {
    return parser.parse(directory);
  }
}
