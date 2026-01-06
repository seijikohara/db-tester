/**
 * Dataset abstraction for representing database tables and rows.
 *
 * <p>This package contains the core interfaces for representing database data in a format-agnostic
 * way. The main interfaces are:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.TableSet} - A collection of tables
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.Table} - A single database table with
 *       columns and rows
 *   <li>{@link io.github.seijikohara.dbtester.api.dataset.Row} - A single row within a table
 * </ul>
 *
 * <p>These interfaces are implemented by the core module and are used throughout the framework for
 * data loading, comparison, and validation.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.dataset;

import org.jspecify.annotations.NullMarked;
