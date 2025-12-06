package io.github.seijikohara.dbtester.internal.dbunit;

import io.github.seijikohara.dbtester.api.operation.Operation;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;

/**
 * Utility class for converting framework {@link Operation} to DbUnit {@link DatabaseOperation}.
 *
 * <p>This class provides a single point of conversion between the framework's operation enum and
 * DbUnit's database operations, ensuring consistent behavior across all DbUnit-related classes.
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This class is stateless and thread-safe. All methods are static and side-effect free.
 *
 * @see Operation
 * @see DatabaseOperation
 */
final class OperationConverter {

  /** Private constructor to prevent instantiation. */
  private OperationConverter() {
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Converts a framework {@link Operation} to the corresponding DbUnit {@link DatabaseOperation}.
   *
   * <p>The mapping is as follows:
   *
   * <table>
   *   <caption>Operation Mapping</caption>
   *   <tr><th>Framework Operation</th><th>DbUnit Operation</th></tr>
   *   <tr><td>NONE</td><td>DatabaseOperation.NONE</td></tr>
   *   <tr><td>UPDATE</td><td>DatabaseOperation.UPDATE</td></tr>
   *   <tr><td>INSERT</td><td>DatabaseOperation.INSERT</td></tr>
   *   <tr><td>REFRESH</td><td>DatabaseOperation.REFRESH</td></tr>
   *   <tr><td>DELETE</td><td>DatabaseOperation.DELETE</td></tr>
   *   <tr><td>DELETE_ALL</td><td>DatabaseOperation.DELETE_ALL</td></tr>
   *   <tr><td>TRUNCATE_TABLE</td><td>DatabaseOperation.TRUNCATE_TABLE</td></tr>
   *   <tr><td>CLEAN_INSERT</td><td>DatabaseOperation.CLEAN_INSERT</td></tr>
   *   <tr><td>TRUNCATE_INSERT</td><td>CompositeOperation(TRUNCATE_TABLE, INSERT)</td></tr>
   * </table>
   *
   * @param operation the framework operation to convert
   * @return the corresponding DbUnit database operation
   */
  static DatabaseOperation toDbUnitOperation(final Operation operation) {
    return switch (operation) {
      case NONE -> DatabaseOperation.NONE;
      case UPDATE -> DatabaseOperation.UPDATE;
      case INSERT -> DatabaseOperation.INSERT;
      case REFRESH -> DatabaseOperation.REFRESH;
      case DELETE -> DatabaseOperation.DELETE;
      case DELETE_ALL -> DatabaseOperation.DELETE_ALL;
      case TRUNCATE_TABLE -> DatabaseOperation.TRUNCATE_TABLE;
      case CLEAN_INSERT -> DatabaseOperation.CLEAN_INSERT;
      case TRUNCATE_INSERT ->
          new CompositeOperation(DatabaseOperation.TRUNCATE_TABLE, DatabaseOperation.INSERT);
    };
  }
}
