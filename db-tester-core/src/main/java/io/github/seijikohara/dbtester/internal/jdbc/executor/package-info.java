/**
 * Executor implementations for database operations.
 *
 * <p>This package contains specialized executor classes that perform specific database operations
 * (INSERT, UPDATE, DELETE, TRUNCATE, REFRESH) on tables. All executors implement the {@link
 * TableExecutor} interface and are designed to be stateless and thread-safe.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.executor;

import org.jspecify.annotations.NullMarked;
