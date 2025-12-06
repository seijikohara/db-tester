package io.github.seijikohara.dbtester.internal.dbunit.assertion;

import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;

/** Internal helpers that build filtered views of DbUnit tables. */
final class ColumnFilter {

  /** Private constructor to prevent instantiation. */
  private ColumnFilter() {}

  /**
   * Filters a table to include only the specified columns.
   *
   * <p>This method creates a view of the table containing only the columns listed in the {@code
   * columnNames} parameter. The order of columns in the filtered table matches the original table.
   *
   * @param table the table to filter
   * @param columnNames the list of column names to include
   * @return a filtered table containing only the specified columns
   * @throws ValidationException if the table cannot be filtered
   */
  static ITable includeColumns(final ITable table, final Collection<ColumnName> columnNames) {
    try {
      final var columnNameValues =
          columnNames.stream().map(ColumnName::value).toArray(String[]::new);
      return DefaultColumnFilter.includedColumnsTable(table, columnNameValues);
    } catch (final DataSetException e) {
      throw new ValidationException("Failed to include columns in table", e);
    }
  }

  /**
   * Filters a table to exclude the specified columns.
   *
   * <p>This method creates a view of the table that excludes the columns listed in the {@code
   * columnNames} parameter. All other columns remain in their original order.
   *
   * @param table the table to filter
   * @param columnNames the list of column names to exclude
   * @return a filtered table excluding the specified columns
   * @throws ValidationException if the table cannot be filtered
   */
  static ITable excludeColumns(final ITable table, final Collection<ColumnName> columnNames) {
    try {
      final var columnNameValues =
          columnNames.stream().map(ColumnName::value).toArray(String[]::new);
      return DefaultColumnFilter.excludedColumnsTable(table, columnNameValues);
    } catch (final DataSetException e) {
      throw new ValidationException("Failed to exclude columns from table", e);
    }
  }

  /**
   * Filters a table to match the columns present in a template table.
   *
   * <p>This method is useful for ensuring that two tables have the same column structure before
   * comparison. It creates a view of the source table containing only the columns that exist in the
   * template table, in the same order as they appear in the template.
   *
   * @param source the table to filter
   * @param template the table whose column structure should be matched
   * @return a filtered table containing only the columns from the template
   * @throws ValidationException if the table cannot be filtered
   */
  static ITable matchColumns(final ITable source, final ITable template) {
    try {
      final var templateColumnNames =
          Stream.of(template.getTableMetaData().getColumns())
              .map(Column::getColumnName)
              .map(ColumnName::new)
              .collect(Collectors.toCollection(LinkedHashSet::new));
      return includeColumns(source, templateColumnNames);
    } catch (final DataSetException e) {
      throw new ValidationException("Failed to retrieve columns from table metadata", e);
    }
  }
}
