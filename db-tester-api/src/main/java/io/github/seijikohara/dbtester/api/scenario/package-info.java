/**
 * Provides the scenario name resolution SPI for test framework integration.
 *
 * <p><strong>API Category: Extension API</strong> — Intended for framework integrators building
 * custom testing extensions. Test authors do not interact with this package directly.
 *
 * <p>This package defines the SPI for resolving test scenario names from test methods. Each test
 * framework (JUnit, Spock, Kotest) has its own naming convention, and this SPI allows each to
 * supply its own implementation.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.scenario.ScenarioName} — immutable value object
 *       representing a scenario identifier
 *   <li>{@link io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver} — SPI for
 *       resolving scenario names from test methods
 * </ul>
 *
 * <p>Scenario names filter rows in shared CSV/TSV files that contain data for multiple test
 * scenarios. When a test method name matches the scenario column value, the framework uses only
 * those rows for the test.
 *
 * @see io.github.seijikohara.dbtester.api.scenario.ScenarioName
 * @see io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
 */
@NullMarked
package io.github.seijikohara.dbtester.api.scenario;

import org.jspecify.annotations.NullMarked;
