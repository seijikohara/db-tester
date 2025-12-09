package io.github.seijikohara.dbtester.api.dataset;

import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Represents a logical dataset comprised of one or more tables and an optional data source.
 *
 * <p>A DataSet is the primary abstraction for representing database data in the DB Tester
 * framework. It contains zero or more {@link Table} instances, each representing a single database
 * table with its columns and rows.
 *
 * <p>Implementations must ensure that:
 *
 * <ul>
 *   <li>Table order is preserved (insertion order)
 *   <li>All returned collections are immutable
 *   <li>Table names are unique within a dataset
 * </ul>
 *
 * @see Table
 * @see Row
 */
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
  Optional<Table> getTable(TableName tableName);

  /**
   * Returns the data source that should be used when executing the dataset.
   *
   * @return an Optional containing the bound data source, or empty if not specified
   */
  Optional<DataSource> getDataSource();
}
