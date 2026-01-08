package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
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
 * {@link ApplicationContext} using Spring's {@link TestContextManager}.
 * This ensures that Spring dependency injection has completed before accessing beans.
 *
 * <p>Unlike the standard {@code DatabaseTestInterceptor} from {@code db-tester-spock},
 * this interceptor does not rely on field injection or getter methods in the specification.
 * Instead, it directly retrieves the auto-configured beans from Spring.
 *
 * @see SpringBootDatabaseTestExtension
 * @see TestContextManager
 */
class SpringBootDatabaseTestInterceptor implements IMethodInterceptor {

	/** Logger for this class. */
	private static final Logger logger = LoggerFactory.getLogger(SpringBootDatabaseTestInterceptor)

	/** The data set annotation for preparation phase (may be null). */
	private final DataSet dataSet

	/** The expected data set annotation for verification phase (may be null). */
	private final ExpectedDataSet expectedDataSet

	/** Executor for the preparation phase. */
	private final SpockPreparationExecutor preparationExecutor = new SpockPreparationExecutor()

	/** Verifier for the expectation phase. */
	private final SpockExpectationVerifier expectationVerifier = new SpockExpectationVerifier()

	/**
	 * Creates a new interceptor with the given annotations.
	 *
	 * @param dataSet the data set annotation (may be null)
	 * @param expectedDataSet the expected data set annotation (may be null)
	 */
	SpringBootDatabaseTestInterceptor(DataSet dataSet, ExpectedDataSet expectedDataSet) {
		this.dataSet = dataSet
		this.expectedDataSet = expectedDataSet
	}

	@Override
	void intercept(IMethodInvocation invocation) throws Throwable {
		def testContext = createTestContext(invocation)

		dataSet?.with { preparationExecutor.execute(testContext, it) }
		invocation.proceed()
		expectedDataSet?.with { expectationVerifier.verify(testContext, it) }
	}

	/**
	 * Creates a TestContext from the Spock invocation.
	 *
	 * @param invocation the method invocation
	 * @return the test context
	 */
	private TestContext createTestContext(IMethodInvocation invocation) {
		def specClass = invocation.spec.reflection
		def featureMethod = (invocation.feature?.featureMethod?.reflection
				?: invocation.method?.reflection) as Method

		def applicationContext = getApplicationContext(invocation)

		new TestContext(
				specClass,
				featureMethod,
				getConfiguration(applicationContext),
				getDataSourceRegistry(applicationContext)
				)
	}

	/**
	 * Gets the ApplicationContext using Spring's TestContextManager.
	 *
	 * <p>The TestContextManager handles the Spring TestContext Framework lifecycle.
	 * This method prepares the test instance and retrieves the ApplicationContext.
	 */
	private ApplicationContext getApplicationContext(IMethodInvocation invocation) {
		def spec = invocation.instance
		def specClass = spec.class

		try {
			def manager = new TestContextManager(specClass)
			manager.prepareTestInstance(spec)
			manager.testContext.applicationContext
		} catch (Exception e) {
			logger.error('Failed to get ApplicationContext for spec: {}', specClass.name, e)
			throw new IllegalStateException(
			"Failed to initialize Spring ApplicationContext for ${specClass.name}. " +
			'Ensure the spec is annotated with @SpringBootTest or similar.', e)
		}
	}

	/**
	 * Gets the Configuration from the ApplicationContext.
	 *
	 * <p>Falls back to defaults if the bean is not available.
	 *
	 * @param applicationContext the Spring ApplicationContext
	 * @return the configuration
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
	 * Gets the DataSourceRegistry from the ApplicationContext.
	 *
	 * <p>Populates the registry with DataSources if empty.
	 *
	 * @param applicationContext the Spring ApplicationContext
	 * @return the data source registry
	 * @throws IllegalStateException if the registry bean is not found
	 */
	private DataSourceRegistry getDataSourceRegistry(ApplicationContext applicationContext) {
		try {
			def registry = applicationContext.getBean('dbTesterDataSourceRegistry', DataSourceRegistry)

			// Populate registry if empty
			if (!registry.hasDefault() && applicationContext.containsBean('dataSourceRegistrar')) {
				def registrar = applicationContext.getBean('dataSourceRegistrar', DataSourceRegistrar)
				registrar.registerAll(registry)
			}

			registry
		} catch (Exception e) {
			logger.error('Failed to get DataSourceRegistry from Spring context', e)
			throw new IllegalStateException(
			'DataSourceRegistry bean not found in Spring context. ' +
			'Ensure db-tester-spock-spring-boot-starter is properly configured.', e)
		}
	}
}
