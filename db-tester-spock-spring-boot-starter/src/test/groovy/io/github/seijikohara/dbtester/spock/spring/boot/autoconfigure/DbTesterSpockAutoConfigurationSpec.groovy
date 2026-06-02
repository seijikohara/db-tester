package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
import io.github.seijikohara.dbtester.api.domain.Strategy
import io.github.seijikohara.dbtester.api.operation.Operation
import java.time.Duration
import javax.sql.DataSource
import org.springframework.beans.factory.ObjectProvider
import spock.lang.Specification

/**
 * Unit tests for {@link DbTesterSpockAutoConfiguration}.
 *
 * <p>This specification verifies the Spring Boot auto-configuration
 * for DB Tester with Spock.
 */
class DbTesterSpockAutoConfigurationSpec extends Specification {

	/** The auto-configuration under test. */
	DbTesterSpockAutoConfiguration autoConfiguration

	/** Test properties. */
	DbTesterProperties properties

	def setup() {
		autoConfiguration = new DbTesterSpockAutoConfiguration()
		properties = new DbTesterProperties()
	}

	def 'should create instance'() {
		when: 'creating a new instance'
		def instance = new DbTesterSpockAutoConfiguration()

		then: 'instance is created successfully'
		instance != null
	}

	def 'should return default Configuration'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration is not null'
		config != null
		config instanceof Configuration
	}

	def 'should return Configuration with conventions'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has conventions'
		config.conventions() != null
	}

	def 'should return Configuration with operations'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has operations'
		config.operations() != null
	}

	def 'should return Configuration with loader'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has loader'
		config.loader() != null
	}

	def 'should return DataSourceRegistry'() {
		given: 'an empty DataSource provider'
		def dataSourceProvider = createEmptyDataSourceProvider()

		when: 'getting dbTesterDataSourceRegistry'
		def registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)

		then: 'registry is not null'
		registry != null
		registry instanceof DataSourceRegistry
	}

	def 'should register DataSource as default when provided'() {
		given: 'a DataSource provider with one DataSource'
		def dataSource = Mock(DataSource)
		def dataSourceProvider = createDataSourceProvider(dataSource)

		when: 'getting dbTesterDataSourceRegistry'
		def registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)

		then: 'registry has default DataSource'
		registry.hasDefault()
	}

	def 'should return empty DataSourceRegistry when no DataSource provided'() {
		given: 'an empty DataSource provider'
		def dataSourceProvider = createEmptyDataSourceProvider()

		when: 'getting dbTesterDataSourceRegistry'
		def registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)

		then: 'registry has no default'
		!registry.hasDefault()
	}

	def 'should return new DataSourceRegistry on each call'() {
		given: 'an empty DataSource provider'
		def dataSourceProvider = createEmptyDataSourceProvider()

		when: 'getting dbTesterDataSourceRegistry twice'
		def registry1 = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)
		def registry2 = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)

		then: 'different instances are returned'
		!registry1.is(registry2)
	}

	def 'should return DataSourceRegistrar'() {
		when: 'getting dataSourceRegistrar'
		def registrar = autoConfiguration.dataSourceRegistrar(properties)

		then: 'registrar is not null'
		registrar != null
		registrar instanceof DataSourceRegistrar
	}

	def 'should return DataSourceRegistrar with provided properties'() {
		given: 'custom properties'
		def customProperties = new DbTesterProperties()
		customProperties.autoRegisterDataSources = false

		when: 'getting dataSourceRegistrar'
		def registrar = autoConfiguration.dataSourceRegistrar(customProperties)

		then: 'registrar is created with properties'
		registrar != null
	}

	def 'should return new DataSourceRegistrar on each call'() {
		when: 'getting dataSourceRegistrar twice'
		def registrar1 = autoConfiguration.dataSourceRegistrar(properties)
		def registrar2 = autoConfiguration.dataSourceRegistrar(properties)

		then: 'different instances are returned'
		!registrar1.is(registrar2)
	}

	def 'should create all beans successfully'() {
		given: 'an empty DataSource provider'
		def dataSourceProvider = createEmptyDataSourceProvider()

		when: 'creating all beans'
		def config = autoConfiguration.dbTesterConfiguration(properties)
		def registry = autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider)
		def registrar = autoConfiguration.dataSourceRegistrar(properties)

		then: 'all beans are created'
		config != null
		registry != null
		registrar != null
	}

	/** Verifies that Configuration has verification settings. */
	def 'should return Configuration with verification settings'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has verification'
		config.verification() != null
	}

	/** Verifies that Configuration has execution settings. */
	def 'should return Configuration with execution settings'() {
		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'configuration has execution'
		config.execution() != null
	}

	/** Verifies that custom convention properties are mapped to Configuration. */
	def 'should map custom convention properties to Configuration'() {
		given: 'custom convention properties'
		properties.convention.baseDirectory = '/custom/base'
		properties.convention.expectationSuffix = '/verify'
		properties.convention.scenarioMarker = '[TestCase]'
		properties.convention.dataFormat = DataFormat.TSV
		properties.convention.tableMergeStrategy = TableMergeStrategy.FIRST
		properties.convention.loadOrderFileName = 'custom-order.txt'

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'convention properties are mapped'
		def conventions = config.conventions()
		verifyAll {
			conventions.baseDirectory() == '/custom/base'
			conventions.expectationSuffix() == '/verify'
			conventions.scenarioMarker() == '[TestCase]'
			conventions.dataFormat() == DataFormat.TSV
			conventions.tableMergeStrategy() == TableMergeStrategy.FIRST
			conventions.loadOrderFileName() == 'custom-order.txt'
		}
	}

	/** Verifies that custom verification properties are mapped to Configuration. */
	def 'should map custom verification properties to Configuration'() {
		given: 'custom verification properties'
		properties.verification.globalExcludeColumns = ['created_at', 'updated_at'] as Set
		properties.verification.rowOrdering = RowOrdering.UNORDERED
		properties.verification.retryCount = 3
		properties.verification.retryDelay = Duration.ofSeconds(2)

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'verification properties are mapped'
		def verification = config.verification()
		verifyAll {
			verification.globalExcludeColumns() == ['created_at', 'updated_at'] as Set
			verification.rowOrdering() == RowOrdering.UNORDERED
			verification.retryCount() == 3
			verification.retryDelay() == Duration.ofSeconds(2)
		}
	}

	/** Verifies that custom execution properties are mapped to Configuration. */
	def 'should map custom execution properties to Configuration'() {
		given: 'custom execution properties'
		properties.execution.queryTimeout = Duration.ofSeconds(30)
		properties.execution.transactionMode = TransactionMode.AUTO_COMMIT

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'execution properties are mapped'
		def execution = config.execution()
		verifyAll {
			execution.queryTimeout() == Duration.ofSeconds(30)
			execution.transactionMode() == TransactionMode.AUTO_COMMIT
		}
	}

	/** Verifies that custom operation properties are mapped to Configuration. */
	def 'should map custom operation properties to Configuration'() {
		given: 'custom operation properties'
		properties.operation.preparation = Operation.INSERT
		properties.operation.expectation = Operation.DELETE_ALL

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'operation properties are mapped'
		def operations = config.operations()
		verifyAll {
			operations.preparation() == Operation.INSERT
			operations.expectation() == Operation.DELETE_ALL
		}
	}

	/** Verifies that column strategies properties are mapped to Configuration. */
	def 'should map column strategies properties to Configuration'() {
		given: 'custom column strategies'
		def timestampStrategy = new DbTesterProperties.ColumnStrategyProperty()
		timestampStrategy.columnName = 'CREATED_AT'
		timestampStrategy.strategy = Strategy.TIMESTAMP_FLEXIBLE

		def ignoreStrategy = new DbTesterProperties.ColumnStrategyProperty()
		ignoreStrategy.columnName = 'updated_at'
		ignoreStrategy.strategy = Strategy.IGNORE

		properties.verification.columnStrategies = [
			timestampStrategy,
			ignoreStrategy
		]

		when: 'getting dbTesterConfiguration'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'column strategies are mapped'
		def strategies = config.verification().globalColumnStrategies()
		verifyAll {
			strategies.size() == 2
			strategies.get('CREATED_AT').strategy() == ComparisonStrategy.TIMESTAMP_FLEXIBLE
			strategies.get('UPDATED_AT').strategy() == ComparisonStrategy.IGNORE
		}
	}

	/** Verifies that empty column strategies produces empty map. */
	def 'should produce empty column strategies when none configured'() {
		when: 'getting dbTesterConfiguration with default properties'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'column strategies are empty'
		config.verification().globalColumnStrategies().isEmpty()
	}

	/** Verifies that default properties produce default Configuration values. */
	def 'should produce default values for all Configuration sections'() {
		when: 'getting dbTesterConfiguration with default properties'
		def config = autoConfiguration.dbTesterConfiguration(properties)

		then: 'all default values are correct'
		verifyAll {
			config.conventions().baseDirectory() == null
			config.verification().rowOrdering() == RowOrdering.ORDERED
			config.verification().retryCount() == 0
			config.execution().queryTimeout() == null
			config.execution().transactionMode() == TransactionMode.SINGLE_TRANSACTION
			config.operations().preparation() == Operation.CLEAN_INSERT
			config.operations().expectation() == Operation.NONE
		}
	}

	/**
	 * Creates an empty ObjectProvider for DataSource.
	 *
	 * @return the mock provider
	 */
	private ObjectProvider<DataSource> createEmptyDataSourceProvider() {
		def provider = Mock(ObjectProvider)
		provider.stream() >> { java.util.stream.Stream.empty() }
		provider
	}

	/**
	 * Creates an ObjectProvider with a single DataSource.
	 *
	 * @param dataSource the DataSource to provide
	 * @return the mock provider
	 */
	private ObjectProvider<DataSource> createDataSourceProvider(DataSource dataSource) {
		def provider = Mock(ObjectProvider)
		provider.stream() >> { java.util.stream.Stream.of(dataSource) }
		provider
	}
}
