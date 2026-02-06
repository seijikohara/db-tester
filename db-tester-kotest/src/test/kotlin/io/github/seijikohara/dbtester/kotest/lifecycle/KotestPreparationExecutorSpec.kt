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
import io.github.seijikohara.dbtester.api.spi.PreparationSupport
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

/**
 * Unit tests for [KotestPreparationExecutor].
 *
 * This specification verifies the preparation phase executor that loads
 * and applies datasets before test execution.
 */
class KotestPreparationExecutorSpec : AnnotationSpec() {
    /** Mock preparation support. */
    private lateinit var mockSupport: PreparationSupport

    /** The executor under test. */
    private lateinit var executor: KotestPreparationExecutor

    @BeforeEach
    fun setup(): Unit =
        run {
            mockSupport = mockk(relaxed = true)
            executor = KotestPreparationExecutor(mockSupport)
        }

    @Test
    fun `should create instance`(): Unit =
        KotestPreparationExecutor(mockSupport).let { instance ->
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
        KotestPreparationExecutor(mockSupport).let { executor1 ->
            KotestPreparationExecutor(mockSupport).let { executor2 ->
                (executor1 === executor2) shouldBe false
            }
        }

    @Test
    fun `should delegate to preparation support`() {
        // Given: a context and data set
        val context = createTestContextWithEmptyDatasets()
        val dataSet = createMockDataSet(Operation.CLEAN_INSERT)

        // When: executing preparation
        executor.execute(context, dataSet)

        // Then: support is called
        verify(exactly = 1) {
            mockSupport.execute(context, dataSet)
        }
    }

    @Test
    fun `should handle datasets with INSERT operation`() {
        testOperationType(Operation.INSERT)
    }

    @Test
    fun `should handle datasets with DELETE_ALL operation`() {
        testOperationType(Operation.DELETE_ALL)
    }

    @Test
    fun `should handle datasets with TRUNCATE_INSERT operation`() {
        testOperationType(Operation.TRUNCATE_INSERT)
    }

    @Test
    fun `should handle null query timeout from conventions`() {
        // Given: a context with null query timeout
        val context = createTestContextWithDatasets(queryTimeout = null)
        val dataSet = createMockDataSet(Operation.CLEAN_INSERT)

        // When: executing preparation
        executor.execute(context, dataSet)

        // Then: support is called
        verify(exactly = 1) {
            mockSupport.execute(context, dataSet)
        }
    }

    /**
     * Helper method to test different operation types.
     *
     * @param operation the operation to test
     */
    private fun testOperationType(operation: Operation) {
        // Given: a context and data set with specified operation
        val context = createTestContextWithEmptyDatasets()
        val dataSet = createMockDataSet(operation)

        // When: executing preparation
        executor.execute(context, dataSet)

        // Then: support is called with correct data set
        verify(exactly = 1) {
            mockSupport.execute(context, dataSet)
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
     * Creates a TestContext with configuration.
     *
     * @param queryTimeout whether to include query timeout (null for no timeout)
     * @return the test context
     */
    private fun createTestContextWithDatasets(queryTimeout: java.time.Duration? = java.time.Duration.ofSeconds(30)): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
                val loader =
                    mockk<DataSetLoader>().also { loader ->
                        every { loader.loadPreparationDataSets(any()) } returns emptyList()
                        every { loader.loadExpectationDataSets(any()) } returns emptyList()
                        every { loader.loadExpectationDataSetsWithExclusions(any()) } returns emptyList()
                    }

                val conventionsBuilder = ConventionSettings.builder()
                if (queryTimeout != null) {
                    conventionsBuilder.queryTimeout(queryTimeout)
                }
                val conventions = conventionsBuilder.build()

                val configuration =
                    Configuration
                        .builder()
                        .conventions(conventions)
                        .operations(OperationDefaults.standard())
                        .loader(loader)
                        .build()

                TestContext(testClass, testMethod, configuration, DataSourceRegistry())
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
