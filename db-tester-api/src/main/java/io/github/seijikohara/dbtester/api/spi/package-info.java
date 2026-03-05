/**
 * Provides Service Provider Interfaces (SPI) for framework extensibility.
 *
 * <p><strong>API Category: Extension API</strong> — Intended for framework integrators building
 * custom testing extensions. Test authors do not interact with this package directly.
 *
 * <p>These SPI interfaces decouple the API module from concrete implementations. The core module
 * provides implementations, loaded via {@link java.util.ServiceLoader}.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.AssertionProvider} — database assertion
 *       operations (data comparison)
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider} — query-based
 *       assertion operations (SQL query execution and comparison)
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider} — dataset loading
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.OperationProvider} — database operation
 *       execution
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.ExpectationProvider} — expectation
 *       verification
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.TypeHandler} — custom database type handling
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.api.spi;

import org.jspecify.annotations.NullMarked;
