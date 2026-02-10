/**
 * Type-safe domain value objects for database identifiers, metadata, and values.
 *
 * <p><strong>API Category: Domain Types</strong> — Shared value objects used across both User API
 * and Extension API. These types appear in method signatures throughout the framework.
 *
 * <p>This package contains immutable value objects that provide compile-time type safety and rich
 * domain modeling for database testing.
 *
 * <p>Core identifiers:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.TableName} - Database table identifiers
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ColumnName} - Simple column name wrapper
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.DataSourceName} - DataSource identifiers
 * </ul>
 *
 * <p>Rich domain models:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.Column} - Column with metadata and
 *       comparison strategy
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ColumnMetadata} - Schema metadata (type,
 *       constraints, precision)
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.Cell} - Column-value pair with comparison
 *       support
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.CellValue} - Cell values with NULL
 *       handling
 *   <li>{@link io.github.seijikohara.dbtester.api.domain.ComparisonStrategy} - Configurable value
 *       comparison strategies
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.api.domain;

import org.jspecify.annotations.NullMarked;
