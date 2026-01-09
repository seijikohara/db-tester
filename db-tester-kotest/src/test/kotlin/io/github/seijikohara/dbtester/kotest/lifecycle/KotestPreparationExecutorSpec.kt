package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

/**
 * Unit tests for [KotestPreparationExecutor].
 *
 * This specification verifies the preparation phase executor that loads
 * and applies datasets before test execution.
 */
class KotestPreparationExecutorSpec : AnnotationSpec() {
    /** The executor under test. */
    private lateinit var executor: KotestPreparationExecutor

    @BeforeEach
    fun setup(): Unit = run { executor = KotestPreparationExecutor() }

    @Test
    fun `should create instance`(): Unit =
        KotestPreparationExecutor().let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should handle empty datasets gracefully`(): Unit =
        createTestContextWithEmptyDatasets().let { context ->
            createMockDataSet(Operation.CLEAN_INSERT).let { dataSet ->
                shouldNotThrowAny {
                    executor.execute(context, dataSet)
                }
            }
        }

    @Test
    fun `should create multiple independent executors`(): Unit =
        KotestPreparationExecutor().let { executor1 ->
            KotestPreparationExecutor().let { executor2 ->
                (executor1 === executor2) shouldBe false
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
     * Creates a mock DataSet annotation with the specified operation.
     *
     * @param operation the operation to use
     * @return the mocked annotation
     */
    private fun createMockDataSet(operation: Operation): DataSet =
        mockk<DataSet>().also { dataSet ->
            every { dataSet.operation } returns operation
            every { dataSet.tableOrdering } returns TableOrderingStrategy.AUTO
            every { dataSet.sources } returns emptyArray()
        }

    /**
     * Sample test class for reflection.
     */
    class SampleTestClass {
        /** Sample test method. */
        fun sampleMethod(): Unit = Unit
    }
}
