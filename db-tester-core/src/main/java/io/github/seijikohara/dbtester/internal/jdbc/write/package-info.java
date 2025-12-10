/**
 * Database write operations for test data setup.
 *
 * <p>This package provides classes for writing data to databases, including insert, update, delete,
 * truncate, and refresh operations.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.OperationExecutor} - Coordinates
 *       database write operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.SqlBuilder} - Builds SQL
 *       statements for database operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.ParameterBinder} - Binds
 *       parameters to PreparedStatement with type conversion
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.InsertExecutor} - Executes INSERT
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.UpdateExecutor} - Executes UPDATE
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.DeleteExecutor} - Executes DELETE
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.TruncateExecutor} - Executes
 *       TRUNCATE operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.RefreshExecutor} - Executes
 *       REFRESH (upsert) operations
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.write;

import org.jspecify.annotations.NullMarked;
