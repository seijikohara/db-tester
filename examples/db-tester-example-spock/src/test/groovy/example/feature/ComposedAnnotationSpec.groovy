package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.spock.extension.DatabaseTest
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestSupport
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates meta-annotation (composed annotation) support for {@link DataSet} and
 * {@link ExpectedDataSet} with Spock.
 *
 * <p>Meta-annotations allow encapsulating reusable {@code @DataSet} and {@code @ExpectedDataSet}
 * configurations into custom annotations. This reduces duplication when multiple test methods share
 * the same dataset locations or column exclusion settings.
 *
 * <p>This specification covers three composition patterns:
 * <ul>
 *   <li>Composed {@code @DataSet}: a custom annotation wrapping {@code @DataSet} with a fixed
 *       resource location
 *   <li>Composed {@code @ExpectedDataSet}: a custom annotation wrapping {@code @ExpectedDataSet}
 *       with column exclusions
 *   <li>Two-level composition: a custom annotation that combines both composed annotations,
 *       requiring the framework to traverse two levels of meta-annotation hierarchy
 * </ul>
 *
 * <p>The framework's {@code AnnotationUtils} discovers these annotations through recursive
 * meta-annotation traversal with cycle detection.
 *
 * <p>The composed annotations ({@link UserSeedData}, {@link VerifyIgnoringAuditColumns}, and
 * {@link UserDataTest}) are defined in separate files within the same package.
 *
 * @see DataSet
 * @see ExpectedDataSet
 * @see UserSeedData
 * @see VerifyIgnoringAuditColumns
 * @see UserDataTest
 */
@DatabaseTest
class ComposedAnnotationSpec extends Specification implements DatabaseTestSupport {

	/** Shared DataSource for all feature methods. */
	@Shared
	DataSource dataSource

	/** Groovy SQL helper for database operations. */
	@Shared
	Sql sql

	/** Static registry and DataSource shared across all tests. */
	static DataSourceRegistry sharedRegistry
	static DataSource sharedDataSource

	/**
	 * Gets the DataSourceRegistry (Groovy property accessor).
	 * @return the registry
	 */
	DataSourceRegistry getDbTesterRegistry() {
		if (sharedRegistry == null) {
			initializeSharedResources()
		}
		return sharedRegistry
	}

	/**
	 * Initializes shared resources (DataSource, Registry, SQL helper).
	 */
	private static void initializeSharedResources() {
		sharedDataSource = new JdbcDataSource().tap {
			setURL('jdbc:h2:mem:ComposedAnnotationSpec;DB_CLOSE_DELAY=-1')
			setUser('sa')
			setPassword('')
		}
		sharedRegistry = new DataSourceRegistry()
		sharedRegistry.registerDefault(sharedDataSource)
	}

	/**
	 * Sets up H2 in-memory database connection and schema.
	 * Uses Groovy compact syntax and extension methods.
	 */
	def setupSpec() {
		// Ensure resources are initialized
		if (sharedDataSource == null) {
			initializeSharedResources()
		}
		dataSource = sharedDataSource

		// Create Groovy SQL helper
		sql = new Sql(dataSource)

		// Execute DDL script using Groovy's resource handling
		executeScript('ddl/feature/ComposedAnnotationSpec.sql')
	}

	/**
	 * Cleans up database resources after all tests complete.
	 */
	def cleanupSpec() {
		sql?.close()
	}

	/**
	 * Verifies that the framework loads preparation data via composed {@code @DataSet} annotation.
	 *
	 * <p>The {@link UserSeedData} meta-annotation carries {@code @DataSet} with a fixed resource
	 * location. The framework traverses the annotation hierarchy to discover and apply it. A direct
	 * {@code @ExpectedDataSet} verifies that the data was loaded correctly.
	 */
	@UserSeedData
	@ExpectedDataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ComposedAnnotationSpec/composed-dataset/expected/'))
	def 'should load data via composed @DataSet annotation'() {
		expect: 'framework loads data via @UserSeedData and verifies via @ExpectedDataSet'
		true
	}

	/**
	 * Verifies that the framework applies column exclusions via composed {@code @ExpectedDataSet}
	 * annotation.
	 *
	 * <p>A direct {@code @DataSet} loads preparation data, and the {@link VerifyIgnoringAuditColumns}
	 * meta-annotation carries {@code @ExpectedDataSet} with {@code CREATED_AT} and
	 * {@code UPDATED_AT} excluded from comparison.
	 */
	@DataSet(sources = @DataSetSource(
	resourceLocation = 'classpath:example/feature/ComposedAnnotationSpec/user-seed/'))
	@VerifyIgnoringAuditColumns
	def 'should exclude audit columns via composed @ExpectedDataSet annotation'() {
		expect: 'framework loads data via @DataSet and verifies via @VerifyIgnoringAuditColumns'
		true
	}

	/**
	 * Verifies that the framework applies both preparation and expectation via a two-level composed
	 * annotation.
	 *
	 * <p>The {@link UserDataTest} meta-annotation carries {@link UserSeedData} and
	 * {@link VerifyIgnoringAuditColumns}, which in turn carry {@code @DataSet} and
	 * {@code @ExpectedDataSet}. The framework traverses two levels of meta-annotation hierarchy to
	 * discover and apply both annotations.
	 */
	@UserDataTest
	def 'should apply both annotations via deeply composed annotation'() {
		expect: 'framework loads and verifies via @UserDataTest two-level composition'
		true
	}

	/**
	 * Executes a SQL script from classpath using Groovy features.
	 *
	 * @param scriptPath the classpath resource path
	 */
	private void executeScript(String scriptPath) {
		def resource = getClass().classLoader.getResource(scriptPath)
		if (resource == null) {
			throw new IllegalStateException("Script not found: $scriptPath")
		}

		// Use Groovy's text property and split with filter
		resource.text
				.split(';')
				.collect { it.trim() }
				.findAll { !it.empty }
				.each { sql.execute(it) }
	}
}
