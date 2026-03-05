/**
 * Provides database read operations for test data verification.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TableReader} — reads table data
 *       from the database
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TableOrderResolver} — resolves
 *       table ordering based on foreign key dependencies
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.read.TypeConverter} — converts LOB and
 *       special database types to standard Java types
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.read;

import org.jspecify.annotations.NullMarked;
