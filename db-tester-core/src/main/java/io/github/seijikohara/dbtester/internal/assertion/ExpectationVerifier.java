package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.ExpectationContext;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableOrderResolver;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies database state matches expected dataset.
 *
 * <p>For each table in the expected dataset:
 *
 * <ol>
 *   <li>Retrieves actual data from database
 *   <li>Filters actual data to only include columns present in expected table
 *   <li>Compares filtered actual data against expected data
 * </ol>
 *
 * <p>Only columns present in expected dataset are compared, allowing partial column validation.
 *
 * <p>This class is stateless and thread-safe. All operations can be safely called from multiple
 * threads.
 */
public final class ExpectationVerifier {

  /** Logger for tracking operations. */
  private static final Logger logger = LoggerFactory.getLogger(ExpectationVerifier.class);

  /** Table reader for retrieving database state. */
  private final TableReader tableReader;

  /** Dataset comparator for assertions. */
  private final DataSetComparator comparator;

  /** Table order resolver for foreign key-based ordering. */
  private final TableOrderResolver tableOrderResolver;

  /** Creates a new expectation verifier with default dependencies. */
  public ExpectationVerifier() {
    this(new TableReader(), OperationDefaults.standard());
  }

  /**
   * Creates a new expectation verifier with specified dependencies.
   *
   * @param tableReader the table reader
   * @param operationDefaults the operation defaults for comparison
   */
  public ExpectationVerifier(
      final TableReader tableReader, final OperationDefaults operationDefaults) {
    this(tableReader, new DataSetComparator(operationDefaults));
  }

  /**
   * Creates a new expectation verifier with specified dependencies.
   *
   * @param tableReader the table reader
   * @param comparator the dataset comparator
   */
  public ExpectationVerifier(final TableReader tableReader, final DataSetComparator comparator) {
    this(tableReader, comparator, new TableOrderResolver());
  }

  /**
   * Creates a new expectation verifier with specified dependencies.
   *
   * @param tableReader the table reader
   * @param comparator the dataset comparator
   * @param tableOrderResolver the table order resolver
   */
  public ExpectationVerifier(
      final TableReader tableReader,
      final DataSetComparator comparator,
      final TableOrderResolver tableOrderResolver) {
    this.tableReader = tableReader;
    this.comparator = comparator;
    this.tableOrderResolver = tableOrderResolver;
  }

