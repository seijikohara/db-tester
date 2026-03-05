/**
 * Provides format-agnostic dataset abstractions for database tables and rows.
 *
 * <p><strong>API Category: Domain Types</strong> — Shared value objects used across the User API
 * and Extension API.
 *
 * <p>This package defines the following interfaces:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.TableSet} — a collection of tables
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.Table} — a single database table with
 *       columns and rows
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.Row} — a single row within a table
 * </ul>
 *
 * <p>The core module implements these interfaces. The framework uses them for data loading,
 * comparison, and validation.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.dataset;

import org.jspecify.annotations.NullMarked;
