package io.github.seijikohara.dbtester.internal.scenario;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator that applies scenario filtering to a dataset.
 *
 * <p>This class wraps a source dataset and applies scenario-based row filtering to all tables. The
 * filtering is delegated to {@link FilteredTable} for each table in the dataset.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @see ScenarioFilter
 * @see FilteredTable
 */
public final class FilteredTableSet implements TableSet {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(FilteredTableSet.class);

  /** The scenario-filtered tables. */
  private final List<Table> tables;

  /** The data source associated with this dataset. */
  private final @Nullable DataSource dataSource;

  /**
   * Creates a filtered dataset from a source dataset.
   *
   * @param sourceTableSet the source dataset to filter
   * @param filter the scenario filter to apply
   * @param dataSource the data source to associate with this dataset, or null
   */
  public FilteredTableSet(
      final TableSet sourceTableSet,
      final ScenarioFilter filter,
      final @Nullable DataSource dataSource) {
    this.tables =
        sourceTableSet.getTables().stream()
            .map(table -> (Table) new FilteredTable(table, filter))
            .toList();
    this.dataSource = dataSource;

    logger.debug(
        "Created filtered dataset with {} tables, filter active: {}",
        tables.size(),
        filter.isActive());
  }

  /**
   * {@inheritDoc}
   *
   * @return immutable list of scenario-filtered tables
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
   * @return an Optional containing the data source if specified, or empty otherwise
   */
  @Override
  public Optional<DataSource> getDataSource() {
    return Optional.ofNullable(dataSource);
  }
}
