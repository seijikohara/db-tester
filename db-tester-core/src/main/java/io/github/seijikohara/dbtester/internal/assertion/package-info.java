/**
 * Internal assertion utilities for database state comparison.
 *
 * <p>This package provides internal implementations for comparing expected and actual database
 * states. The primary class is {@link
 * io.github.seijikohara.dbtester.internal.assertion.DataSetComparator} which handles table-level
 * comparisons with support for column filtering and custom failure handlers.
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.assertion;

import org.jspecify.annotations.NullMarked;
