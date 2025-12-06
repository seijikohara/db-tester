package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.internal.context.TestContext
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExpectationVerifier
import io.github.seijikohara.dbtester.spock.lifecycle.SpockPreparationExecutor
import java.lang.reflect.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestContextManager

/**
 * Spring Boot-aware Spock method interceptor for database testing.
 *
 * <p>This interceptor retrieves the {@link DataSourceRegistry} from the Spring
 * {@link ApplicationContext} using Spring's {@link TestContextManager}. This ensures
 * that Spring dependency injection has completed before accessing beans.
 *
 * <p>Unlike the standard {@code DatabaseTestInterceptor} from {@code db-tester-spock},
 * this interceptor does not rely on field injection or getter methods in the specification.
 * Instead, it directly retrieves the auto-configured beans from Spring.
 */
class SpringBootDatabaseTestInterceptor implements IMethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootDatabaseTestInterceptor)

	private final Preparation preparation
	private final Expectation expectation
	private final SpockPreparationExecutor preparationExecutor
	private final SpockExpectationVerifier expectationVerifier

	/**
	 * Creates a new interceptor with the given annotations.
	 *
	 * @param preparation the preparation annotation (may be null)
	 * @param expectation the expectation annotation (may be null)
	 */
	SpringBootDatabaseTestInterceptor(Preparation preparation, Expectation expectation) {
		this.preparation = preparation
		this.expectation = expectation
		this.preparationExecutor = new SpockPreparationExecutor()
		this.expectationVerifier = new SpockExpectationVerifier()
	}

	/**
	 * Intercepts a test method invocation to perform database setup and verification.
	 *
	 * <p>This method retrieves beans from the Spring ApplicationContext, executes the
	 * preparation phase before the test, and verifies expectations after the test completes.
	 *
	 * @param invocation the method invocation to intercept (must not be null)
	 * @throws Throwable if an error occurs during interception
	 */
	@Override
	void intercept(IMethodInvocation invocation) throws Throwable {
		def testContext = createTestContext(invocation)

		// Execute preparation phase
		if (preparation != null) {
			preparationExecutor.execute(testContext, preparation)
		}

		// Proceed with the actual test
		invocation.proceed()

		// Execute expectation verification phase
		if (expectation != null) {
			expectationVerifier.verify(testContext, expectation)
		}
	}

	/**
	 * Creates a TestContext from the Spock invocation using Spring's TestContextManager.
	 *
	 * @param invocation the method invocation (must not be null)
	 * @return the test context, never null
	 */
	private TestContext createTestContext(IMethodInvocation invocation) {
		def specClass = invocation.spec.reflection
		def featureMethod = invocation.feature?.featureMethod?.reflection as Method

		// If feature method is not available, try to get it from the iteration
		if (featureMethod == null) {
			featureMethod = invocation.method?.reflection as Method
		}

		// Get Spring ApplicationContext using TestContextManager
		def applicationContext = getApplicationContext(invocation)

		// Get Configuration and DataSourceRegistry from Spring context
		def configuration = getConfiguration(applicationContext)
		def registry = getDataSourceRegistry(applicationContext)

		new TestContext(specClass, featureMethod, configuration, registry)
	}

	/**
	 * Gets the Spring ApplicationContext using TestContextManager.
	 *
	 * <p>This method ensures that Spring's test context is properly initialized
	 * before attempting to access beans.
	 *
	 * @param invocation the method invocation (must not be null)
	 * @return the Spring ApplicationContext, never null
	 * @throws IllegalStateException if the ApplicationContext cannot be initialized
	 */
	private ApplicationContext getApplicationContext(IMethodInvocation invocation) {
		def spec = invocation.instance
		def specClass = spec.class

		try {
			// Create TestContextManager for the spec class
			def testContextManager = new TestContextManager(specClass)

			// Prepare test instance (this triggers Spring context initialization)
			testContextManager.prepareTestInstance(spec)

			// Get the ApplicationContext from the test context
			return testContextManager.testContext.applicationContext
		} catch (Exception e) {
			logger.error('Failed to get ApplicationContext for spec: {}', specClass.name, e)
			throw new IllegalStateException(
			"Failed to initialize Spring ApplicationContext for ${specClass.name}. " +
			'Ensure the spec is annotated with @SpringBootTest or similar.', e)
		}
	}

	/**
	 * Gets the Configuration bean from Spring context.
	 *
	 * @param applicationContext the Spring ApplicationContext (must not be null)
	 * @return the Configuration bean or defaults if not available, never null
	 */
	private Configuration getConfiguration(ApplicationContext applicationContext) {
		try {
			if (applicationContext.containsBean('dbTesterConfiguration')) {
				return applicationContext.getBean('dbTesterConfiguration', Configuration)
			}
		} catch (Exception e) {
			logger.debug('Configuration bean not available, using defaults: {}', e.message)
		}
		Configuration.defaults()
	}

	/**
	 * Gets the DataSourceRegistry bean from Spring context and ensures DataSources are registered.
	 *
	 * @param applicationContext the Spring ApplicationContext (must not be null)
	 * @return the DataSourceRegistry bean, never null
	 * @throws IllegalStateException if the DataSourceRegistry bean is not found
	 */
	private DataSourceRegistry getDataSourceRegistry(ApplicationContext applicationContext) {
		try {
			// Get the registry
			def registry = applicationContext.getBean('dbTesterDataSourceRegistry', DataSourceRegistry)

			// If registry doesn't have a default, use the registrar to populate it
			if (!registry.hasDefault() && applicationContext.containsBean('dataSourceRegistrar')) {
				def registrar = applicationContext.getBean('dataSourceRegistrar', DataSourceRegistrar)
				registrar.registerAll(registry)
			}

			return registry
		} catch (Exception e) {
			logger.error('Failed to get DataSourceRegistry from Spring context', e)
			throw new IllegalStateException(
			'DataSourceRegistry bean not found in Spring context. ' +
			'Ensure db-tester-spock-spring-boot-starter is properly configured.', e)
		}
	}
}
