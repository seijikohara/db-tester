/**
 * Database read operations for test data verification.
 *
 * <p>This package provides classes for reading data from databases, including table content
 * retrieval, foreign key dependency resolution, and LOB type conversion.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TableReader} - Reads table data
 *       from the database
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TableOrderResolver} - Resolves
 *       table ordering based on foreign key dependencies
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TypeConverter} - Converts LOB and
 *       special database types to standard Java types
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.read;

import org.jspecify.annotations.NullMarked;
