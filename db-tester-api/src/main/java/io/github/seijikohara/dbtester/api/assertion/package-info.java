/**
 * Provides programmatic assertion utilities for database validation.
 *
 * <p>While {@link io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet} expresses
 * validation declaratively, this package offers APIs for scenarios that require imperative control.
 * {@link io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion} exposes comparison
 * primitives. {@link io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler} allows
 * callers to customize how mismatches are reported.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.assertion;

import org.jspecify.annotations.NullMarked;