  /**
   * Verifies database state matches expected dataset using the specified context.
   *
   * <p>This method accepts an {@link ExpectationContext} parameter object that encapsulates all
   * optional verification parameters.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param context the verification context containing optional parameters
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final ExpectationContext context) {
    final var excludeColumns = context.excludeColumns();
    final var columnStrategies = context.columnStrategies();
    final var rowOrdering = context.rowOrdering();
    final var operationDefaults = context.operationDefaults();
    final var tableOrdering = context.tableOrdering();

    logger.debug(
        "Verifying expectation for {} tables with {} ordering, {} table ordering, and epsilon {}",
        expectedTableSet.tables().size(),
        rowOrdering,
        tableOrdering,
        operationDefaults.floatingPointEpsilon());

    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);
    final var effectiveColumnStrategies =
        columnStrategies.isEmpty() ? Map.<String, ColumnStrategyMapping>of() : columnStrategies;

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    if (!effectiveColumnStrategies.isEmpty()) {
      logger.debug("Using column strategies for: {}", effectiveColumnStrategies.keySet());
    }

    // Create a comparator with the specified operation defaults if different from instance default
    final var effectiveComparator =
        operationDefaults.floatingPointEpsilon() != OperationDefaults.DEFAULT_FLOATING_POINT_EPSILON
            ? new DataSetComparator(operationDefaults)
            : comparator;

    // Resolve table processing order
    final var orderedTables =
        resolveTableOrder(expectedTableSet.tables(), dataSource, tableOrdering);

    orderedTables.forEach(
        expectedTable -> {
          final var tableName = expectedTable.name().value();
          final var expectedColumns = expectedTable.columns();

          logger.trace(
              "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

          final var actualTable = tableReader.fetchTable(dataSource, tableName, expectedColumns);

          logger.trace(
              "Comparing table {}: expected {} rows, actual {} rows ({})",
              tableName,
              expectedTable.rowCount(),
              actualTable.rowCount(),
              rowOrdering);

          effectiveComparator.assertEqualsWithStrategies(
              expectedTable,
              actualTable,
              normalizedExcludeColumns,
              effectiveColumnStrategies,
              rowOrdering);
        });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.tables().size());
  }

  /**
   * Verifies database state matches expected dataset.
   *
   * <p>For each table in the expected dataset:
   *
   * <ol>
   *   <li>Retrieves actual data from database
   *   <li>Filters actual data to only include columns present in expected table
   *   <li>Compares filtered actual data against expected data
   * </ol>
   *
   * <p>Only columns present in expected dataset are compared, allowing partial column validation.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(final TableSet expectedTableSet, final DataSource dataSource) {
    verifyExpectation(expectedTableSet, dataSource, (Collection<String>) null);
  }

  /**
   * Verifies database state matches expected dataset, excluding specified columns.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource)} with column exclusion
   * support. Excluded columns are ignored during the comparison, which is useful for auto-generated
   * columns (timestamps, version numbers, auto-increment IDs) that cannot be predicted in test
   * data.
   *
   * <p>Column name matching is case-insensitive.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison, or null/empty for no exclusions
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final @Nullable Collection<String> excludeColumns) {
    logger.debug("Verifying expectation for {} tables", expectedTableSet.tables().size());

    // Normalize exclude columns to uppercase for case-insensitive matching
    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    expectedTableSet
        .tables()
        .forEach(
            expectedTable -> {
              final var tableName = expectedTable.name().value();
              final var expectedColumns = expectedTable.columns();

              logger.trace(
                  "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

              // Fetch actual table data with only the expected columns
              final var actualTable =
                  tableReader.fetchTable(dataSource, tableName, expectedColumns);

              logger.trace(
                  "Comparing table {}: expected {} rows, actual {} rows",
                  tableName,
                  expectedTable.rowCount(),
                  actualTable.rowCount());

              // Compare with or without column exclusion
              if (normalizedExcludeColumns.isEmpty()) {
                comparator.assertEquals(expectedTable, actualTable, null);
              } else {
                comparator.assertEqualsIgnoreColumns(
                    expectedTable, actualTable, normalizedExcludeColumns);
              }
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.tables().size());
  }

  /**
   * Verifies database state matches expected dataset with column comparison strategies.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource, Collection)} with
   * column-specific comparison strategy support. Each column can have its own comparison strategy
   * (IGNORE, CASE_INSENSITIVE, NUMERIC, TIMESTAMP_FLEXIBLE, NOT_NULL, REGEX, or STRICT).
   *
   * <p>Column exclusion takes precedence: columns in excludeColumns are skipped entirely before
   * column strategies are applied.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison, or null/empty for no exclusions
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final @Nullable Collection<String> excludeColumns,
      final @Nullable Map<String, ColumnStrategyMapping> columnStrategies) {
    // If no column strategies, delegate to simpler method
    if (columnStrategies == null || columnStrategies.isEmpty()) {
      verifyExpectation(expectedTableSet, dataSource, excludeColumns);
      return;
    }

    logger.debug("Verifying expectation for {} tables", expectedTableSet.tables().size());

    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    logger.debug("Using column strategies for: {}", columnStrategies.keySet());

    expectedTableSet
        .tables()
        .forEach(
            expectedTable -> {
              final var tableName = expectedTable.name().value();
              final var expectedColumns = expectedTable.columns();

              logger.trace(
                  "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

              final var actualTable =
                  tableReader.fetchTable(dataSource, tableName, expectedColumns);

              logger.trace(
                  "Comparing table {}: expected {} rows, actual {} rows",
                  tableName,
                  expectedTable.rowCount(),
                  actualTable.rowCount());

              comparator.assertEqualsWithStrategies(
                  expectedTable, actualTable, normalizedExcludeColumns, columnStrategies);
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.tables().size());
  }

  /**
   * Verifies database state matches expected dataset with row ordering control.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource, Collection, Map)} with
   * row ordering support. When set to {@link RowOrdering#UNORDERED}, rows are compared without
   * considering their position, using set-based matching.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison, or null/empty for no exclusions
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final @Nullable Collection<String> excludeColumns,
      final @Nullable Map<String, ColumnStrategyMapping> columnStrategies,
      final RowOrdering rowOrdering) {
    logger.debug(
        "Verifying expectation for {} tables with {} ordering",
        expectedTableSet.tables().size(),
        rowOrdering);

    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);
    final var effectiveColumnStrategies =
        columnStrategies != null ? columnStrategies : Map.<String, ColumnStrategyMapping>of();

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    if (!effectiveColumnStrategies.isEmpty()) {
      logger.debug("Using column strategies for: {}", effectiveColumnStrategies.keySet());
    }

    expectedTableSet
        .tables()
        .forEach(
            expectedTable -> {
              final var tableName = expectedTable.name().value();
              final var expectedColumns = expectedTable.columns();

              logger.trace(
                  "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

              final var actualTable =
                  tableReader.fetchTable(dataSource, tableName, expectedColumns);

              logger.trace(
                  "Comparing table {}: expected {} rows, actual {} rows ({})",
                  tableName,
                  expectedTable.rowCount(),
                  actualTable.rowCount(),
                  rowOrdering);

              comparator.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  normalizedExcludeColumns,
                  effectiveColumnStrategies,
                  rowOrdering);
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.tables().size());
  }

  /**
   * Verifies database state matches expected dataset with operation defaults.
   *
   * <p>This method extends {@link #verifyExpectation(TableSet, DataSource, Collection, Map,
   * RowOrdering)} with operation defaults support. The operation defaults contain comparison
   * settings such as the floating-point epsilon for numeric comparisons.
   *
   * <p>This method creates a new comparator with the specified operation defaults for each
   * verification to ensure the correct epsilon is used.
   *
   * @param expectedTableSet the expected dataset containing expected table data
   * @param dataSource the database connection source for retrieving actual data
   * @param excludeColumns column names to exclude from comparison, or null/empty for no exclusions
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
   * @param operationDefaults the operation defaults containing comparison settings
   * @throws AssertionError if verification fails
   */
  public void verifyExpectation(
      final TableSet expectedTableSet,
      final DataSource dataSource,
      final @Nullable Collection<String> excludeColumns,
      final @Nullable Map<String, ColumnStrategyMapping> columnStrategies,
      final RowOrdering rowOrdering,
      final OperationDefaults operationDefaults) {
    logger.debug(
        "Verifying expectation for {} tables with {} ordering and epsilon {}",
        expectedTableSet.tables().size(),
        rowOrdering,
        operationDefaults.floatingPointEpsilon());

    final var normalizedExcludeColumns = normalizeExcludeColumns(excludeColumns);
    final var effectiveColumnStrategies =
        columnStrategies != null ? columnStrategies : Map.<String, ColumnStrategyMapping>of();

    // Create a comparator with the specified operation defaults
    final var effectiveComparator = new DataSetComparator(operationDefaults);

    if (!normalizedExcludeColumns.isEmpty()) {
      logger.debug("Excluding columns from verification: {}", normalizedExcludeColumns);
    }

    if (!effectiveColumnStrategies.isEmpty()) {
      logger.debug("Using column strategies for: {}", effectiveColumnStrategies.keySet());
    }

    expectedTableSet
        .tables()
        .forEach(
            expectedTable -> {
              final var tableName = expectedTable.name().value();
              final var expectedColumns = expectedTable.columns();

              logger.trace(
                  "Fetching table {} with {} expected columns", tableName, expectedColumns.size());

              final var actualTable =
                  tableReader.fetchTable(dataSource, tableName, expectedColumns);

              logger.trace(
                  "Comparing table {}: expected {} rows, actual {} rows ({})",
                  tableName,
                  expectedTable.rowCount(),
                  actualTable.rowCount(),
                  rowOrdering);

              effectiveComparator.assertEqualsWithStrategies(
                  expectedTable,
                  actualTable,
                  normalizedExcludeColumns,
                  effectiveColumnStrategies,
                  rowOrdering);
            });

    logger.debug(
        "Successfully verified expectation for {} tables", expectedTableSet.tables().size());
  }

