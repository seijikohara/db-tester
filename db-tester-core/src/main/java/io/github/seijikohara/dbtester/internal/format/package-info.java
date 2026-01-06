/**
 * Data format parsing and loading infrastructure.
 *
 * <p>This package provides the Service Provider Interface (SPI) and implementations for parsing
 * various data file formats (CSV, TSV, etc.) into {@link
 * io.github.seijikohara.dbtester.api.dataset.TableSet} objects.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.spi.FormatProvider} - SPI for format
 *       support
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.spi.FormatRegistry} - Provider
 *       registration and lookup
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.parser.DelimitedParser} - CSV/TSV
 *       parser
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.TableOrdering} - Table ordering file
 *       management
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.dataset
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.format;

import org.jspecify.annotations.NullMarked;
