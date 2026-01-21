package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.OperationProvider
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.sql.DataSource

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

    @Test
    fun `should execute operation when datasets are found`() {
        // Given: a mock operation provider
        val mockOperationProvider = mockk<OperationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectOperationProvider(executor, mockOperationProvider)

        // Given: a context with datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithDatasets(registry)

        // Given: a mock DataSet annotation
        val dataSet = createMockDataSet(Operation.CLEAN_INSERT)

        // When: executing preparation
        executor.execute(context, dataSet)

        // Then: operation is executed
        verify(exactly = 1) {
            mockOperationProvider.execute(
                Operation.CLEAN_INSERT,
                any(),
                mockDataSource,
                TableOrderingStrategy.AUTO,
                any(),
                any(),
            )
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
        // Given: a mock operation provider
        val mockOperationProvider = mockk<OperationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectOperationProvider(executor, mockOperationProvider)

        // Given: a context with datasets and null query timeout
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithDatasets(registry, queryTimeout = null)

        // Given: a mock DataSet annotation
        val dataSet = createMockDataSet(Operation.CLEAN_INSERT)

        // When: executing preparation
        executor.execute(context, dataSet)

        // Then: operation is executed with null timeout
        verify(exactly = 1) {
            mockOperationProvider.execute(
                Operation.CLEAN_INSERT,
                any(),
                mockDataSource,
                TableOrderingStrategy.AUTO,
                any(),
                null,
            )
        }
    }

    /**
     * Helper method to test different operation types.
     *
     * @param operation the operation to test
     */
    private fun testOperationType(operation: Operation) {
        // Given: a mock operation provider
        val mockOperationProvider = mockk<OperationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Create a fresh executor and inject mock provider
        val testExecutor = KotestPreparationExecutor()
        injectOperationProvider(testExecutor, mockOperationProvider)

        // Given: a context with datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithDatasets(registry)

        // Given: a mock DataSet annotation with specified operation
        val dataSet = createMockDataSet(operation)

        // When: executing preparation
        testExecutor.execute(context, dataSet)

        // Then: operation is executed with correct operation type
        verify(exactly = 1) {
            mockOperationProvider.execute(
                operation,
                any(),
                mockDataSource,
                any(),
                any(),
                any(),
            )
        }
    }

    /**
     * Injects a mock OperationProvider into the executor using reflection.
     *
     * @param executor the executor to inject into
     * @param mockProvider the mock provider to inject
     */
    private fun injectOperationProvider(
        executor: KotestPreparationExecutor,
        mockProvider: OperationProvider,
    ) {
        val providerField = KotestPreparationExecutor::class.java.getDeclaredField("operationProvider")
        providerField.isAccessible = true
        providerField.set(executor, mockProvider)
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
     * Creates a TestContext with actual datasets.
     *
     * @param registry the registry to use
     * @param queryTimeout whether to include query timeout (null for no timeout)
     * @return the test context
     */
    private fun createTestContextWithDatasets(
        registry: DataSourceRegistry,
        queryTimeout: java.time.Duration? = java.time.Duration.ofSeconds(30),
    ): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
                // Create a simple table set
                val table =
                    Table.of(
                        TableName("users"),
                        listOf(ColumnName("id"), ColumnName("name")),
                        listOf(
                            Row.of(
                                mapOf(
                                    ColumnName("id") to CellValue("1"),
                                    ColumnName("name") to CellValue("John"),
                                ),
                            ),
                        ),
                    )
                val tableSet = TableSet.of(table)

                val loader =
                    object : DataSetLoader {
                        override fun loadPreparationDataSets(context: TestContext): List<TableSet> = listOf(tableSet)

                        override fun loadExpectationDataSets(context: TestContext): List<TableSet> = emptyList()

                        override fun loadExpectationDataSetsWithExclusions(
                            context: TestContext,
                        ): List<io.github.seijikohara.dbtester.api.loader.ExpectedTableSet> = emptyList()
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

                TestContext(testClass, testMethod, configuration, registry)
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
