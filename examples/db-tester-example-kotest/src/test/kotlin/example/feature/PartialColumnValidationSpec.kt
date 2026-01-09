package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates partial column validation techniques using CSV files with Kotest.
 *
 * This specification illustrates:
 * - Validating only specific columns via partial CSV files
 * - Excluding auto-generated columns (ID, timestamps) from CSV expectations
 * - Testing business logic without worrying about database-generated values
 * - Using custom expectation paths for different validation scenarios
 *
 * Use partial column validation when:
 * - Testing tables with auto-increment IDs
 * - Ignoring timestamp columns (CREATED_AT, UPDATED_AT)
 * - Focusing on business-relevant columns only
 * - Dealing with database-generated values (UUIDs, sequences)
 *
 * Note: For programmatic column exclusion using
 * [io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion.assertEqualsIgnoreColumns],
 * you would need to manually create datasets using DbUnit APIs.
 */
class PartialColumnValidationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(PartialColumnValidationSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:PartialColumnValidationSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                PartialColumnValidationSpec::class.java.classLoader.getResource(scriptPath)
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
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for PartialColumnValidationSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/PartialColumnValidationSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates partial column validation using CSV with subset of columns.
     *
     * CSV contains only business-relevant columns, ignoring auto-generated ID and timestamp.
     *
     * Test flow:
     * - Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
     * - Execution: Inserts (DELETE,User,789) - ID and COLUMN4/5 auto-generated
     * - Expectation: Verifies all three records exist with expected COLUMN1/2/3 values
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should validate partial columns via CSV`(): Unit =
        logger.info("Inserting new record with business columns only").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
                VALUES ('DELETE', 'User', 789)
                """.trimIndent(),
            )
            logger.info("Record inserted successfully")
        }

    /**
     * Demonstrates validation with partial CSV (ignoring auto-generated columns).
     *
     * CSV file contains only business columns, excluding ID, COLUMN4, and COLUMN5.
     *
     * Test flow:
     * - Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
     * - Execution: Inserts (UPDATE,Product,456) - same values but different auto-generated ID
     * - Expectation: Verifies three records with matching COLUMN1/2/3, ignoring ID differences
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/PartialColumnValidationSpec/expected-ignore-columns/",
            ),
        ],
    )
    fun `should ignore auto generated columns`(): Unit =
        logger.info("Inserting duplicate business values with different auto-generated ID").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
                VALUES ('UPDATE', 'Product', 456)
                """.trimIndent(),
            )
            logger.info("Record inserted successfully")
        }

    /**
     * Demonstrates validation with minimal CSV columns including default value verification.
     *
     * CSV contains essential business columns plus COLUMN5 to verify DEFAULT 'SYSTEM' value.
     *
     * Test flow:
     * - Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
     * - Execution: Inserts (CREATE,Order,999) - COLUMN5 defaults to 'SYSTEM'
     * - Expectation: Verifies COLUMN1/2/3/5 values including default COLUMN5='SYSTEM'
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/PartialColumnValidationSpec/expected-combined/",
            ),
        ],
    )
    fun `should validate with minimal columns`(): Unit =
        logger.info("Inserting record with minimal columns").also {
            executeSql(
                dataSource,
                """
                INSERT INTO TABLE1 (COLUMN1, COLUMN2, COLUMN3)
                VALUES ('CREATE', 'Order', 999)
                """.trimIndent(),
            )
            logger.info("Record inserted successfully")
        }

    /**
     * Demonstrates validation after UPDATE operation.
     *
     * Note: This test validates the complete table state after an update operation. True partial
     * column validation (validating only specific columns while ignoring others) requires
     * programmatic assertions using
     * [io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion.assertEqualsIgnoreColumns],
     * which is beyond the scope of annotation-based testing.
     *
     * Test flow:
     * - Preparation: TABLE1(CREATE,User,123), (UPDATE,Product,456) - only COLUMN1/2/3
     * - Execution: Updates ID=1 COLUMN3 from 123 to 555
     * - Expectation: Verifies (CREATE,User,555), (UPDATE,Product,456) with updated value
     */
    @Test
    @DataSet
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/PartialColumnValidationSpec/expected-after-update/",
            ),
        ],
    )
    fun `should validate after update`(): Unit =
        logger.info("Updating existing record column value").also {
            executeSql(
                dataSource,
                "UPDATE TABLE1 SET COLUMN3 = 555 WHERE COLUMN1 = 'CREATE'",
            )
            logger.info("Record updated successfully")
        }
}
