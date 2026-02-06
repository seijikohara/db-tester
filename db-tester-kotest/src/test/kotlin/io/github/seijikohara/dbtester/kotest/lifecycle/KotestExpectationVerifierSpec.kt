package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.ExpectationSupport
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

/**
 * Unit tests for [KotestExpectationVerifier].
 *
 * This specification verifies the expectation phase verifier that validates
 * database state after test execution.
 */
class KotestExpectationVerifierSpec : AnnotationSpec() {
    /** Mock expectation support. */
    private lateinit var mockSupport: ExpectationSupport

    /** The verifier under test. */
    private lateinit var verifier: KotestExpectationVerifier

    @BeforeEach
    fun setup(): Unit =
        run {
            mockSupport = mockk(relaxed = true)
            verifier = KotestExpectationVerifier(mockSupport)
        }

    @Test
    fun `should create instance`(): Unit =
        KotestExpectationVerifier(mockSupport).let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should handle empty datasets gracefully`(): Unit =
        createTestContextWithEmptyDatasets().let { context ->
            createMockExpectedDataSet().let { expectedDataSet ->
                shouldNotThrowAny {
                    verifier.verify(context, expectedDataSet)
                }
            }
        }

    @Test
    fun `should create multiple independent verifiers`(): Unit =
        KotestExpectationVerifier(mockSupport).let { verifier1 ->
            KotestExpectationVerifier(mockSupport).let { verifier2 ->
                (verifier1 === verifier2) shouldBe false
            }
        }

    @Test
    fun `should delegate to expectation support`() {
        // Given: a context and expected data set
        val context = createTestContextWithEmptyDatasets()
        val expectedDataSet = createMockExpectedDataSet()

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: support is called
        verify(exactly = 1) {
            mockSupport.verify(context, expectedDataSet)
        }
    }

    @Test
    fun `should propagate validation exception from support`() {
        // Given: a support that throws validation exception
        val context = createTestContextWithEmptyDatasets()
        val expectedDataSet = createMockExpectedDataSet()

        every { mockSupport.verify(any(), any()) } throws ValidationException("Verification failed")

        // When/Then: verifying expectation throws ValidationException
        shouldThrow<ValidationException> {
            verifier.verify(context, expectedDataSet)
        }
    }

    @Test
    fun `should handle ordered row verification`() {
        // Given: a context and expected data set with ordered rows
        val context = createTestContextWithEmptyDatasets()
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 0
                every { eds.retryDelayMillis } returns -1
                every { eds.rowOrdering } returns RowOrdering.ORDERED
                every { eds.sources } returns emptyArray()
                every { eds.tableOrdering } returns TableOrderingStrategy.AUTO
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: support is called with the expected data set
        verify(exactly = 1) {
            mockSupport.verify(context, expectedDataSet)
        }
    }

    @Test
    fun `should handle unordered row verification`() {
        // Given: a context and expected data set with unordered rows
        val context = createTestContextWithEmptyDatasets()
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 0
                every { eds.retryDelayMillis } returns -1
                every { eds.rowOrdering } returns RowOrdering.UNORDERED
                every { eds.sources } returns emptyArray()
                every { eds.tableOrdering } returns TableOrderingStrategy.AUTO
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: support is called with the expected data set
        verify(exactly = 1) {
            mockSupport.verify(context, expectedDataSet)
        }
    }

    @Test
    fun `should handle retry count settings`() {
        // Given: a context and expected data set with retry settings
        val context = createTestContextWithEmptyDatasets()
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 3
                every { eds.retryDelayMillis } returns 100
                every { eds.rowOrdering } returns RowOrdering.ORDERED
                every { eds.sources } returns emptyArray()
                every { eds.tableOrdering } returns TableOrderingStrategy.AUTO
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: support is called with the expected data set (retry logic is handled by support)
        verify(exactly = 1) {
            mockSupport.verify(context, expectedDataSet)
        }
    }

    /**
     * Creates a TestContext with empty datasets.
     *
     * @return the test context
     */
    private fun createTestContextWithEmptyDatasets(): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
                mockk<DataSetLoader>()
                    .also { loader ->
                        every { loader.loadPreparationDataSets(any()) } returns emptyList()
                        every { loader.loadExpectationDataSets(any()) } returns emptyList()
                        every { loader.loadExpectationDataSetsWithExclusions(any()) } returns emptyList()
                    }.let { loader ->
                        Configuration
                            .builder()
                            .conventions(ConventionSettings.standard())
                            .operations(OperationDefaults.standard())
                            .loader(loader)
                            .build()
                            .let { configuration ->
                                DataSourceRegistry().let { registry ->
                                    TestContext(testClass, testMethod, configuration, registry)
                                }
                            }
                    }
            }
        }

    /**
     * Creates a mock ExpectedDataSet annotation.
     *
     * @return the mocked annotation
     */
    private fun createMockExpectedDataSet(): ExpectedDataSet =
        mockk<ExpectedDataSet>().also { expectedDataSet ->
            every { expectedDataSet.sources } returns emptyArray()
            every { expectedDataSet.tableOrdering } returns TableOrderingStrategy.AUTO
            every { expectedDataSet.retryCount } returns 0
            every { expectedDataSet.retryDelayMillis } returns -1
            every { expectedDataSet.rowOrdering } returns RowOrdering.ORDERED
        }

    /**
     * Sample test class for reflection.
     */
    class SampleTestClass {
        /** Sample test method. */
        fun sampleMethod(): Unit = Unit
    }
}
