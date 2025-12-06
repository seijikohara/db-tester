/**
 * Internal dataset implementations and utilities.
 *
 * <p>This package provides internal implementations of the dataset interfaces defined in the API
 * module, including:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.SimpleDataSet} - basic dataset
 *       implementation
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.SimpleTable} - basic table
 *       implementation
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.SimpleRow} - basic row
 *       implementation
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.ScenarioDataSetFactory} - factory
 *       for creating scenario-filtered datasets
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dataset;

import org.jspecify.annotations.NullMarked;
