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
   * Creates a new dataset with the given tables.
   *
   * @param tables the tables in this dataset
   * @return a new immutable dataset instance
   */
  static DataSet of(final List<Table> tables) {
    return new SimpleDataSet(List.copyOf(tables));
  }

  /**
   * Creates a new dataset with the given tables.
   *
   * @param tables the tables in this dataset
   * @return a new immutable dataset instance
   */
  static DataSet of(final Table... tables) {
    return new SimpleDataSet(List.of(tables));
  }

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

  /**
   * Simple immutable implementation of {@link DataSet}.
   *
   * @param tables the tables in this dataset
   */
  record SimpleDataSet(List<Table> tables) implements DataSet {

    /**
     * {@inheritDoc}
     *
     * @return immutable list of tables in this dataset
     */
    @Override
    public List<Table> getTables() {
      return tables;
    }

    /**
     * {@inheritDoc}
     *
     * @param tableName the name of the table to retrieve
     * @return an Optional containing the table if found, or empty if not found
     */
    @Override
    public Optional<Table> getTable(final TableName tableName) {
      return tables.stream().filter(t -> t.getName().equals(tableName)).findFirst();
    }

    /**
     * {@inheritDoc}
     *
     * @return an Optional that is always empty since this dataset has no associated data source
     */
    @Override
    public Optional<DataSource> getDataSource() {
      return Optional.empty();
    }
  }
}
