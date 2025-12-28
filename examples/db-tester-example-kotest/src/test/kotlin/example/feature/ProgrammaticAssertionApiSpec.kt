package example.feature

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates both annotation-based and programmatic database validation approaches with Kotest.
 *
 * This specification illustrates two complementary validation strategies:
 * - Annotation-based validation using [Expectation] - suitable for standard table comparisons
 *   with convention-based expected data
 * - Programmatic validation using custom SQL queries - provides flexibility for complex scenarios
 *   where annotation-based testing is insufficient
 *
 * Key programmatic API features available in DatabaseAssertion:
 * - assertEqualsByQuery: Compare expected data against SQL query results
 * - assertEquals: Compare two datasets or tables directly
 * - assertEqualsIgnoreColumns: Compare datasets ignoring specific columns
 *
 * Programmatic assertions are useful for custom SQL queries, dynamic column filtering, mid-test
 * state verification, or comparing multiple dataset sources.
 */
class ProgrammaticAssertionApiSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammaticAssertionApiSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:ProgrammaticAssertionApiSpec;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                ProgrammaticAssertionApiSpec::class.java.classLoader.getResource(scriptPath)
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
        logger.info("Setting up H2 in-memory database for ProgrammaticAssertionApiSpec").also {
            dataSource = createDataSource()
            registry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/ProgrammaticAssertionApiSpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Demonstrates basic programmatic assertion without annotations.
     *
     * Shows direct use of DatabaseAssertion assertion APIs for custom validation scenarios
     * where annotation-based testing is insufficient.
     *
     * Test flow:
     * - Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
     * - Execution: Inserts (3,Value3,300,NULL)
     * - Expectation: Verifies all three records including NULL COLUMN3
     */
    @Test
    @Preparation
    @Expectation
    fun `should demonstrate basic programmatic API`(): Unit =
        logger.info("Running basic programmatic API test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3) VALUES (3, 'Value3', 300, NULL)")
            logger.info("Record inserted successfully")
        }

    /**
     * Demonstrates programmatic custom SQL query validation.
     *
     * This test shows validation using direct SQL queries instead of relying on
     * [Expectation] annotation. Programmatic assertions provide flexibility for custom
     * validation scenarios.
     *
     * Test flow:
     * - Preparation: TABLE1(1,Value1,100,Extra1), (2,Value2,200,Extra2)
     * - Execution: Inserts (3,Value3,300,NULL) and (4,Value4,400,NULL)
     * - Expectation: Validates using SQL queries to verify row count and specific records
     */
    @Test
    @Preparation
    fun `should validate using multiple queries`(): Unit =
        logger.info("Running multiple queries validation test").also {
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (3, 'Value3', 300)")
            executeSql(dataSource, "INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2) VALUES (4, 'Value4', 400)")
            logger.info("Two records inserted")

            // Verify total row count is 4
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT COUNT(*) as cnt FROM TABLE1").use { rs ->
                        rs.next()
                        rs.getInt("cnt") shouldBe 4
                    }
                }
            }

            // Verify newly inserted records have correct values
            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement
                        .executeQuery("SELECT COLUMN1, COLUMN2 FROM TABLE1 WHERE ID IN (3, 4) ORDER BY ID")
                        .use { rs ->
                            rs.next() shouldBe true
                            rs.getString("COLUMN1") shouldBe "Value3"
                            rs.getInt("COLUMN2") shouldBe 300

                            rs.next() shouldBe true
                            rs.getString("COLUMN1") shouldBe "Value4"
                            rs.getInt("COLUMN2") shouldBe 400

                            rs.next() shouldBe false
                        }
                }
            }
            logger.info("All assertions passed")
        }
}
