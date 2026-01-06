/**
 * Programmatic assertion utilities for fine-grained database validation.
 *
 * <p>Where {@link io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet} expresses
 * validation declaratively, this package provides APIs for scenarios that demand imperative
 * control. The {@link io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion} facade
 * exposes comparison primitives, while {@link
 * io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler} allows callers to customise
 * how mismatches are reported.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.assertion;

import org.jspecify.annotations.NullMarked;
