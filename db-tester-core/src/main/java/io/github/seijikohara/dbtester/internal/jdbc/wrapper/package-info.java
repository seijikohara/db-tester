/**
 * JDBC exception wrapping utilities.
 *
 * <p>This package provides utilities to convert checked SQLException to unchecked
 * DatabaseOperationException, enabling cleaner code with functional programming patterns.
 *
 * <p>The utilities allow JDBC operations to be used in lambda expressions and Stream chains without
 * the need for explicit try-catch blocks at each call site.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.jdbc.wrapper;

import org.jspecify.annotations.NullMarked;
