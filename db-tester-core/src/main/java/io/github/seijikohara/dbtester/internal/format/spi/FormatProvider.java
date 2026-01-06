package io.github.seijikohara.dbtester.internal.format.spi;

import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import java.nio.file.Path;

/**
 * SPI for constructing {@link TableSet} instances from a particular file format.
 *
 * <p>Implementations of this interface define how to load dataset files in specific formats (e.g.,
 * CSV, TSV, JSON) and convert them into {@link TableSet} instances that can be used by the database
 * testing framework.
 *
 * <p>Unlike the legacy {@code DataSetFormatProvider}, this interface separates format parsing from
 * scenario filtering. Scenario filtering is applied as a separate layer using the classes in the
 * {@code io.github.seijikohara.dbtester.internal.scenario} package.
 *
 * <p>Implementations must be thread-safe and stateless (or use immutable state). File extension
 * matching is case-insensitive.
 *
 * <p>Providers are discovered automatically using Java's {@link java.util.ServiceLoader} mechanism.
 * Configure service providers in {@code
 * META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider}.
 *
 * @see TableSet
 * @see FileExtension
 * @see FormatRegistry
 */
public interface FormatProvider {

  /**
   * Returns the file extension supported by this provider.
   *
   * <p>The extension should be specified without a leading dot (e.g., "csv", "tsv"). Extensions are
   * automatically normalized for case-insensitive matching.
   *
   * @return the file extension (e.g., new FileExtension("csv"), new FileExtension("tsv"))
   */
  FileExtension supportedFileExtension();

  /**
   * Parses all data files in the specified directory into a TableSet.
   *
   * <p>This method reads all files matching the supported extension from the directory and converts
   * them into a TableSet. Each file typically represents one table.
   *
   * @param directory the directory that contains data files (one file per logical table)
   * @return the parsed dataset containing all tables
   * @throws io.github.seijikohara.dbtester.api.exception.DataSetLoadException if parsing fails
   */
  TableSet parse(Path directory);
}
