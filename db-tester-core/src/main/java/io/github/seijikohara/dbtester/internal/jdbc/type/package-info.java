/**
 * Provides the type handler registry and database-specific implementations.
 *
 * <p>This package supports custom database type handling via the {@link
 * io.github.seijikohara.dbtester.api.spi.TypeHandler} SPI.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.jdbc.type.TypeHandlerRegistry} — registry
 *       for discovering and selecting type handlers
 *   <li>Database-specific handlers for PostgreSQL JSON, UUID, and ARRAY types
 * </ul>
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.type;

import org.jspecify.annotations.NullMarked;
