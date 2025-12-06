package io.github.seijikohara.dbtester.internal.dbunit.format;

import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import java.nio.file.Path;

/** Strategy interface implemented by format-specific dataset readers. */
public interface DataSetReader {

  /**
   * Reads the dataset located at {@code path}.
   *
   * @param path absolute or relative path pointing to the dataset directory
   * @return a dataset instance populated from the files at the supplied path
   */
  DataSet read(Path path);
}
