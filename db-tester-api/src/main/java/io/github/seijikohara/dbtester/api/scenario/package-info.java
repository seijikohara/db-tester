/**
 * Scenario name resolution API for test framework integration.
 *
 * <p>This package provides the SPI for resolving test scenario names from test methods. Different
 * test frameworks (JUnit, Spock, Kotest) have different conventions for naming tests, and this SPI
 * allows each framework to provide its own implementation.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.api.scenario.ScenarioName} - Immutable value object
 *       representing a scenario identifier
 *   <li>{@link io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver} - SPI for
 *       resolving scenario names from test methods
 * </ul>
 *
 * <p>Scenario names are used to filter rows in shared CSV/TSV files that contain data for multiple
 * test scenarios. When a test method name (or framework-specific feature name) matches the scenario
 * column value, only those rows are used for the test.
 *
 * @see io.github.seijikohara.dbtester.api.scenario.ScenarioName
 * @see io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
 */
@NullMarked
package io.github.seijikohara.dbtester.api.scenario;

import org.jspecify.annotations.NullMarked;
