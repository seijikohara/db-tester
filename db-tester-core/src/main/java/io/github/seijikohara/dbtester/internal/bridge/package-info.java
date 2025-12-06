/**
 * Bridge layer providing SPI implementations for database operations.
 *
 * <p>This package contains the {@link
 * io.github.seijikohara.dbtester.internal.bridge.DatabaseBridgeProviderImpl} which implements the
 * {@link io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider} SPI interface. It
 * serves as the primary entry point for database operations, delegating to DbUnit for the actual
 * work.
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.bridge;

import org.jspecify.annotations.NullMarked;
