package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.domain.Strategy
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates template expression processing in CSV dataset values.
 *
 * <p>Template expressions generate dynamic values at data load time. This specification covers:
 * <ul>
 *   <li>{@code ${uuid}} - Random UUID generation
 *   <li>{@code ${sequence:N}} and {@code ${sequence}} - Auto-incrementing sequence numbers
 *   <li>{@code ${now}} and {@code ${now+Xd}} - Current and relative timestamp generation
 *   <li>{@code ${faker.xxx.yyy}} - Datafaker integration for realistic test data
 * </ul>
 *
 * <p>Dynamic values are validated using {@link Strategy#REGEX} and {@link Strategy#NOT_NULL}
 * comparison strategies in the expected dataset.
 */
class TemplateExpressionSpec extends Specification {

	/**
	 * Tests {@code ${uuid}} template expression.
	 *
	 * <p>The {@code ${uuid}} expression generates a random UUID (version 4) for each occurrence. The
	 * expected dataset uses {@link Strategy#REGEX} to validate the UUID format.
	 */
	@DatabaseTest
	static class UuidExpressionSpec extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TemplateExpressionSpec_UUID;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TemplateExpressionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that {@code ${uuid}} generates valid UUID values.
		 */
		@DataSet(
		operation = Operation.CLEAN_INSERT,
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$UuidExpressionSpec/shouldGenerateUuid/'))
		@ExpectedDataSet(
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$UuidExpressionSpec/shouldGenerateUuid/expected/',
		columnStrategies = @ColumnStrategy(
		name = 'UUID_VALUE',
		strategy = Strategy.REGEX,
		pattern = '[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}')))
		def 'shouldGenerateUuid'() {
			when: 'data is loaded with ${uuid} expressions'
			// preparation has executed by the time this block runs
			then: 'UUID format is validated via the REGEX strategy'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
		 */
		private void executeScript(String scriptPath) {
			def resource = getClass().classLoader.getResource(scriptPath)
			if (resource == null) {
				throw new IllegalStateException("Script not found: $scriptPath")
			}
			resource.text
					.split(';')
					.collect { it.trim() }
					.findAll { !it.empty }
					.each { sql.execute(it) }
		}
	}

	/**
	 * Tests {@code ${sequence}} template expressions.
	 *
	 * <p>The {@code ${sequence:N}} expression sets the counter to N and returns N. The {@code
	 * ${sequence}} expression increments the counter and returns the next value.
	 */
	@DatabaseTest
	static class SequenceExpressionSpec extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TemplateExpressionSpec_Sequence;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TemplateExpressionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that {@code ${sequence:N}} and {@code ${sequence}} generate sequential values.
		 */
		@DataSet(
		operation = Operation.CLEAN_INSERT,
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$SequenceExpressionSpec/shouldGenerateSequence/'))
		@ExpectedDataSet(
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$SequenceExpressionSpec/shouldGenerateSequence/expected/'))
		def 'shouldGenerateSequence'() {
			when: 'data is loaded with ${sequence} expressions'
			// preparation has executed by the time this block runs
			then: 'deterministic values are verified via STRICT comparison'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
		 */
		private void executeScript(String scriptPath) {
			def resource = getClass().classLoader.getResource(scriptPath)
			if (resource == null) {
				throw new IllegalStateException("Script not found: $scriptPath")
			}
			resource.text
					.split(';')
					.collect { it.trim() }
					.findAll { !it.empty }
					.each { sql.execute(it) }
		}
	}

	/**
	 * Tests {@code ${now}} template expressions.
	 *
	 * <p>The {@code ${now}} expression generates the current timestamp. The {@code ${now+Xd}}
	 * expression generates a relative timestamp.
	 */
	@DatabaseTest
	static class TimestampExpressionSpec extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TemplateExpressionSpec_Timestamp;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TemplateExpressionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that {@code ${now}} and {@code ${now+1d}} generate timestamp values.
		 */
		@DataSet(
		operation = Operation.CLEAN_INSERT,
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$TimestampExpressionSpec/shouldGenerateTimestamp/'))
		@ExpectedDataSet(
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$TimestampExpressionSpec/shouldGenerateTimestamp/expected/',
		columnStrategies = @ColumnStrategy(name = 'CREATED_AT', strategy = Strategy.NOT_NULL)))
		def 'shouldGenerateTimestamp'() {
			when: 'data is loaded with ${now} expressions'
			// preparation has executed by the time this block runs
			then: 'timestamps are validated via the NOT_NULL strategy'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
		 */
		private void executeScript(String scriptPath) {
			def resource = getClass().classLoader.getResource(scriptPath)
			if (resource == null) {
				throw new IllegalStateException("Script not found: $scriptPath")
			}
			resource.text
					.split(';')
					.collect { it.trim() }
					.findAll { !it.empty }
					.each { sql.execute(it) }
		}
	}

	/**
	 * Tests {@code ${faker.xxx.yyy}} template expressions.
	 *
	 * <p>The {@code ${faker.xxx.yyy}} expression uses the Datafaker library to generate realistic
	 * test data. The expected dataset uses {@link Strategy#NOT_NULL} to validate the generated value.
	 */
	@DatabaseTest
	static class FakerExpressionSpec extends Specification implements DatabaseTestSupport {

		/** Shared DataSource for all feature methods. */
		@Shared
		DataSource dataSource

		/** Groovy SQL helper for database operations. */
		@Shared
		Sql sql

		/** Static registry shared across all features. */
		static DataSourceRegistry sharedRegistry

		/** Static DataSource shared across all features. */
		static DataSource sharedDataSource

		/**
		 * Gets the DataSourceRegistry (Groovy property accessor).
		 *
		 * @return the registry
		 */
		DataSourceRegistry getDbTesterRegistry() {
			if (sharedRegistry == null) {
				initializeSharedResources()
			}
			return sharedRegistry
		}

		/**
		 * Initializes shared resources.
		 */
		private static void initializeSharedResources() {
			sharedDataSource = new JdbcDataSource().tap {
				setURL('jdbc:h2:mem:TemplateExpressionSpec_Faker;DB_CLOSE_DELAY=-1')
				setUser('sa')
				setPassword('')
			}
			sharedRegistry = new DataSourceRegistry()
			sharedRegistry.registerDefault(sharedDataSource)
		}

		/**
		 * Sets up H2 in-memory database connection and schema.
		 */
		def setupSpec() {
			if (sharedDataSource == null) {
				initializeSharedResources()
			}
			dataSource = sharedDataSource
			sql = new Sql(dataSource)
			executeScript('ddl/feature/TemplateExpressionSpec.sql')
		}

		/**
		 * Closes the SQL helper.
		 */
		def cleanupSpec() {
			sql?.close()
		}

		/**
		 * Verifies that {@code ${faker.name.fullName}} generates non-null name values.
		 */
		@DataSet(
		operation = Operation.CLEAN_INSERT,
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$FakerExpressionSpec/shouldGenerateFakerData/'))
		@ExpectedDataSet(
		sources = @DataSetSource(
		resourceLocation = 'classpath:example/feature/TemplateExpressionSpec$FakerExpressionSpec/shouldGenerateFakerData/expected/',
		columnStrategies = @ColumnStrategy(name = 'NAME', strategy = Strategy.NOT_NULL)))
		def 'shouldGenerateFakerData'() {
			when: 'data is loaded with ${faker} expressions'
			// preparation has executed by the time this block runs
			then: 'names are validated via the NOT_NULL strategy'
			noExceptionThrown()
		}

		/**
		 * Executes a SQL script from the classpath.
		 *
		 * @param scriptPath the classpath resource path
		 * @throws IllegalStateException if the script is not found
		 */
		private void executeScript(String scriptPath) {
			def resource = getClass().classLoader.getResource(scriptPath)
			if (resource == null) {
				throw new IllegalStateException("Script not found: $scriptPath")
			}
			resource.text
					.split(';')
					.collect { it.trim() }
					.findAll { !it.empty }
					.each { sql.execute(it) }
		}
	}
}
