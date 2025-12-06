/**
 * DbUnit integration layer providing complete isolation of DbUnit dependencies.
 *
 * <p>This package encapsulates all direct DbUnit interactions, ensuring that DbUnit types never
 * leak outside this package hierarchy. Key classes include:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.DbUnitOperations} - executes database
 *       operations (CLEAN_INSERT, INSERT, REFRESH, DELETE) using DbUnit
 *   <li>{@link io.github.seijikohara.dbtester.internal.dbunit.DbUnitDataSetAdapter} - adapts
 *       framework DataSet to DbUnit IDataSet
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dbunit;

import org.jspecify.annotations.NullMarked;
