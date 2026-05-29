package example.feature

import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.export.DataSetExporter
import io.github.seijikohara.dbtester.api.export.ExportConfiguration
import io.github.seijikohara.dbtester.api.export.LobHandling
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.sql.SQLException
import javax.sql.DataSource
import kotlin.io.path.createTempDirectory

/**
 * Demonstrates the [DataSetExporter] facade for exporting database state to files.
 *
 * This specification covers:
 * - [DataSetExporter.csv] - Export to CSV format
 * - [DataSetExporter.json] - Export to JSON format
 * - [DataSetExporter.yaml] - Export to YAML format
 * - [ExportConfiguration] - Custom export settings including [LobHandling]
 *
 * The export API generates dataset files from existing database state. Exported files follow the
 * same format as input dataset files and can be used as expected datasets.
 */
@DatabaseTest
class DataSetExportSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(DataSetExportSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:DataSetExportSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                DataSetExportSpec::class.java.classLoader.getResource(scriptPath)
                    ?: throw IllegalStateException("Script not found: $scriptPath")
            ).readText()
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .let { statements ->
                    dataSource.connection.use { connection ->
                        connection.createStatement().use { statement ->
                            statements.forEach { sql ->
                                runCatching { statement.execute(sql) }
                                    .onFailure { e ->
                                        throw RuntimeException("Failed to execute SQL: $sql", e as? SQLException ?: e)
                                    }
                            }
                        }
                    }
                }

        private fun executeSql(
            dataSource: DataSource,
            sql: String,
        ): Unit =
            dataSource.connection
                .use { connection ->
                    connection.createStatement().use { statement ->
                        statement.executeUpdate(sql)
                    }
                }.let { }
    }

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up the H2 in-memory database, schema, and seed data.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for DataSetExportSpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/DataSetExportSpec.sql")
            executeSql(dataSource, "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (1, 'Alice', 99.99)")
            executeSql(dataSource, "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (2, 'Bob', 149.50)")
            executeSql(dataSource, "INSERT INTO EXPORT_DATA (ID, NAME, AMOUNT) VALUES (3, 'Charlie', 75.00)")
            logger.info("Database setup completed with test data")
        }

    /**
     * Verifies that database state exports to CSV format.
     */
    @Test
    fun `should export database state to CSV file`() {
        val tempDir = createTempDirectory("dataset-export-csv")

        DataSetExporter.csv(dataSource, listOf("EXPORT_DATA"), tempDir)

        val exportedFile = tempDir.resolve("EXPORT_DATA.csv")
        Files.exists(exportedFile) shouldBe true
        val content = Files.readString(exportedFile, StandardCharsets.UTF_8)
        content shouldContain "Alice"
        content shouldContain "Bob"
        content shouldContain "Charlie"
    }

    /**
     * Verifies that database state exports to JSON format.
     */
    @Test
    fun `should export database state to JSON file`() {
        val tempDir = createTempDirectory("dataset-export-json")

        DataSetExporter.json(dataSource, listOf("EXPORT_DATA"), tempDir)

        val exportedFile = tempDir.resolve("EXPORT_DATA.json")
        Files.exists(exportedFile) shouldBe true
        val content = Files.readString(exportedFile, StandardCharsets.UTF_8)
        content shouldContain "Alice"
        content shouldContain "Bob"
    }

    /**
     * Verifies that database state exports to YAML format.
     */
    @Test
    fun `should export database state to YAML file`() {
        val tempDir = createTempDirectory("dataset-export-yaml")

        DataSetExporter.yaml(dataSource, listOf("EXPORT_DATA"), tempDir)

        val exportedFile = tempDir.resolve("EXPORT_DATA.yaml")
        Files.exists(exportedFile) shouldBe true
        val content = Files.readString(exportedFile, StandardCharsets.UTF_8)
        content shouldContain "Alice"
        content shouldContain "Bob"
    }

    /**
     * Verifies that export configuration customizes output behavior.
     *
     * Demonstrates using [ExportConfiguration.Builder] to customize null value representation and
     * [LobHandling] strategy.
     */
    @Test
    fun `should apply custom export configuration`() {
        val tempDir = createTempDirectory("dataset-export-config")
        val config =
            ExportConfiguration
                .builder()
                .lobHandling(LobHandling.OMIT)
                .nullValue("[NULL]")
                .build()

        DataSetExporter.export(dataSource, listOf("EXPORT_DATA"), tempDir, DataFormat.CSV, config)

        Files.exists(tempDir.resolve("EXPORT_DATA.csv")) shouldBe true
    }
}
