/**
 * Format-specific dataset readers for loading test data.
 *
 * <p>This package provides implementations for reading datasets from various file formats. Each
 * reader is responsible for parsing a specific format and converting it to the framework's dataset
 * abstraction.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.format.DataSetReader} - strategy
 *       interface for format-specific readers
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.format.CsvDataSetReader} - reads CSV
 *       files using DbUnit's CsvDataSet
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dbunit.format;

import org.jspecify.annotations.NullMarked;
