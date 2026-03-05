/**
 * Provides data format parsing and loading infrastructure.
 *
 * <p>This package defines the SPI and implementations for parsing data files (CSV, TSV, JSON, YAML)
 * into {@link io.github.seijikohara.dbtester.api.dataset.TableSet} objects.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.spi.FormatProvider} — SPI for format
 *       support
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.spi.FormatRegistry} — provider
 *       registration and lookup
 *   <li>{@link io.github.seijikohara.dbtester.internal.format.parser.DelimitedParser} — CSV/TSV
 *       parser
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.dataset
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.format;

import org.jspecify.annotations.NullMarked;
