package io.github.seijikohara.dbtester.internal.jdbc.read;

import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.Jdbc.open;

import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.internal.util.TopologicalSorter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves table ordering based on foreign key relationships.
 *
 * <p>This class analyzes foreign key dependencies from database metadata and performs topological
 * sorting to produce an ordering that respects foreign key constraints.
 *
 * <p>For INSERT operations, parent tables (referenced tables) are processed before child tables
 * (tables with foreign keys). For DELETE operations, the order is reversed.
 *
 * <p>This class handles circular dependencies by detecting cycles and falling back to the original
 * table order for tables involved in cycles.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class TableOrderResolver {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(TableOrderResolver.class);

  /**
   * JDBC standard column name for the primary key table name in foreign key metadata.
   *
   * @see DatabaseMetaData#getImportedKeys(String, String, String)
   */
  private static final String PKTABLE_NAME = "PKTABLE_NAME";

  /** Creates a new table order resolver. */
  public TableOrderResolver() {}

  /**
   * Resolves table ordering based on foreign key relationships.
   *
   * <p>Tables are ordered so that parent tables (those referenced by foreign keys) come before
   * child tables (those with foreign keys). This ordering is suitable for INSERT operations.
   *
   * <p>If foreign key metadata cannot be retrieved or if circular dependencies are detected, the
   * original table order is preserved for affected tables.
   *
   * @param tableNames the table names to order
   * @param connection the database connection for metadata queries
   * @param schema the schema name, or null for the default schema
   * @return the ordered list of table names
   */
  public List<TableName> resolveOrder(
      final List<TableName> tableNames,
      final Connection connection,
      final @Nullable String schema) {
    if (tableNames.size() <= 1) {
      return tableNames;
    }

    try {
      final var dependencies = extractDependencies(tableNames, connection, schema);
      if (dependencies.isEmpty()) {
        logger.debug("No foreign key dependencies found, using original order");
        return tableNames;
      }

      final var sorted = TopologicalSorter.sort(tableNames, dependencies);
      logger.debug("Resolved table order based on foreign keys: {}", sorted);
      return sorted;
    } catch (final DatabaseTesterException e) {
      logger.warn("Failed to retrieve foreign key metadata, using original order", e);
      return tableNames;
    }
  }

  /**
   * Extracts a dependency graph from foreign key relationships.
   *
   * <p>The graph maps each table to the set of tables it depends on (i.e., tables it references via
   * foreign keys). Only tables in the provided list are considered.
   *
   * @param tableNames the table names to analyze
   * @param connection the database connection
   * @param schema the schema name, or null for the default schema
   * @return a map from table name to its dependencies (referenced tables)
   */
  private Map<TableName, Set<TableName>> extractDependencies(
      final List<TableName> tableNames,
      final Connection connection,
      final @Nullable String schema) {
    if (tableNames.isEmpty()) {
      return Map.of();
    }

    final var tableNameSet = Set.copyOf(tableNames);
    final var databaseMetaData = get(connection::getMetaData);
    final var catalog = get(connection::getCatalog);

    return tableNames.stream()
        .map(
            tableName -> {
              final var tableDependencies =
                  extractTableDependencies(
                      tableName, tableNameSet, databaseMetaData, catalog, schema);
              if (!tableDependencies.isEmpty()) {
                logger.trace("Table {} depends on: {}", tableName, tableDependencies);
              }
              return Map.entry(tableName, tableDependencies);
            })
        .filter(Predicate.not(entry -> entry.getValue().isEmpty()))
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Extracts foreign key dependencies for a single table.
   *
   * @param tableName the table to analyze
   * @param tableNameSet the set of table names in the dataset
   * @param databaseMetaData the database metadata
   * @param catalog the database catalog
   * @param schema the schema name, or null for the default schema
   * @return the set of tables this table depends on
   */
  private Set<TableName> extractTableDependencies(
      final TableName tableName,
      final Set<TableName> tableNameSet,
      final DatabaseMetaData databaseMetaData,
      final @Nullable String catalog,
      final @Nullable String schema) {
    try (final var resultSetResource =
        open(() -> databaseMetaData.getImportedKeys(catalog, schema, tableName.value()))) {
      return streamResultSet(resultSetResource.value(), rs -> get(() -> rs.getString(PKTABLE_NAME)))
          .map(
              referencedTableName ->
                  extractReferencedTableName(referencedTableName, tableName, tableNameSet))
          .flatMap(Optional::stream)
          .collect(Collectors.toUnmodifiableSet());
    }
  }

  /**
   * Extracts a referenced table name if it is valid and in the dataset.
   *
   * @param referencedTableName the referenced table name from metadata
   * @param currentTableName the current table being analyzed
   * @param tableNameSet the set of table names in the dataset
   * @return the referenced table name if valid, empty otherwise
   */
  private Optional<TableName> extractReferencedTableName(
      final @Nullable String referencedTableName,
      final TableName currentTableName,
      final Set<TableName> tableNameSet) {
    return Optional.ofNullable(referencedTableName)
        .map(TableName::new)
        .filter(tableNameSet::contains)
        .filter(Predicate.not(currentTableName::equals));
  }

  /**
   * Creates a Stream from a ResultSet using the provided row mapper.
   *
   * <p>The returned Stream is sequential and ordered. It does not close the underlying ResultSet;
   * the caller must manage the ResultSet lifecycle.
   *
   * @param <T> the type of elements in the resulting Stream
   * @param resultSet the ResultSet to stream over
   * @param rowMapper the function to map each row to the target type
   * @return a Stream of mapped elements
   */
  private static <T> Stream<T> streamResultSet(
      final ResultSet resultSet, final Function<ResultSet, T> rowMapper) {
    final var iterator = new ResultSetIterator<>(resultSet, rowMapper);
    final var spliterator =
        Spliterators.spliteratorUnknownSize(
            iterator, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE);
    return StreamSupport.stream(spliterator, false);
  }

  /**
   * An iterator adapter for JDBC ResultSet that enables Stream-based processing.
   *
   * @param <T> the type of elements returned by this iterator
   */
  private static final class ResultSetIterator<T> implements Iterator<T> {

    /** The underlying JDBC result set. */
    private final ResultSet resultSet;

    /** The function to map result set rows to domain objects. */
    private final Function<ResultSet, T> rowMapper;

    /** Whether hasNext() has been called without a subsequent next() call. */
    private boolean hasNextChecked;

    /** Cached result of the last hasNext() check. */
    private boolean hasNextResult;

    /**
     * Constructs a new ResultSetIterator.
     *
     * @param resultSet the result set to iterate over
     * @param rowMapper the function to map rows to domain objects
     */
    private ResultSetIterator(final ResultSet resultSet, final Function<ResultSet, T> rowMapper) {
      this.resultSet = resultSet;
      this.rowMapper = rowMapper;
      this.hasNextChecked = false;
      this.hasNextResult = false;
    }

    @Override
    public boolean hasNext() {
      if (hasNextChecked) {
        return hasNextResult;
      }
      hasNextResult = get(resultSet::next);
      hasNextChecked = true;
      return hasNextResult;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException("No more rows in ResultSet");
      }
      hasNextChecked = false;
      return rowMapper.apply(resultSet);
    }
  }
}
