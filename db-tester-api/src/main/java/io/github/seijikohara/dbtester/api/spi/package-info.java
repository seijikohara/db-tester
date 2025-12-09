/**
 * Service Provider Interfaces (SPI) for framework extensibility.
 *
 * <p>This package contains SPI interfaces that allow the API module to remain independent of
 * specific implementations. Implementations are provided by the core module and loaded via {@link
 * java.util.ServiceLoader}.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.AssertionProvider} - Database assertion
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider} - Dataset loading
 *       operations
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.OperationProvider} - Database operation
 *       execution
 *   <li>{@link io.github.seijikohara.dbtester.api.spi.ExpectationProvider} - Expectation
 *       verification
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.api.spi;

import org.jspecify.annotations.NullMarked;
