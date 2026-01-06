package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Configuration properties for DB Tester Spring Boot integration.
 *
 * These properties control how DataSource beans from the Spring application context are
 * registered with the [io.github.seijikohara.dbtester.api.config.DataSourceRegistry], as well
 * as default conventions and operations for database testing.
 *
 * Properties are prefixed with `db-tester`.
 *
 * ## Available Properties
 *
 * - `db-tester.enabled` - Enable/disable DB Tester (default: true)
 * - `db-tester.auto-register-data-sources` - Auto-register DataSources (default: true)
 * - `db-tester.convention.*` - Convention settings for dataset resolution
 * - `db-tester.operation.*` - Default database operations
 */
@ConfigurationProperties(prefix = "db-tester")
class DbTesterProperties {
    /** Whether DB Tester is enabled. Defaults to true. */
    var isEnabled: Boolean = true

    /** Whether to automatically register DataSource beans. Defaults to true. */
    var isAutoRegisterDataSources: Boolean = true

    /** Convention settings for dataset resolution. */
    @NestedConfigurationProperty
    var convention: ConventionProperties = ConventionProperties()

    /** Default operation settings for preparation and expectation phases. */
    @NestedConfigurationProperty
    var operation: OperationProperties = OperationProperties()

    /**
     * Convention properties for dataset resolution.
     *
     * These properties define how dataset files are located and processed.
     */
    class ConventionProperties {
        /** Base directory for dataset resolution. Null means resolve from classpath relative to test class. */
        var baseDirectory: String? = null

        /** Suffix appended to preparation path for expectation datasets. */
        var expectationSuffix: String = ConventionSettings.DEFAULT_EXPECTATION_SUFFIX

        /** Column name that identifies scenario markers in dataset files. */
        var scenarioMarker: String = ConventionSettings.DEFAULT_SCENARIO_MARKER

        /** File format for dataset files. Defaults to CSV. */
        var dataFormat: DataFormat = DataFormat.CSV

        /** Strategy for merging tables from multiple datasets. Defaults to UNION_ALL. */
        var tableMergeStrategy: TableMergeStrategy = TableMergeStrategy.UNION_ALL

        /** File name for specifying table loading order in dataset directories. */
        var loadOrderFileName: String = ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME

        /** Column names to exclude globally from all expectation verifications. */
        var globalExcludeColumns: Set<String> = emptySet()
    }

    /**
     * Operation properties for database test phases.
     *
     * These properties define the default operations executed during test preparation and
     * expectation verification.
     */
    class OperationProperties {
        /** Default operation for test preparation phase. Defaults to CLEAN_INSERT. */
        var preparation: Operation = Operation.CLEAN_INSERT

        /** Default operation for expectation verification phase. Defaults to NONE. */
        var expectation: Operation = Operation.NONE
    }
}
