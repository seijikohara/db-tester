package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates different data format configurations (CSV and TSV) with Kotest.
 *
 * This specification demonstrates:
 * - Using CSV format (default) with [DataFormat.CSV]
 * - Using TSV format with [DataFormat.TSV]
 * - Configuring data format via [ConventionSettings]
 *
 * CSV files use comma (, ) as delimiter, TSV files use tab character as delimiter.
 */
object DataFormatSpec

/**
 * Tests CSV format (default configuration).
 *
 * CSV files use comma as field delimiter:
 * ```
 * ID,NAME,DATA_VALUE
 * 1,Alice,100
 * 2,Bob,200
 * ```
 */
class CsvFormatSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(CsvFormatSpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration.withConventions(
                ConventionSettings(
                    null,
                    "/expected",
                    "[Scenario]",
                    DataFormat.CSV,
                    TableMergeStrategy.UNION_ALL,
                    ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                    emptySet(),
                    emptyMap(),
                ),
            )

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:DataFormatSpec_CSV;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                CsvFormatSpec::class.java.classLoader.getResource(scriptPath)
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

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    init {
        extensions(
            DatabaseTestExtension(
                registryProvider = { registry },
                configurationProvider = { sharedConfiguration },
            ),
        )
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for CsvFormatSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/DataFormatSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies that CSV format files are loaded correctly.
     *
     * Test flow:
     * - Preparation: Loads data from CSV file (comma-separated)
     * - Execution: Inserts additional record
     * - Expectation: Verifies data from expected CSV file
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/DataFormatSpec\$CsvFormatSpec/should load CSV format data/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/DataFormatSpec\$CsvFormatSpec/should load CSV format data/expected/",
            ),
        ],
    )
    fun `should load CSV format data`(): Unit =
        logger.info("Running CSV format test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE)
                VALUES (3, 'Charlie', 300)
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}

/**
 * Tests TSV format configuration.
 *
 * TSV files use tab as field delimiter:
 * ```
 * ID	NAME	DATA_VALUE
 * 1	Alice	100
 * 2	Bob	200
 * ```
 */
class TsvFormatSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(TsvFormatSpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration.withConventions(
                ConventionSettings(
                    null,
                    "/expected",
                    "[Scenario]",
                    DataFormat.TSV,
                    TableMergeStrategy.UNION_ALL,
                    ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                    emptySet(),
                    emptyMap(),
                ),
            )

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:DataFormatSpec_TSV;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TsvFormatSpec::class.java.classLoader.getResource(scriptPath)
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

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    init {
        extensions(
            DatabaseTestExtension(
                registryProvider = { registry },
                configurationProvider = { sharedConfiguration },
            ),
        )
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for TsvFormatSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/DataFormatSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies that TSV format files are loaded correctly.
     *
     * Test flow:
     * - Preparation: Loads data from TSV file (tab-separated)
     * - Execution: Inserts additional record
     * - Expectation: Verifies data from expected TSV file
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/DataFormatSpec\$TsvFormatSpec/should load TSV format data/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/DataFormatSpec\$TsvFormatSpec/should load TSV format data/expected/",
            ),
        ],
    )
    fun `should load TSV format data`(): Unit =
        logger.info("Running TSV format test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO DATA_FORMAT (ID, NAME, DATA_VALUE)
                VALUES (3, 'Charlie', 300)
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}
