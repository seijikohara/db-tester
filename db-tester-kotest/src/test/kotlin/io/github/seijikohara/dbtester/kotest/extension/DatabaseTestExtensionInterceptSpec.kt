package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport
import io.github.seijikohara.dbtester.api.spi.ExportSupport
import io.github.seijikohara.dbtester.api.spi.PreparationSupport
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExpectationVerifier
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestExportExecutor
import io.github.seijikohara.dbtester.kotest.lifecycle.KotestPreparationExecutor
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.names.TestName
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

/**
 * Verifies the orchestration performed by [DatabaseTestExtension.intercept].
 *
 * Each test drives the real interception path with mock SPI support wrapped in real
 * executors, so the executor facades and the extension orchestration are both covered.
 */
class DatabaseTestExtensionInterceptSpec : AnnotationSpec() {
    /** Mock preparation SPI support. */
    private lateinit var prepSupport: PreparationSupport

    /** Mock expectation SPI support. */
    private lateinit var expectSupport: ExpectationSupport

    /** Mock export SPI support. */
    private lateinit var exportSupport: ExportSupport

    @BeforeEach
    fun setup() {
        prepSupport = mockk(relaxed = true)
        expectSupport = mockk(relaxed = true)
        exportSupport = mockk(relaxed = true)
    }

    @Test
    suspend fun `runs preparation then verifies on success`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "prepareAndVerify")

        extension.intercept(testCase) { successResult() }

        verify(exactly = 1) { prepSupport.execute(any(), any()) }
        verify(exactly = 1) { expectSupport.verify(any(), any()) }
        verify(exactly = 0) { exportSupport.export(any(), any()) }
    }

    @Test
    suspend fun `skips verification when the test result is not success`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "verifyOnly")

        extension.intercept(testCase) { errorResult() }

        verify(exactly = 0) { expectSupport.verify(any(), any()) }
    }

    @Test
    suspend fun `rethrows when verification fails`() {
        every { expectSupport.verify(any(), any()) } throws AssertionError("mismatch")
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "verifyOnly")

        shouldThrow<AssertionError> {
            extension.intercept(testCase) { successResult() }
        }
    }

    @Test
    suspend fun `exports on success when onFailureOnly is false`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "exportAlways")

        extension.intercept(testCase) { successResult() }

        verify(exactly = 1) { exportSupport.export(any(), any()) }
    }

    @Test
    suspend fun `skips export on success when onFailureOnly is true`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "exportOnFailureOnly")

        extension.intercept(testCase) { successResult() }

        verify(exactly = 0) { exportSupport.export(any(), any()) }
    }

    @Test
    suspend fun `exports on failure when onFailureOnly is true`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "exportOnFailureOnly")

        extension.intercept(testCase) { errorResult() }

        verify(exactly = 1) { exportSupport.export(any(), any()) }
    }

    @Test
    suspend fun `swallows export errors so they do not mask the result`() {
        every { exportSupport.export(any(), any()) } throws RuntimeException("export failed")
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "exportAlways")

        shouldNotThrowAny {
            extension.intercept(testCase) { successResult() }
        }
    }

    @Test
    suspend fun `executes the test directly when no annotations are present`() {
        val extension = buildExtension()
        val testCase = mockTestCase(InterceptSampleSpec(), "noAnnotations")
        val expected = successResult()

        val result = extension.intercept(testCase) { expected }

        result shouldBe expected
        verify(exactly = 0) { prepSupport.execute(any(), any()) }
    }

    @Test
    suspend fun `resolves registry and configuration from DatabaseTestSupport when providers are null`() {
        val extension = buildExtension(registryProvider = null, configurationProvider = null)
        val testCase = mockTestCase(InterceptSampleSpec(), "prepareAndVerify")

        extension.intercept(testCase) { successResult() }

        verify(exactly = 1) { prepSupport.execute(any(), any()) }
    }

    @Test
    suspend fun `throws when no registry is available`() {
        val extension = buildExtension(registryProvider = null, configurationProvider = null)
        val testCase = mockTestCase(PlainSampleSpec(), "prepareOnly")

        shouldThrow<IllegalStateException> {
            extension.intercept(testCase) { successResult() }
        }
    }

    /**
     * Builds an extension with real executors backed by the mock SPI support.
     *
     * @param registryProvider the registry provider, defaulting to an empty registry
     * @param configurationProvider the configuration provider, defaulting to [Configuration.defaults]
     * @return the extension under test
     */
    private fun buildExtension(
        registryProvider: (() -> DataSourceRegistry)? = { DataSourceRegistry() },
        configurationProvider: (() -> Configuration)? = { Configuration.defaults() },
    ): DatabaseTestExtension =
        DatabaseTestExtension(
            registryProvider,
            configurationProvider,
            KotestPreparationExecutor(prepSupport),
            KotestExpectationVerifier(expectSupport),
            KotestExportExecutor(exportSupport),
        )

    /**
     * Creates a mock [TestCase] that resolves to the given spec and method name.
     *
     * @param spec the spec instance whose class carries the test methods
     * @param methodName the reflective method name to resolve
     * @return the mock test case
     */
    private fun mockTestCase(
        spec: Spec,
        methodName: String,
    ): TestCase =
        mockk<TestCase>().also { testCase ->
            every { testCase.spec } returns spec
            every { testCase.name } returns mockk<TestName>().also { testName -> every { testName.name } returns methodName }
        }

    /**
     * Creates a mock successful test result.
     *
     * @return a [TestResult.Success] mock
     */
    private fun successResult(): TestResult.Success = mockk()

    /**
     * Creates a mock failing test result.
     *
     * @return a [TestResult.Error] mock
     */
    private fun errorResult(): TestResult.Error = mockk()
}

/**
 * Test fixture spec whose methods carry database testing annotations.
 *
 * The class implements [DatabaseTestSupport] to exercise the provider fallback path. It
 * declares no Kotest `@Test` methods, so Kotest discovers it without running any test.
 */
class InterceptSampleSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    override val dbTesterRegistry: DataSourceRegistry = DataSourceRegistry()

    /** Method annotated for both preparation and verification. */
    @DataSet
    @ExpectedDataSet
    fun prepareAndVerify() = Unit

    /** Method annotated for verification only. */
    @ExpectedDataSet
    fun verifyOnly() = Unit

    /** Method annotated to export unconditionally. */
    @ExportDataSet
    fun exportAlways() = Unit

    /** Method annotated to export only when the test fails. */
    @ExportDataSet(onFailureOnly = true)
    fun exportOnFailureOnly() = Unit

    /** Method without database testing annotations. */
    fun noAnnotations() = Unit
}

/**
 * Test fixture spec that does not implement [DatabaseTestSupport].
 *
 * Used to verify the registry resolution failure path.
 */
class PlainSampleSpec : AnnotationSpec() {
    /** Method annotated for preparation only. */
    @DataSet
    fun prepareOnly() = Unit
}
