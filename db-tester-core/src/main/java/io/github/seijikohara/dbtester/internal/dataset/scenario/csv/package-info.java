/**
 * CSV format implementation for scenario-based datasets.
 *
 * <p>This package provides a CSV-specific implementation of scenario-based dataset loading. It uses
 * DbUnit's CsvDataSet for parsing and wraps it with scenario filtering capabilities.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.scenario.csv.CsvScenarioDataSet} -
 *       CSV dataset with scenario filtering
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.scenario.csv.CsvScenarioTable} - CSV
 *       table with scenario-based row filtering
 *   <li>{@link io.github.seijikohara.dbtester.internal.dataset.scenario.csv.CsvFormatProvider} -
 *       format provider registered via SPI
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import org.jspecify.annotations.NullMarked;
