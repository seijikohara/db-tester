package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.internal.context.TestContext
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
 */
class DatabaseTestInterceptor implements IMethodInterceptor {

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
	DatabaseTestInterceptor(Preparation preparation, Expectation expectation) {
		this.preparation = preparation
		this.expectation = expectation
		this.preparationExecutor = new SpockPreparationExecutor()
		this.expectationVerifier = new SpockExpectationVerifier()
	}

	/**
	 * Intercepts a test method invocation to perform database setup and verification.
	 *
	 * <p>This method executes the preparation phase before the test and the expectation
	 * verification phase after the test completes.
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
	 * Creates a TestContext from the Spock invocation.
	 *
	 * <p>This method handles Spock's feature naming convention where test method names contain
	 * spaces (e.g., "should load data"). The scenario name resolution is handled by the
	 * {@link io.github.seijikohara.dbtester.spock.spi.SpockScenarioNameResolver} SPI implementation
	 * which extracts the human-readable feature name from Spock's {@code @FeatureMetadata} annotation.
	 *
	 * @param invocation the method invocation (must not be null)
	 * @return the test context, never null
	 */
	private TestContext createTestContext(IMethodInvocation invocation) {
		def specClass = invocation.spec.reflection
		def featureMethod = invocation.feature?.featureMethod?.reflection as Method

		// If feature method is not available (e.g., for spec-level interceptors),
		// try to get it from the iteration
		if (featureMethod == null) {
			featureMethod = invocation.method?.reflection as Method
		}

		// Get or create configuration and registry from the specification instance
		// Use Spock-aware configuration that resolves feature names from @FeatureMetadata
		def configuration = getOrCreateConfiguration(invocation)
		def registry = getOrCreateRegistry(invocation)

		new TestContext(specClass, featureMethod, configuration, registry)
	}

	/**
	 * Gets or creates the Configuration for the specification.
	 *
	 * <p>The configuration uses the default loader which delegates scenario name resolution
	 * to the {@link io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolverRegistry}.
	 * The {@link io.github.seijikohara.dbtester.spock.spi.SpockScenarioNameResolver} is automatically
	 * discovered via ServiceLoader and handles Spock feature name resolution.
	 *
	 * @param invocation the method invocation (must not be null)
	 * @return the configuration, never null
	 */
	private Configuration getOrCreateConfiguration(IMethodInvocation invocation) {
		def spec = invocation.instance

		// First, try Groovy property access (works for both properties and fields)
		// This supports getDbTesterConfiguration() accessor methods which can return
		// static or @Shared configuration instances that are initialized early in the lifecycle
		def metaProperty = spec.metaClass.hasProperty(spec, 'dbTesterConfiguration')
		if (metaProperty != null) {
			def existing = spec.dbTesterConfiguration
			if (existing instanceof Configuration) {
				return existing as Configuration
			}
		}

		// Fallback: Check if specification has a configuration field using reflection
		def configField = findField(spec.class, 'dbTesterConfiguration', Configuration)
		if (configField != null) {
			configField.accessible = true
			def existing = configField.get(spec) as Configuration
			if (existing != null) {
				return existing
			}
			// Create and cache default configuration
			def config = Configuration.defaults()
			configField.set(spec, config)
			return config
		}

		// Return default configuration (SPI handles Spock-specific resolution)
		Configuration.defaults()
	}

	/**
	 * Gets or creates the DataSourceRegistry for the specification.
	 *
	 * @param invocation the method invocation (must not be null)
	 * @return the data source registry, never null
	 */
	private DataSourceRegistry getOrCreateRegistry(IMethodInvocation invocation) {
		def spec = invocation.instance

		// First, try invoking getDbTesterRegistry() method if it exists
		// This supports accessor methods that return @Autowired or lazily-initialized registry instances
		def metaMethod = spec.metaClass.getMetaMethod('getDbTesterRegistry', new Class[0])
		if (metaMethod != null) {
			// Explicitly invoke the method rather than relying on property access
			def existing = metaMethod.invoke(spec, new Object[0])
			if (existing instanceof DataSourceRegistry) {
				return existing as DataSourceRegistry
			}
		}

		// Second, try property access for fields or properties
		def metaProperty = spec.metaClass.hasProperty(spec, 'dbTesterRegistry')
		if (metaProperty != null) {
			def existing = spec.dbTesterRegistry
			if (existing instanceof DataSourceRegistry) {
				return existing as DataSourceRegistry
			}
		}

		// Fallback: Check if specification has a registry field using reflection
		def registryField = findField(spec.class, 'dbTesterRegistry', DataSourceRegistry)
		if (registryField != null) {
			registryField.accessible = true
			def existing = registryField.get(spec) as DataSourceRegistry
			if (existing != null) {
				return existing
			}
			// Create and cache new registry
			def registry = new DataSourceRegistry()
			registryField.set(spec, registry)
			return registry
		}

		new DataSourceRegistry()
	}

	/**
	 * Finds a field by name and type in the class hierarchy.
	 *
	 * @param clazz the class to search (must not be null)
	 * @param fieldName the field name (must not be null)
	 * @param fieldType the expected field type (must not be null)
	 * @return the field, or {@code null} if not found
	 */
	private Field findField(Class<?> clazz, String fieldName, Class<?> fieldType) {
		def current = clazz
		while (current != null && current != Object) {
			try {
				def field = current.getDeclaredField(fieldName)
				if (fieldType.isAssignableFrom(field.type)) {
					return field
				}
			} catch (NoSuchFieldException ignored) {
				// Continue searching in parent class
			}
			current = current.superclass
		}
		null
	}
}
