package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.dataset.Row
import io.github.seijikohara.dbtester.api.dataset.Table
import io.github.seijikohara.dbtester.api.dataset.TableSet
import io.github.seijikohara.dbtester.api.domain.CellValue
import io.github.seijikohara.dbtester.api.domain.ColumnName
import io.github.seijikohara.dbtester.api.domain.TableName
import io.github.seijikohara.dbtester.api.exception.ValidationException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.loader.ExpectedTableSet
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy
import io.github.seijikohara.dbtester.api.spi.ExpectationProvider
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import javax.sql.DataSource

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

    @Test
    fun `should verify expectation when datasets are found`() {
        // Given: a mock expectation provider
        val mockExpectationProvider = mockk<ExpectationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithExpectedDatasets(registry)

        // Given: a mock ExpectedDataSet annotation
        val expectedDataSet = createMockExpectedDataSet()

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification is executed
        verify(exactly = 1) {
            mockExpectationProvider.verifyExpectation(
                any(),
                mockDataSource,
                any(),
                any(),
                any(),
            )
        }
    }

    @Test
    fun `should use annotation retry count when specified`() {
        // Given: a mock expectation provider
        val mockExpectationProvider = mockk<ExpectationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithExpectedDatasets(registry)

        // Given: a mock ExpectedDataSet annotation with retry settings
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 2
                every { eds.retryDelayMillis } returns 10
                every { eds.rowOrdering } returns RowOrdering.ORDERED
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification is executed
        verify(exactly = 1) {
            mockExpectationProvider.verifyExpectation(
                any(),
                mockDataSource,
                any(),
                any(),
                RowOrdering.ORDERED,
            )
        }
    }

    @Test
    fun `should retry and succeed on validation exception`() {
        // Given: a mock expectation provider that fails first then succeeds
        val mockExpectationProvider = mockk<ExpectationProvider>()
        val mockDataSource = mockk<DataSource>()
        var callCount = 0

        every { mockExpectationProvider.verifyExpectation(any(), any(), any(), any(), any()) } answers {
            callCount++
            if (callCount == 1) {
                throw ValidationException("First attempt failed")
            }
        }

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithExpectedDatasets(registry)

        // Given: a mock ExpectedDataSet annotation with retry settings
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 2
                every { eds.retryDelayMillis } returns 10
                every { eds.rowOrdering } returns RowOrdering.ORDERED
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification succeeds after retry
        callCount shouldBe 2
    }

    @Test
    fun `should throw exception after all retries exhausted`() {
        // Given: a mock expectation provider that always fails
        val mockExpectationProvider = mockk<ExpectationProvider>()
        val mockDataSource = mockk<DataSource>()

        every { mockExpectationProvider.verifyExpectation(any(), any(), any(), any(), any()) } throws
            ValidationException("Always fails")

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithExpectedDatasets(registry)

        // Given: a mock ExpectedDataSet annotation with retry settings
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 1
                every { eds.retryDelayMillis } returns 10
                every { eds.rowOrdering } returns RowOrdering.ORDERED
            }

        // When/Then: verifying expectation throws ValidationException
        shouldThrow<ValidationException> {
            verifier.verify(context, expectedDataSet)
        }
    }

    @Test
    fun `should handle exclusions and column strategies`() {
        // Given: a mock expectation provider
        val mockExpectationProvider = mockk<ExpectationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets having exclusions
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithExclusions(registry)

        // Given: a mock ExpectedDataSet annotation
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 0
                every { eds.retryDelayMillis } returns -1
                every { eds.rowOrdering } returns RowOrdering.ORDERED
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification is executed with exclusions
        verify(exactly = 1) {
            mockExpectationProvider.verifyExpectation(
                any(),
                mockDataSource,
                match { it.contains("CREATED_AT") },
                any(),
                any(),
            )
        }
    }

    @Test
    fun `should handle column strategies in verification`() {
        // Given: a mock expectation provider
        val mockExpectationProvider = mockk<ExpectationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets having column strategies
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithColumnStrategies(registry)

        // Given: a mock ExpectedDataSet annotation
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns 0
                every { eds.retryDelayMillis } returns -1
                every { eds.rowOrdering } returns RowOrdering.ORDERED
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification is executed with column strategies
        verify(exactly = 1) {
            mockExpectationProvider.verifyExpectation(
                any(),
                mockDataSource,
                any(),
                match { it.containsKey("AMOUNT") },
                any(),
            )
        }
    }

    @Test
    fun `should use convention retry count when annotation has negative value`() {
        // Given: a mock expectation provider
        val mockExpectationProvider = mockk<ExpectationProvider>(relaxed = true)
        val mockDataSource = mockk<DataSource>()

        // Inject mock provider using reflection
        injectExpectationProvider(verifier, mockExpectationProvider)

        // Given: a context with expected datasets and custom conventions
        val registry = DataSourceRegistry()
        registry.registerDefault(mockDataSource)
        val context = createTestContextWithRetryConventions(registry)

        // Given: a mock ExpectedDataSet annotation with negative retry values (use convention)
        val expectedDataSet =
            mockk<ExpectedDataSet>().also { eds ->
                every { eds.retryCount } returns -1
                every { eds.retryDelayMillis } returns -1
                every { eds.rowOrdering } returns RowOrdering.UNORDERED
            }

        // When: verifying expectation
        verifier.verify(context, expectedDataSet)

        // Then: verification is executed
        verify(exactly = 1) {
            mockExpectationProvider.verifyExpectation(
                any(),
                mockDataSource,
                any(),
                any(),
                RowOrdering.UNORDERED,
            )
        }
    }

    /**
     * Injects a mock ExpectationProvider into the verifier using reflection.
     *
     * @param verifier the verifier to inject into
     * @param mockProvider the mock provider to inject
     */
    private fun injectExpectationProvider(
        verifier: KotestExpectationVerifier,
        mockProvider: ExpectationProvider,
    ) {
        val providerField = KotestExpectationVerifier::class.java.getDeclaredField("expectationProvider")
        providerField.isAccessible = true
        providerField.set(verifier, mockProvider)
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
     * Creates a TestContext with expected datasets.
     *
     * @param registry the registry to use
     * @return the test context
     */
    private fun createTestContextWithExpectedDatasets(registry: DataSourceRegistry): TestContext =
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
                val expectedTableSet = ExpectedTableSet(tableSet, emptySet(), emptyMap())

                val loader =
                    object : DataSetLoader {
                        override fun loadPreparationDataSets(context: TestContext): List<TableSet> = emptyList()

                        override fun loadExpectationDataSets(context: TestContext): List<TableSet> = listOf(tableSet)

                        override fun loadExpectationDataSetsWithExclusions(context: TestContext): List<ExpectedTableSet> =
                            listOf(expectedTableSet)
                    }

                val configuration =
                    Configuration
                        .builder()
                        .conventions(ConventionSettings.standard())
                        .operations(OperationDefaults.standard())
                        .loader(loader)
                        .build()

                TestContext(testClass, testMethod, configuration, registry)
            }
        }

    /**
     * Creates a TestContext with exclusions.
     *
     * @param registry the registry to use
     * @return the test context
     */
    private fun createTestContextWithExclusions(registry: DataSourceRegistry): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
                val table =
                    Table.of(
                        TableName("users"),
                        listOf(ColumnName("id"), ColumnName("name"), ColumnName("created_at")),
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
                val expectedTableSet = ExpectedTableSet(tableSet, setOf("CREATED_AT"), emptyMap())

                val loader =
                    object : DataSetLoader {
                        override fun loadPreparationDataSets(context: TestContext): List<TableSet> = emptyList()

                        override fun loadExpectationDataSets(context: TestContext): List<TableSet> = listOf(tableSet)

                        override fun loadExpectationDataSetsWithExclusions(context: TestContext): List<ExpectedTableSet> =
                            listOf(expectedTableSet)
                    }

                val configuration =
                    Configuration
                        .builder()
                        .conventions(ConventionSettings.standard())
                        .operations(OperationDefaults.standard())
                        .loader(loader)
                        .build()

                TestContext(testClass, testMethod, configuration, registry)
            }
        }

    /**
     * Creates a TestContext with column strategies.
     *
     * @param registry the registry to use
     * @return the test context
     */
    private fun createTestContextWithColumnStrategies(registry: DataSourceRegistry): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
                val table =
                    Table.of(
                        TableName("orders"),
                        listOf(ColumnName("id"), ColumnName("amount")),
                        listOf(
                            Row.of(
                                mapOf(
                                    ColumnName("id") to CellValue("1"),
                                    ColumnName("amount") to CellValue("100.50"),
                                ),
                            ),
                        ),
                    )
                val tableSet = TableSet.of(table)
                val columnStrategies: Map<String, ColumnStrategyMapping> =
                    mapOf("AMOUNT" to ColumnStrategyMapping.numeric("AMOUNT"))
                val expectedTableSet = ExpectedTableSet(tableSet, emptySet(), columnStrategies)

                val loader =
                    object : DataSetLoader {
                        override fun loadPreparationDataSets(context: TestContext): List<TableSet> = emptyList()

                        override fun loadExpectationDataSets(context: TestContext): List<TableSet> = listOf(tableSet)

                        override fun loadExpectationDataSetsWithExclusions(context: TestContext): List<ExpectedTableSet> =
                            listOf(expectedTableSet)
                    }

                val configuration =
                    Configuration
                        .builder()
                        .conventions(ConventionSettings.standard())
                        .operations(OperationDefaults.standard())
                        .loader(loader)
                        .build()

                TestContext(testClass, testMethod, configuration, registry)
            }
        }

    /**
     * Creates a TestContext with custom retry conventions.
     *
     * @param registry the registry to use
     * @return the test context
     */
    private fun createTestContextWithRetryConventions(registry: DataSourceRegistry): TestContext =
        SampleTestClass::class.java.let { testClass ->
            testClass.getMethod("sampleMethod").let { testMethod ->
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
                val expectedTableSet = ExpectedTableSet(tableSet, emptySet(), emptyMap())

                val loader =
                    object : DataSetLoader {
                        override fun loadPreparationDataSets(context: TestContext): List<TableSet> = emptyList()

                        override fun loadExpectationDataSets(context: TestContext): List<TableSet> = listOf(tableSet)

                        override fun loadExpectationDataSetsWithExclusions(context: TestContext): List<ExpectedTableSet> =
                            listOf(expectedTableSet)
                    }

                val conventions =
                    ConventionSettings
                        .builder()
                        .retryCount(3)
                        .retryDelay(java.time.Duration.ofMillis(50))
                        .build()

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
