package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * Simple immutable implementation of {@link DataSet}.
 *
 * <p>This implementation stores tables in a list and provides lookup by table name.
 */
public final class SimpleDataSet implements DataSet {

  /** The tables in this dataset. */
  private final List<Table> tables;

  /**
   * Creates a new dataset with the given tables.
   *
   * @param tables the tables in this dataset
   */
  public SimpleDataSet(final List<Table> tables) {
    this.tables = List.copyOf(tables);
  }

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