  /**
   * Normalizes exclude column names for case-insensitive matching.
   *
   * @param excludeColumns the column names to normalize, may be null or empty
   * @return set of uppercase column names, empty set if input is null or empty
   */
  private Set<String> normalizeExcludeColumns(final @Nullable Collection<String> excludeColumns) {
    if (excludeColumns == null || excludeColumns.isEmpty()) {
      return Set.of();
    }
    return excludeColumns.stream()
        .map(column -> column.toUpperCase(Locale.ROOT))
        .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Resolves the table processing order based on the specified strategy.
   *
   * @param tables the original table list
   * @param dataSource the data source for metadata queries
   * @param strategy the table ordering strategy
   * @return the reordered table list
   */
  private List<Table> resolveTableOrder(
      final List<Table> tables, final DataSource dataSource, final TableOrderingStrategy strategy) {
    if (tables.size() <= 1) {
      return tables;
    }

    return switch (strategy) {
      case AUTO -> resolveTableOrderAuto(tables, dataSource);
      case LOAD_ORDER_FILE -> tables; // Already ordered by load order file during dataset loading
      case FOREIGN_KEY -> resolveTableOrderByForeignKey(tables, dataSource);
      case ALPHABETICAL -> resolveTableOrderAlphabetically(tables);
    };
  }

  /**
   * Resolves table order using AUTO strategy.
   *
   * <p>AUTO strategy attempts foreign key resolution via JDBC metadata and falls back to the
   * original order if resolution fails.
   *
   * @param tables the original table list
   * @param dataSource the data source for metadata queries
   * @return the reordered table list
   */
  private List<Table> resolveTableOrderAuto(final List<Table> tables, final DataSource dataSource) {
    try (final var connection = dataSource.getConnection()) {
      final var schema = getSchema(connection).orElse(null);
      final var tableNames = tables.stream().map(Table::name).toList();
      final var orderedNames = tableOrderResolver.resolveOrder(tableNames, connection, schema);

      if (!tableNames.equals(orderedNames)) {
        logger.debug("Resolved verification table order based on foreign keys: {}", orderedNames);
        final var tableMap =
            tables.stream().collect(Collectors.toMap(Table::name, Function.identity()));
        return orderedNames.stream().map(tableMap::get).toList();
      }
    } catch (final SQLException e) {
      logger.debug(
          "Foreign key resolution failed for verification, using original order: {}",
          e.getMessage());
    }

    return tables;
  }

  /**
   * Resolves table order based on foreign key relationships.
   *
   * @param tables the original table list
   * @param dataSource the data source for metadata queries
   * @return the reordered table list
   */
  private List<Table> resolveTableOrderByForeignKey(
      final List<Table> tables, final DataSource dataSource) {
    try (final var connection = dataSource.getConnection()) {
      final var schema = getSchema(connection).orElse(null);
      final var tableNames = tables.stream().map(Table::name).toList();
      final var orderedNames = tableOrderResolver.resolveOrder(tableNames, connection, schema);

      if (tableNames.equals(orderedNames)) {
        logger.debug("No foreign key dependencies found for verification, using original order");
        return tables;
      }

      logger.debug("Resolved verification table order based on foreign keys: {}", orderedNames);
      final var tableMap =
          tables.stream().collect(Collectors.toMap(Table::name, Function.identity()));
      return orderedNames.stream().map(tableMap::get).toList();

    } catch (final SQLException e) {
      logger.warn(
          "Failed to resolve verification table order based on foreign keys, using original order",
          e);
      return tables;
    }
  }

  /**
   * Resolves table order alphabetically by table name.
   *
   * @param tables the original table list
   * @return the alphabetically sorted table list
   */
  private List<Table> resolveTableOrderAlphabetically(final List<Table> tables) {
    logger.debug("Sorting verification tables alphabetically");
    return tables.stream()
        .sorted(Comparator.comparing(table -> table.name().value().toLowerCase(Locale.ROOT)))
        .toList();
  }

  /**
   * Gets the schema from the connection.
   *
   * @param connection the database connection
   * @return an Optional containing the schema name, or empty if not available
   */
  private Optional<String> getSchema(final Connection connection) {
    try {
      return Optional.ofNullable(connection.getSchema());
    } catch (final SQLException e) {
      logger.debug("Failed to retrieve schema: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
