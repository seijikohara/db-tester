/**
 * Scenario-based dataset implementations.
 *
 * <p>This package provides implementations for scenario-based dataset loading, where test data can
 * be filtered by scenario names. This enables organizing multiple test scenarios within the same
 * data files.
 *
 * <p>Sub-packages contain format-specific implementations:
 *
 * <ul>
 *   <li>{@code csv} - CSV format implementation with scenario filtering
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.dataset.scenario;

import org.jspecify.annotations.NullMarked;
