/**
 * Provides type-safe domain value objects for database identifiers, metadata, and values.
 *
 * <p><strong>API Category: Domain Types</strong> — Shared value objects used across the User API
 * and Extension API.
 *
 * <p>Identifiers:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.TableName} — database table identifier
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ColumnName} — column name wrapper
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.DataSourceName} — DataSource identifier
 * </ul>
 *
 * <p>Domain models:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.Column} — column with metadata and
 *       comparison strategy
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ColumnMetadata} — schema metadata (type,
 *       constraints, precision)
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.Cell} — column-value pair with comparison
 *       support
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.CellValue} — cell values with NULL
 *       handling
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ComparisonStrategy} — configurable value
 *       comparison strategies
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.api.domain;

import org.jspecify.annotations.NullMarked;
