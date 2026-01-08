package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExpectationVerifier
import io.github.seijikohara.dbtester.spock.lifecycle.SpockPreparationExecutor
import java.lang.reflect.Field
import java.lang.reflect.Method
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

/**
 * Spock method interceptor that handles database testing operations.
 *
 * <p>This interceptor executes the preparation phase before the test method and the expectation
 * verification phase after the test method completes.
 *
 * <p>Configuration and DataSourceRegistry are resolved from the specification instance using
 * Groovy property access or field reflection. Subclasses can override {@link #getConfiguration}
 * and {@link #getRegistry} to provide custom resolution strategies (e.g., Spring dependency injection).
 *
 * @see DatabaseTestExtension
 */
class DatabaseTestInterceptor implements IMethodInterceptor {

	/** The data set annotation for preparation phase (may be null). */
	protected final DataSet dataSet

	/** The expected data set annotation for verification phase (may be null). */
	protected final ExpectedDataSet expectedDataSet

	/** Executor for the preparation phase. */
	protected final SpockPreparationExecutor preparationExecutor = new SpockPreparationExecutor()

	/** Verifier for the expectation phase. */
	protected final SpockExpectationVerifier expectationVerifier = new SpockExpectationVerifier()

	/**
	 * Creates a new interceptor with the given annotations.
	 *
	 * @param dataSet the data set annotation (may be null)
	 * @param expectedDataSet the expected data set annotation (may be null)
	 */
	DatabaseTestInterceptor(DataSet dataSet, ExpectedDataSet expectedDataSet) {
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
	protected TestContext createTestContext(IMethodInvocation invocation) {
		def specClass = invocation.spec.reflection
		def featureMethod = (invocation.feature?.featureMethod?.reflection
				?: invocation.method?.reflection) as Method

		new TestContext(
				specClass,
				featureMethod,
				getConfiguration(invocation),
				getRegistry(invocation)
				)
	}

	/**
	 * Gets the Configuration for the specification.
	 *
	 * <p>Resolution order:
	 * <ol>
	 *   <li>Groovy property {@code dbTesterConfiguration}
	 *   <li>Field named {@code dbTesterConfiguration}
	 *   <li>Default configuration
	 * </ol>
	 *
	 * @param invocation the method invocation
	 * @return the configuration
	 */
	protected Configuration getConfiguration(IMethodInvocation invocation) {
		def spec = invocation.instance

		// Try Groovy property access first
		if (spec.metaClass.hasProperty(spec, 'dbTesterConfiguration')) {
			def value = spec.dbTesterConfiguration
			if (value instanceof Configuration) {
				return value
			}
		}

		// Try field reflection
		findField(spec.class, 'dbTesterConfiguration', Configuration)?.with { field ->
			field.accessible = true
			def value = field.get(spec)
			if (value != null) {
				return value as Configuration
			}
			// Cache default configuration in field
			def config = Configuration.defaults()
			field.set(spec, config)
			return config
		}

		Configuration.defaults()
	}

	/**
	 * Gets the DataSourceRegistry for the specification.
	 *
	 * <p>Resolution order:
	 * <ol>
	 *   <li>Method {@code getDbTesterRegistry()}
	 *   <li>Groovy property {@code dbTesterRegistry}
	 *   <li>Field named {@code dbTesterRegistry}
	 *   <li>New empty registry
	 * </ol>
	 *
	 * @param invocation the method invocation
	 * @return the data source registry
	 */
	protected DataSourceRegistry getRegistry(IMethodInvocation invocation) {
		def spec = invocation.instance

		// Try method invocation first (supports lazy initialization)
		spec.metaClass.getMetaMethod('getDbTesterRegistry', [] as Class[])?.with { method ->
			def value = method.invoke(spec, [] as Object[])
			if (value instanceof DataSourceRegistry) {
				return value
			}
		}

		// Try Groovy property access
		if (spec.metaClass.hasProperty(spec, 'dbTesterRegistry')) {
			def value = spec.dbTesterRegistry
			if (value instanceof DataSourceRegistry) {
				return value
			}
		}

		// Try field reflection
		findField(spec.class, 'dbTesterRegistry', DataSourceRegistry)?.with { field ->
			field.accessible = true
			def value = field.get(spec)
			if (value != null) {
				return value as DataSourceRegistry
			}
			// Cache new registry in field
			def registry = new DataSourceRegistry()
			field.set(spec, registry)
			return registry
		}

		new DataSourceRegistry()
	}

	/**
	 * Finds a field by name and type in the class hierarchy.
	 *
	 * @param clazz the class to search
	 * @param fieldName the name of the field to find
	 * @param fieldType the expected type of the field
	 * @return the field, or null if not found
	 */
	private static Field findField(Class<?> clazz, String fieldName, Class<?> fieldType) {
		generateSequence(clazz) { it.superclass }
		.takeWhile { it != Object }
		.collectMany { it.declaredFields.toList() }
		.find { it.name == fieldName && fieldType.isAssignableFrom(it.type) }
	}

	/**
	 * Generates a sequence starting from seed, applying generator until null.
	 *
	 * @param seed the initial value
	 * @param generator the function to generate the next value
	 * @return the list of generated values
	 */
	private static <T> List<T> generateSequence(T seed, Closure<T> generator) {
		def result = []
		def current = seed
		while (current != null) {
			result << current
			current = generator(current)
		}
		result
	}
}
