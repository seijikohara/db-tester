package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

/**
 * Unit tests for [KotestExpectationVerifier].
 *
 * This specification verifies the expectation phase verifier that validates
 * database state after test execution.
 */
class KotestExpectationVerifierSpec : AnnotationSpec() {
    /** The verifier under test. */
    private lateinit var verifier: KotestExpectationVerifier

    @BeforeEach
    fun setup(): Unit = run { verifier = KotestExpectationVerifier() }

    @Test
    fun `should create instance`(): Unit =
        KotestExpectationVerifier().let { instance ->
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
        KotestExpectationVerifier().let { verifier1 ->
            KotestExpectationVerifier().let { verifier2 ->
                (verifier1 === verifier2) shouldBe false
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
                        Configuration(
                            ConventionSettings.standard(),
                            OperationDefaults.standard(),
                            loader,
                        ).let { configuration ->
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
        }

    /**
     * Sample test class for reflection.
     */
    class SampleTestClass {
        /** Sample test method. */
        fun sampleMethod(): Unit = Unit
    }
}
