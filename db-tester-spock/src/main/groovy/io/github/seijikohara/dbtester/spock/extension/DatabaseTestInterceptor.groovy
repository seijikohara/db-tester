package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExpectationVerifier
import io.github.seijikohara.dbtester.spock.lifecycle.SpockPreparationExecutor
import java.lang.reflect.Method
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation

/**
 * Spock method interceptor that handles database testing operations.
 *
 * <p>This interceptor executes the preparation phase before the test method and the expectation
 * verification phase after the test method completes.
 *
 * <p>The specification class should implement {@link DatabaseTestSupport} trait to provide
 * the {@link DataSourceRegistry} and optionally a custom {@link Configuration}.
 * Subclasses can override {@link #getConfiguration} and {@link #getRegistry} to provide
 * custom resolution strategies (e.g., Spring dependency injection).
 *
 * @see DatabaseTestExtension
 * @see DatabaseTestSupport
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
	 *   <li>{@link DatabaseTestSupport} trait implementation
	 *   <li>Default configuration
	 * </ol>
	 *
	 * @param invocation the method invocation
	 * @return the configuration
	 */
	protected Configuration getConfiguration(IMethodInvocation invocation) {
		def spec = invocation.instance

		// Try DatabaseTestSupport trait first
		if (spec instanceof DatabaseTestSupport) {
			return spec.dbTesterConfiguration
		}

		Configuration.defaults()
	}

	/**
	 * Gets the DataSourceRegistry for the specification.
	 *
	 * <p>Resolution order:
	 * <ol>
	 *   <li>{@link DatabaseTestSupport} trait implementation
	 *   <li>Throws {@link IllegalStateException} if not found
	 * </ol>
	 *
	 * @param invocation the method invocation
	 * @return the data source registry
	 * @throws IllegalStateException if the specification does not implement DatabaseTestSupport
	 */
	protected DataSourceRegistry getRegistry(IMethodInvocation invocation) {
		def spec = invocation.instance

		// Try DatabaseTestSupport trait first
		if (spec instanceof DatabaseTestSupport) {
			return spec.dbTesterRegistry
		}

		throw new IllegalStateException(
		"Specification class '${spec.class.simpleName}' must implement DatabaseTestSupport " +
		"trait to provide dbTesterRegistry."
		)
	}
}
