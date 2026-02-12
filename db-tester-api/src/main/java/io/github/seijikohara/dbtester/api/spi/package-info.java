/**
 * Service Provider Interfaces (SPI) for framework extensibility.
 *
 * <p><strong>API Category: Extension API</strong> — This package is intended for framework
 * integrators building custom testing extensions. Test authors typically do not interact with this
 * package directly.
 *
 * <p>This package contains SPI interfaces that allow the API module to remain independent of
 * specific implementations. Implementations are provided by the core module and loaded via {@link
 * java.util.ServiceLoader}.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.AssertionProvider} - Database assertion
 *       operations (data comparison)
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider} - Query-based
 *       database assertion operations (SQL query execution and comparison)
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider} - Dataset loading
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.OperationProvider} - Database operation
 *       execution
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.ExpectationProvider} - Expectation
 *       verification
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.TypeHandler} - Custom database type handling
 * </ul>
 *
 * <p><strong>Audience:</strong> Framework integrators and extension developers. Most users do not
 * need to interact with this package directly.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.spi;

import org.jspecify.annotations.NullMarked;
