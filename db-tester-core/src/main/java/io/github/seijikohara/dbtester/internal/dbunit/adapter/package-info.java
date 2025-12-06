/**
 * Adapter classes that bridge DbUnit types to framework abstractions.
 *
 * <p>This package contains adapters that wrap DbUnit's {@code IDataSet}, {@code ITable}, and row
 * data to expose them through the framework's dataset interfaces. These adapters enable the rest of
 * the framework to work with datasets without direct DbUnit dependencies.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.adapter.DbUnitDataSetAdapter} - wraps
 *       IDataSet as framework DataSet
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.adapter.DbUnitTableAdapter} - wraps
 *       ITable as framework Table
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.adapter.DbUnitRowAdapter} - adapts
 *       table rows to framework Row
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dbunit.adapter;

import org.jspecify.annotations.NullMarked;
