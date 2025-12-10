package io.github.seijikohara.dbtester.internal.jdbc;

import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.get;
import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.open;

import io.github.seijikohara.dbtester.api.domain.TableName;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts foreign key dependencies from database metadata.
 *
 * <p>This class queries the database metadata to determine which tables reference other tables via
 * foreign keys. The result is a dependency graph that maps each table to the set of tables it
 * depends on.
 *
 * <p>Self-referencing foreign keys and references to tables outside the provided dataset are
 * ignored.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class ForeignKeyDependencyExtractor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(ForeignKeyDependencyExtractor.class);

  /**
   * JDBC standard column name for the primary key table name in foreign key metadata.
   *
   * <p>This constant represents the column name returned by {@link
   * DatabaseMetaData#getImportedKeys(String, String, String)} as defined in the JDBC 4.3
   * specification (JSR-221). The column contains the name of the primary key table being referenced
   * by a foreign key.
   *
   * <p>The {@code getImportedKeys} ResultSet includes the following standard columns:
   *
   * <ul>
   *   <li>{@code PKTABLE_CAT} - Primary key table catalog (may be null)
   *   <li>{@code PKTABLE_SCHEM} - Primary key table schema (may be null)
   *   <li>{@code PKTABLE_NAME} - Primary key table name
   *   <li>{@code PKCOLUMN_NAME} - Primary key column name
   *   <li>{@code FKTABLE_CAT} - Foreign key table catalog (may be null)
   *   <li>{@code FKTABLE_SCHEM} - Foreign key table schema (may be null)
   *   <li>{@code FKTABLE_NAME} - Foreign key table name
   *   <li>{@code FKCOLUMN_NAME} - Foreign key column name
   *   <li>{@code KEY_SEQ} - Sequence number within foreign key
   *   <li>{@code UPDATE_RULE} - Action on primary key update
   *   <li>{@code DELETE_RULE} - Action on primary key delete
   *   <li>{@code FK_NAME} - Foreign key name (may be null)
   *   <li>{@code PK_NAME} - Primary key name (may be null)
   *   <li>{@code DEFERRABILITY} - Foreign key constraint deferrability
   * </ul>
   *
   * @see DatabaseMetaData#getImportedKeys(String, String, String)
   * @see <a
   *     href="https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/DatabaseMetaData.html#getImportedKeys(java.lang.String,java.lang.String,java.lang.String)">
   *     JDBC 4.3 DatabaseMetaData.getImportedKeys</a>
   * @see <a href="https://jcp.org/en/jsr/detail?id=221">JSR-221: JDBC 4.0 API Specification</a>
   */
  private static final String PKTABLE_NAME = "PKTABLE_NAME";

  /** Creates a new foreign key dependency extractor. */
  public ForeignKeyDependencyExtractor() {}

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
   * @throws io.github.seijikohara.dbtester.api.exception.DatabaseOperationException if metadata
   *     retrieval fails
   */
  public Map<TableName, Set<TableName>> extract(
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
                logger.trace(
                    String.format("Table %s depends on: %s", tableName, tableDependencies));
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
   * @throws io.github.seijikohara.dbtester.api.exception.DatabaseOperationException if metadata
   *     retrieval fails
   */
  Set<TableName> extractTableDependencies(
      final TableName tableName,
      final Set<TableName> tableNameSet,
      final DatabaseMetaData databaseMetaData,
      final @Nullable String catalog,
      final @Nullable String schema) {
    try (final var resultSetResource =
        open(() -> databaseMetaData.getImportedKeys(catalog, schema, tableName.value()))) {
      return ResultSetIterator.stream(
              resultSetResource.value(), resultSet -> get(() -> resultSet.getString(PKTABLE_NAME)))
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
  Optional<TableName> extractReferencedTableName(
      final @Nullable String referencedTableName,
      final TableName currentTableName,
      final Set<TableName> tableNameSet) {
    return Optional.ofNullable(referencedTableName)
        .map(TableName::new)
        .filter(tableNameSet::contains)
        .filter(Predicate.not(currentTableName::equals));
  }
}
