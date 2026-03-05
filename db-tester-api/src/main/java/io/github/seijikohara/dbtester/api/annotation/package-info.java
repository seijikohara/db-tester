/**
 * Provides declarative annotations for dataset-driven database tests.
 *
 * <p>This package contains the following annotations:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.DataSet @DataSet} — defines datasets
 *       and database operations that establish pre-test state.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet @ExpectedDataSet} —
 *       specifies datasets representing the expected database state after a test.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.ExportDataSet @ExportDataSet} —
 *       exports the actual database state to files for debugging and analysis.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.DataSetSource @DataSetSource} —
 *       captures metadata required to locate and filter an individual dataset.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.ColumnStrategy @ColumnStrategy} —
 *       associates a column with a comparison strategy for expectation verification.
 * </ul>
 *
 * <p>When explicit locations are omitted, {@link
 * io.github.seijikohara.dbtester.api.config.ConventionSettings} maps test classes and methods to
 * dataset directories. Format-specific providers (CSV by default) are discovered via {@link
 * java.util.ServiceLoader}.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.annotation;

import org.jspecify.annotations.NullMarked;
