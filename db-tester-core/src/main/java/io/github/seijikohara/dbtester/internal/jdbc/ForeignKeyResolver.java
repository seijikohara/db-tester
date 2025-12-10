package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException;
import io.github.seijikohara.dbtester.internal.util.TopologicalSorter;
import java.sql.Connection;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves table ordering based on foreign key relationships.
 *
 * <p>This class coordinates foreign key dependency extraction and topological sorting to produce an
 * ordering that respects foreign key constraints.
 *
 * <p>For INSERT operations, parent tables (referenced tables) are processed before child tables
 * (tables with foreign keys). For DELETE operations, the order is reversed.
 *
 * <p>This class handles circular dependencies by detecting cycles and falling back to the original
 * table order for tables involved in cycles.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class ForeignKeyResolver {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(ForeignKeyResolver.class);

  /** The dependency extractor for foreign key relationships. */
  private final ForeignKeyDependencyExtractor dependencyExtractor;

  /** Creates a new foreign key resolver with default dependencies. */
  public ForeignKeyResolver() {
    this.dependencyExtractor = new ForeignKeyDependencyExtractor();
  }

  /**
   * Creates a new foreign key resolver with the specified dependency extractor.
   *
   * @param dependencyExtractor the dependency extractor
   */
  public ForeignKeyResolver(final ForeignKeyDependencyExtractor dependencyExtractor) {
    this.dependencyExtractor = dependencyExtractor;
  }

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
      final var dependencies = dependencyExtractor.extract(tableNames, connection, schema);
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
}
