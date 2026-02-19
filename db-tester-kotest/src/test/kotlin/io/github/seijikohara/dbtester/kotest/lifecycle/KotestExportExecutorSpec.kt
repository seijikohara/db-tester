package io.github.seijikohara.dbtester.kotest.lifecycle

import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.OperationDefaults
import io.github.seijikohara.dbtester.api.context.TestContext
import io.github.seijikohara.dbtester.api.exception.DatabaseTesterException
import io.github.seijikohara.dbtester.api.loader.DataSetLoader
import io.github.seijikohara.dbtester.api.spi.ExportSupport
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

/**
 * Unit tests for [KotestExportExecutor].
 *
 * This specification verifies the export phase executor that captures
 * database state after test execution.
 */
class KotestExportExecutorSpec : AnnotationSpec() {
    /** Mock export support. */
    private lateinit var mockSupport: ExportSupport

    /** The executor under test. */
    private lateinit var executor: KotestExportExecutor

    @BeforeEach
    fun setup(): Unit =
        run {
            mockSupport = mockk(relaxed = true)
            executor = KotestExportExecutor(mockSupport)
        }

    @Test
    fun `should create instance`(): Unit =
        KotestExportExecutor(mockSupport).let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should export with default settings`(): Unit =
        createTestContext().let { context ->
            createMockExportDataSet().let { exportDataSet ->
                shouldNotThrowAny {
                    executor.export(context, exportDataSet)
                }
            }
        }

    @Test
    fun `should create multiple independent executors`(): Unit =
        KotestExportExecutor(mockSupport).let { executor1 ->
            KotestExportExecutor(mockSupport).let { executor2 ->
                (executor1 === executor2) shouldBe false
            }
        }

    @Test
    fun `should delegate to export support`() {
        // Given: a context and export data set
        val context = createTestContext()
        val exportDataSet = createMockExportDataSet()

        // When: exporting
        executor.export(context, exportDataSet)

        // Then: support is called
        verify(exactly = 1) {
            mockSupport.export(context, exportDataSet)
        }
    }

    @Test
    fun `should propagate exception from support`() {
        // Given: a support that throws exception
        val context = createTestContext()
        val exportDataSet = createMockExportDataSet()

        every { mockSupport.export(any(), any()) } throws DatabaseTesterException("Export failed")

        // When/Then: exporting throws DatabaseTesterException
        shouldThrow<DatabaseTesterException> {
            executor.export(context, exportDataSet)
        }
    }

    @Test
    fun `should handle export with CSV format`() {
        testExportWithFormat(DataFormat.CSV)
    }

    @Test
    fun `should handle export with JSON format`() {
        testExportWithFormat(DataFormat.JSON)
    }

    @Test
    fun `should handle export with YAML format`() {
        testExportWithFormat(DataFormat.YAML)
    }

    @Test
    fun `should handle export with onFailureOnly flag`() {
        // Given: a context and export data set with onFailureOnly
        val context = createTestContext()
        val exportDataSet =
            mockk<ExportDataSet>().also { eds ->
                every { eds.format } returns DataFormat.CSV
                every { eds.outputDirectory } returns "build/db-tester-export"
                every { eds.tables } returns emptyArray()
                every { eds.dataSourceName } returns ""
                every { eds.onFailureOnly } returns true
            }

        // When: exporting
        executor.export(context, exportDataSet)

        // Then: support is called
        verify(exactly = 1) {
            mockSupport.export(context, exportDataSet)
        }
    }

    /**
     * Helper method to test export with different data formats.
     *
     * @param format the data format to test
     */
    private fun testExportWithFormat(format: DataFormat) {
        // Given: a context and export data set with specified format
        val context = createTestContext()
        val exportDataSet =
            mockk<ExportDataSet>().also { eds ->
                every { eds.format } returns format
                every { eds.outputDirectory } returns "build/db-tester-export"
                every { eds.tables } returns emptyArray()
                every { eds.dataSourceName } returns ""
                every { eds.onFailureOnly } returns false
            }

        // When: exporting
        executor.export(context, exportDataSet)

        // Then: support is called with correct export data set
        verify(exactly = 1) {
            mockSupport.export(context, exportDataSet)
        }
    }

    /**
     * Creates a TestContext for testing.
     *
     * @return the test context
     */
    private fun createTestContext(): TestContext =
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
     * Creates a mock ExportDataSet annotation.
     *
     * @return the mocked annotation
     */
    private fun createMockExportDataSet(): ExportDataSet =
        mockk<ExportDataSet>().also { exportDataSet ->
            every { exportDataSet.format } returns DataFormat.CSV
            every { exportDataSet.outputDirectory } returns "build/db-tester-export"
            every { exportDataSet.tables } returns emptyArray()
            every { exportDataSet.dataSourceName } returns ""
            every { exportDataSet.onFailureOnly } returns false
        }

    /**
     * Sample test class for reflection.
     */
    class SampleTestClass {
        /** Sample test method. */
        fun sampleMethod(): Unit = Unit
    }
}
