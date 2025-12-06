package io.github.seijikohara.dbtester.internal.dataset;

import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Base {@link DataSet} implementation that supports scenario-aware filtering of rows.
 *
 * <p>Implementations populate the table collection and associate a {@link DataSource} for execution
 * through the constructor.
 */
public abstract class ScenarioDataSet implements DataSet {

  /** The data source associated with this dataset (immutable). */
  private final @Nullable DataSource dataSource;

  /**
   * Creates a scenario dataset with the specified data source.
   *
   * <p>Subclasses must implement {@link #getTables()} to provide the collection of tables. The data
   * source is immutable after construction.
   *
   * @param dataSource the data source to associate with this dataset, or {@code null} for no
   *     association
   */
  protected ScenarioDataSet(final @Nullable DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Gets all tables in this dataset.
   *
   * @return the list of tables, never null
   */
  @Override
  public abstract List<Table> getTables();

  /**
   * Retrieves a table by its name.
   *
   * <p>This implementation searches through all tables using a stream filter.
   *
   * @param tableName the name of the table to retrieve
   * @return an Optional containing the table if found, or empty if not found
   */
  @Override
  public Optional<Table> getTable(final TableName tableName) {
    return getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst();
  }

  @Override
  public final Optional<DataSource> getDataSource() {
    return Optional.ofNullable(dataSource);
  }
}
