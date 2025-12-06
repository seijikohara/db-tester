package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Represents a logical dataset comprised of one or more tables and an optional data source. */
public interface DataSet {

  /**
   * Returns the tables that belong to this dataset in declaration order.
   *
   * @return immutable list of tables composing the dataset
   */
  List<Table> getTables();

  /**
   * Resolves a table by name.
   *
   * @param tableName the logical table identifier
   * @return an Optional containing the matching table, or empty if the dataset does not contain
   *     that table
   */
  Optional<Table> getTable(final TableName tableName);

  /**
   * Returns the data source that should be used when executing the dataset.
   *
   * @return an Optional containing the bound data source, or empty if not specified
   */
  Optional<DataSource> getDataSource();
}
