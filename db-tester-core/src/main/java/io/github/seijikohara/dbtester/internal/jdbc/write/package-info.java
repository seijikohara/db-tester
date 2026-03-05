/**
 * Provides database write operations for test data setup.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.OperationExecutor} — coordinates
 *       database write operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.SqlBuilder} — builds SQL
 *       statements for database operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.ParameterBinder} — binds
 *       parameters to PreparedStatement with type conversion
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.InsertExecutor} — executes INSERT
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.UpdateExecutor} — executes UPDATE
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.DeleteExecutor} — executes DELETE
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.TruncateExecutor} — executes
 *       TRUNCATE operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.write.UpsertExecutor} — executes UPSERT
 *       operations
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.write;

import org.jspecify.annotations.NullMarked;
