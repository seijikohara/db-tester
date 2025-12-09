package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties for DB Tester Spring Boot integration with Spock.
 *
 * <p>These properties control how DataSource beans from the Spring application context are
 * registered with the {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry}, as well
 * as default conventions and operations for database testing.
 *
 * <p>Properties are prefixed with {@code db-tester}.
 *
 * <h2>Available Properties</h2>
 *
 * <ul>
 *   <li>{@code db-tester.enabled} - Enable/disable DB Tester (default: true)
 *   <li>{@code db-tester.auto-register-data-sources} - Auto-register DataSources (default: true)
 *   <li>{@code db-tester.convention.*} - Convention settings for dataset resolution
 *   <li>{@code db-tester.operation.*} - Default database operations
 * </ul>
 */
@ConfigurationProperties(prefix = "db-tester")
class DbTesterProperties {

	/** Whether DB Tester is enabled. Defaults to true. */
	boolean enabled = true

	/** Whether to automatically register DataSource beans. Defaults to true. */
	boolean autoRegisterDataSources = true

	/** Convention settings for dataset resolution. */
	@NestedConfigurationProperty
	ConventionProperties convention = new ConventionProperties()

	/** Default operation settings for preparation and expectation phases. */
	@NestedConfigurationProperty
	OperationProperties operation = new OperationProperties()

	/**
	 * Convention properties for dataset resolution.
	 *
	 * <p>These properties define how dataset files are located and processed.
	 *
	 * <h2>Available Properties</h2>
	 *
	 * <ul>
	 *   <li>{@code db-tester.convention.base-directory} - Base directory for datasets (default: null,
	 *       uses classpath)
	 *   <li>{@code db-tester.convention.expectation-suffix} - Suffix for expectation directories
	 *       (default: /expected)
	 *   <li>{@code db-tester.convention.scenario-marker} - Column name for scenario filtering
	 *       (default: [Scenario])
	 *   <li>{@code db-tester.convention.data-format} - Dataset file format (default: CSV)
	 *   <li>{@code db-tester.convention.table-merge-strategy} - Strategy for merging tables (default:
	 *       UNION_ALL)
	 * </ul>
	 */
	static class ConventionProperties {

		/**
		 * Base directory for dataset resolution. Null means resolve from classpath relative to test
		 * class.
		 */
		String baseDirectory

		/** Suffix appended to preparation path for expectation datasets. Defaults to "/expected". */
		String expectationSuffix = "/expected"

		/** Column name that identifies scenario markers in dataset files. Defaults to "[Scenario]". */
		String scenarioMarker = "[Scenario]"

		/** File format for dataset files. Defaults to CSV. */
		DataFormat dataFormat = DataFormat.CSV

		/** Strategy for merging tables from multiple datasets. Defaults to UNION_ALL. */
		TableMergeStrategy tableMergeStrategy = TableMergeStrategy.UNION_ALL
	}

	/**
	 * Operation properties for database test phases.
	 *
	 * <p>These properties define the default operations executed during test preparation and
	 * expectation verification.
	 *
	 * <h2>Available Properties</h2>
	 *
	 * <ul>
	 *   <li>{@code db-tester.operation.preparation} - Default preparation operation (default:
	 *       CLEAN_INSERT)
	 *   <li>{@code db-tester.operation.expectation} - Default expectation operation (default: NONE)
	 * </ul>
	 */
	static class OperationProperties {

		/** Default operation for test preparation phase. Defaults to CLEAN_INSERT. */
		Operation preparation = Operation.CLEAN_INSERT

		/** Default operation for expectation verification phase. Defaults to NONE. */
		Operation expectation = Operation.NONE
	}
}
