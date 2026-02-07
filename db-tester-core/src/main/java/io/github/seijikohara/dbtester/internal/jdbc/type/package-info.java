/**
 * Custom type handler registry and implementations.
 *
 * <p>This package provides infrastructure for custom database type handling via the {@link
 * io.github.seijikohara.dbtester.api.spi.TypeHandler} SPI.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.type.TypeHandlerRegistry} - Registry
 *       for discovering and selecting type handlers
 *   <li>Database-specific handlers (e.g., PostgreSQL JSON, UUID, ARRAY)
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.type;

import org.jspecify.annotations.NullMarked;
