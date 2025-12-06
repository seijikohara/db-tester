/**
 * Service Provider Interface (SPI) package for database tester.
 *
 * <p>This package contains interfaces that define extension points for the database testing
 * framework. Implementations are loaded via {@link java.util.ServiceLoader} to allow the API module
 * to remain independent of specific implementations.
 *
 * <p>Key SPIs in this package:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider} - Provides
 *       database assertion operations
 *   <li>{@link io.github.seijikohara.dbtester.internal.spi.DataSetLoaderProvider} - Provides the
 *       default dataset loader
 * </ul>
 *
 * @see java.util.ServiceLoader
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.spi;

import org.jspecify.annotations.NullMarked;
