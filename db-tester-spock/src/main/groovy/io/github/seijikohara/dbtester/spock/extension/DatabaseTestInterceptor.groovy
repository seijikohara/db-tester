package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExpectationVerifier
import io.github.seijikohara.dbtester.spock.lifecycle.SpockExportExecutor
import io.github.seijikohara.dbtester.spock.lifecycle.SpockPreparationExecutor
import java.lang.reflect.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

	/** Logger for tracking export execution and errors. */
	private static final Logger logger = LoggerFactory.getLogger(DatabaseTestInterceptor)

	/** The data set annotation for preparation phase (may be null). */
	protected final DataSet dataSet

	/** The expected data set annotation for verification phase (may be null). */
	protected final ExpectedDataSet expectedDataSet

	/** The export data set annotation for post-test export (may be null). */
	protected final ExportDataSet exportDataSet

	/** Executor for the preparation phase. */
	protected final SpockPreparationExecutor preparationExecutor

	/** Verifier for the expectation phase. */
	protected final SpockExpectationVerifier expectationVerifier

	/** Executor for the export phase. */
	protected final SpockExportExecutor exportExecutor

	/**
	 * Creates a new interceptor with the given annotations.
	 *
	 * @param dataSet the data set annotation (may be null)
	 * @param expectedDataSet the expected data set annotation (may be null)
	 * @param exportDataSet the export data set annotation (may be null)
	 */
	DatabaseTestInterceptor(DataSet dataSet, ExpectedDataSet expectedDataSet,
	ExportDataSet exportDataSet) {
		this(dataSet, expectedDataSet, exportDataSet,
		new SpockPreparationExecutor(), new SpockExpectationVerifier(), new SpockExportExecutor())
	}

	/**
	 * Creates a new interceptor with explicit lifecycle collaborators.
	 *
	 * <p>This constructor exists to inject test doubles for the preparation, verification, and
	 * export phases. Production code uses the three-argument constructor.
	 *
	 * @param dataSet the data set annotation (may be null)
	 * @param expectedDataSet the expected data set annotation (may be null)
	 * @param exportDataSet the export data set annotation (may be null)
	 * @param preparationExecutor the preparation executor
	 * @param expectationVerifier the expectation verifier
	 * @param exportExecutor the export executor
	 */
	DatabaseTestInterceptor(DataSet dataSet, ExpectedDataSet expectedDataSet,
	ExportDataSet exportDataSet, SpockPreparationExecutor preparationExecutor,
	SpockExpectationVerifier expectationVerifier, SpockExportExecutor exportExecutor) {
		this.dataSet = dataSet
		this.expectedDataSet = expectedDataSet
		this.exportDataSet = exportDataSet
		this.preparationExecutor = preparationExecutor
		this.expectationVerifier = expectationVerifier
		this.exportExecutor = exportExecutor
	}

	@Override
	void intercept(IMethodInvocation invocation) throws Throwable {
		def testContext = createTestContext(invocation)

		dataSet?.with { preparationExecutor.execute(testContext, it) }

		boolean testFailed = false
		try {
			invocation.proceed()
			expectedDataSet?.with { expectationVerifier.verify(testContext, it) }
		} catch (Throwable t) {
			testFailed = true
			throw t
		} finally {
			handleExportDataSet(testContext, testFailed)
		}
	}

	/**
	 * Handles ExportDataSet execution in a finally-equivalent block.
	 *
	 * <p>Export errors are caught and logged to prevent masking test or verification failures.
	 *
	 * @param testContext the test context
	 * @param testFailed whether the test execution or verification failed
	 */
	private void handleExportDataSet(TestContext testContext, boolean testFailed) {
		if (exportDataSet == null) {
			return
		}
		if (exportDataSet.onFailureOnly() && !testFailed) {
			logger.debug('Skipping @ExportDataSet for {}.{}() because the test passed and'
					+ ' onFailureOnly=true',
					testContext.testClass().simpleName,
					testContext.testMethod().name)
			return
		}
		try {
			exportExecutor.export(testContext, exportDataSet)
		} catch (Exception e) {
			logger.error('Failed to export dataset for {}.{}(): {}',
					testContext.testClass().simpleName,
					testContext.testMethod().name,
					e.message, e)
		}
	}

	/**
	 * Creates a TestContext from the Spock invocation.
	 *
	 * <p>This interceptor is registered via {@code feature.addInterceptor()}, so
	 * {@code invocation.feature} is always non-null and resolves to the executing
	 * feature method.
	 *
	 * @param invocation the method invocation
	 * @return the test context
	 * @throws IllegalStateException if the feature method cannot be resolved
	 */
	protected TestContext createTestContext(IMethodInvocation invocation) {
		def specClass = invocation.spec.reflection
		Method featureMethod = invocation.feature?.featureMethod?.reflection
		if (featureMethod == null) {
			throw new IllegalStateException(
			"Cannot resolve feature method from invocation for spec '${specClass.simpleName}'."
			)
		}

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
