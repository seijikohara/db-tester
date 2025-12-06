package io.github.seijikohara.dbtester.internal.dbunit.format;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dbunit.adapter.DbUnitDataSetAdapter;
import java.nio.file.Path;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.csv.CsvDataSet;

/** {@link DataSetReader} implementation backed by DbUnit's {@link CsvDataSet}. */
public final class CsvDataSetReader implements DataSetReader {

  /**
   * Creates CSV dataset reader.
   *
   * <p>This is a stateless utility class - instances can be reused or created as needed.
   */
  public CsvDataSetReader() {}

  /**
   * Reads CSV files from the specified directory into a DataSet.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Converts Path to File (DbUnit's CsvDataSet requires java.io.File)
   *   <li>Creates a DbUnit {@link CsvDataSet} from the directory
   *   <li>Wraps it in a {@link DbUnitDataSetAdapter} to hide DbUnit types
   *   <li>Returns the framework-independent {@link DataSet}
   * </ol>
   *
   * <p><strong>Note:</strong> This is the ONLY place in the entire framework where {@code Path ->
   * File} conversion occurs. All other code uses {@link Path} exclusively.
   *
   * @param directory the directory containing CSV files (one file per table)
   * @return the loaded dataset
   * @throws DataSetLoadException if CSV loading fails
   */
  @Override
  public DataSet read(final Path directory) {
    try {
      // Convert Path to File - DbUnit's CsvDataSet only accepts java.io.File
      // This is the ONLY Path->File conversion in the entire framework
      final var directoryFile = directory.toFile();

      // Use DbUnit's CSV parser to load all CSV files in the directory
      final var csvDataSet = new CsvDataSet(directoryFile);

      // Wrap the DbUnit dataset to hide DbUnit types from the framework
      return new DbUnitDataSetAdapter(csvDataSet);
    } catch (final DataSetException e) {
      throw new DataSetLoadException(
          String.format("Failed to load CSV dataset from: %s", directory), e);
    }
  }
}
