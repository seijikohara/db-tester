/**
 * Assertion utilities for comparing datasets and tables using DbUnit.
 *
 * <p>This package provides internal assertion infrastructure for comparing expected and actual
 * database states. The assertions are built on top of DbUnit's comparison capabilities while
 * maintaining framework-level abstraction.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.assertion.DatabaseAssert} - main
 *       facade for dataset and table assertions
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.assertion.DataSetComparator} -
 *       performs structural and content comparisons between datasets
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.assertion.TableComparator} - performs
 *       cell-by-cell table comparisons
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.assertion.FailureHandlerAdapter} -
 *       bridges framework failure handlers to DbUnit
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dbunit.assertion;

import org.jspecify.annotations.NullMarked;
