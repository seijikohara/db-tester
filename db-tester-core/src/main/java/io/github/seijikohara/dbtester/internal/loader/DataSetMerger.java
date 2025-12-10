package io.github.seijikohara.dbtester.internal.loader;

import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleDataSet;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merges multiple datasets into a single dataset according to a merge strategy.
 *
 * <p>This class handles the combination of tables when multiple {@code @DataSet} annotations
 * reference the same table. The merge behavior is controlled by {@link TableMergeStrategy}: {@link
 * TableMergeStrategy#FIRST FIRST} keeps only the first occurrence, {@link TableMergeStrategy#LAST
 * LAST} keeps only the last occurrence, {@link TableMergeStrategy#UNION UNION} merges all tables
 * removing duplicates, and {@link TableMergeStrategy#UNION_ALL UNION_ALL} merges all tables keeping
 * all rows (default).
 *
 * <p>This class is stateless and thread-safe.
 *
 * @see TableMergeStrategy
 * @see DataSet
 */
public final class DataSetMerger {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(DataSetMerger.class);

  /** Creates a new dataset merger. */
  public DataSetMerger() {}

  /**
   * Merges multiple datasets into a single dataset.
   *
   * <p>Tables with the same name are combined according to the specified merge strategy. Tables
   * that appear in only one dataset are included as-is.
   *
   * @param dataSets the datasets to merge (in order)
   * @param strategy the merge strategy to apply
   * @return a single merged dataset, or an empty dataset if the input list is empty
   */
  public DataSet merge(final List<DataSet> dataSets, final TableMergeStrategy strategy) {
    if (dataSets.isEmpty()) {
      logger.debug("No datasets to merge, returning empty dataset");
      return new SimpleDataSet(List.of());
    }

    if (dataSets.size() == 1) {
      logger.debug("Single dataset, no merging needed");
      return dataSets.getFirst();
    }

    logger.debug("Merging {} datasets with strategy: {}", dataSets.size(), strategy);

    // Collect all tables by name, preserving order
    final Map<TableName, List<Table>> tablesByName = new LinkedHashMap<>();

    // Use the first non-null data source
    final @Nullable DataSource dataSource =
        dataSets.stream()
            .map(DataSet::getDataSource)
            .flatMap(Optional::stream)
            .findFirst()
            .orElse(null);

    dataSets.stream()
        .flatMap(dataSet -> dataSet.getTables().stream())
        .forEach(
            table ->
                tablesByName
                    .computeIfAbsent(table.getName(), unused -> new ArrayList<>())
                    .add(table));

    // Merge tables according to strategy
    final List<Table> mergedTables =
        tablesByName.entrySet().stream()
            .map(entry -> mergeTable(entry.getKey(), entry.getValue(), strategy))
            .toList();

    logger.debug("Merged into {} tables", mergedTables.size());

    return new MergedDataSet(mergedTables, dataSource);
  }

  /**
   * Merges multiple tables with the same name into a single table.
   *
   * @param tableName the table name
   * @param tables the tables to merge
   * @param strategy the merge strategy
   * @return the merged table
   */
  private Table mergeTable(
      final TableName tableName, final List<Table> tables, final TableMergeStrategy strategy) {

    if (tables.size() == 1) {
      return tables.getFirst();
    }

    return switch (strategy) {
      case FIRST -> {
        logger.debug("Table '{}': using first occurrence", tableName.value());
        yield tables.getFirst();
      }
      case LAST -> {
        logger.debug("Table '{}': using last occurrence", tableName.value());
        yield tables.getLast();
      }
      case UNION -> {
        logger.debug("Table '{}': merging with UNION (removing duplicates)", tableName.value());
        yield mergeWithUnion(tableName, tables, true);
      }
      case UNION_ALL -> {
        logger.debug("Table '{}': merging with UNION_ALL (keeping duplicates)", tableName.value());
        yield mergeWithUnion(tableName, tables, false);
      }
    };
  }

  /**
   * Merges tables using UNION or UNION ALL semantics.
   *
   * @param tableName the table name
   * @param tables the tables to merge
   * @param removeDuplicates true for UNION (remove duplicates), false for UNION ALL
   * @return the merged table
   */
  private Table mergeWithUnion(
      final TableName tableName, final List<Table> tables, final boolean removeDuplicates) {

    // Collect all columns from all tables (preserving order from first table, then adding new ones)
    final Set<ColumnName> seenColumns = new LinkedHashSet<>();
    final List<ColumnName> allColumns =
        tables.stream()
            .flatMap(table -> table.getColumns().stream())
            .filter(seenColumns::add)
            .toList();

    // Collect all rows
    final List<Row> allRows;
    if (removeDuplicates) {
      // Use a set to track seen rows for UNION semantics
      final Set<RowKey> seenRows = new LinkedHashSet<>();
      allRows =
          tables.stream()
              .flatMap(table -> table.getRows().stream())
              .filter(row -> seenRows.add(new RowKey(row, allColumns)))
              .toList();
    } else {
      // Just concatenate all rows for UNION ALL
      allRows = tables.stream().flatMap(table -> table.getRows().stream()).toList();
    }

    logger.debug(
        "Table '{}': merged {} tables into {} rows with {} columns",
        tableName.value(),
        tables.size(),
        allRows.size(),
        allColumns.size());

    return new SimpleTable(tableName, allColumns, allRows);
  }

  /**
   * A key for row deduplication in UNION operations.
   *
   * <p>Two rows are considered equal if all their column values match.
   *
   * @param row the row to use as a key
   * @param columns the columns to consider for equality
   */
  private record RowKey(Row row, List<ColumnName> columns) {

    @Override
    public boolean equals(final Object object) {
      return switch (object) {
        case RowKey other when this == other -> true;
        case RowKey other ->
            columns.stream()
                .allMatch(
                    column -> Objects.equals(row.getValue(column), other.row.getValue(column)));
        case null, default -> false;
      };
    }

    @Override
    public int hashCode() {
      return columns.stream()
          .map(column -> Objects.hashCode(row.getValue(column)))
          .reduce(1, (accumulated, hash) -> 31 * accumulated + hash);
    }
  }

  /**
   * A dataset implementation that holds merged tables with an optional data source.
   *
   * <p>This is a simple implementation used internally by the merger.
   *
   * @param tables the tables in this dataset
   * @param dataSource the data source for this dataset, or null
   */
  private record MergedDataSet(List<Table> tables, @Nullable DataSource dataSource)
      implements DataSet {

    /**
     * Creates a new merged dataset with immutable tables.
     *
     * @param tables the tables to include
     * @param dataSource the data source, or null
     */
    private MergedDataSet(final List<Table> tables, final @Nullable DataSource dataSource) {
      this.tables = List.copyOf(tables);
      this.dataSource = dataSource;
    }

    @Override
    public List<Table> getTables() {
      return tables;
    }

    @Override
    public Optional<Table> getTable(final TableName tableName) {
      return tables.stream().filter(table -> table.getName().equals(tableName)).findFirst();
    }

    @Override
    public Optional<DataSource> getDataSource() {
      return Optional.ofNullable(dataSource);
    }
  }
}
