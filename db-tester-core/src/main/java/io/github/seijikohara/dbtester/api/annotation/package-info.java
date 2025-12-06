/**
 * Declarative annotations that describe dataset-driven database tests.
 *
 * <p>The package contains three cooperating annotations:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.Preparation @Preparation} defines the
 *       datasets and database operations that establish the pre-test state.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.Expectation @Expectation} specifies
 *       the datasets that capture the expected database state after a test.
 *   <li>{@link io.github.seijikohara.dbtester.api.annotation.DataSet @DataSet} captures the
 *       fine-grained metadata required to locate and filter an individual dataset, regardless of
 *       the underlying file format.
 * </ul>
 *
 * <p>When explicit locations are omitted the {@link
 * io.github.seijikohara.dbtester.api.config.ConventionSettings} are consulted to map test classes
 * and methods to dataset directories. Format-specific providers (CSV by default) are discovered via
 * the service loader.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.annotation;

import org.jspecify.annotations.NullMarked;
