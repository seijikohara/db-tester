package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Base specification demonstrating annotation inheritance for database tests with Kotest.
 *
 * This base specification provides:
 * - Class-level [DataSet] annotation inherited by subclasses
 * - Common database setup and utility methods
 * - Reusable test infrastructure
 *
 * Child specifications inherit:
 * - The class-level [DataSet] annotation
 * - Database setup and helper methods
 *
 * @see InheritedAnnotationSpec
 */
@DataSet(
    sources = [
        DataSetSource(
            resourceLocation = "classpath:example/feature/InheritanceSpecBase/",
            scenarioNames = ["baseSetup"],
        ),
    ],
)
abstract class InheritanceSpecBase : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(InheritanceSpecBase::class.java)

        /** Static registry shared across all tests. */
        private var sharedRegistry: DataSourceRegistry? = null

        /** Static DataSource shared across all tests. */
        private var sharedDataSource: DataSource? = null

        /**
         * Initializes shared resources (DataSource, Registry).
         */
        private fun initializeSharedResources(): Unit =
            JdbcDataSource()
                .apply {
                    setURL("jdbc:h2:mem:InheritanceSpecBase;DB_CLOSE_DELAY=-1")
                    user = "sa"
                    password = ""
                }.also { dataSource ->
                    sharedDataSource = dataSource
                    sharedRegistry =
                        DataSourceRegistry().also { registry ->
                            registry.registerDefault(dataSource)
                        }
                }.let { }

        /**
         * Executes a SQL script from classpath.
         *
         * @param dataSource the data source to execute the script on
         * @param scriptPath the classpath resource path
         */
        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                InheritanceSpecBase::class.java.classLoader.getResource(scriptPath)
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
    }

    /** DataSource for database operations. */
    protected lateinit var dataSource: DataSource

    /**
     * Gets the DataSourceRegistry.
     *
     * @return the registry
     */
    fun getDbTesterRegistry(): DataSourceRegistry =
        sharedRegistry ?: run {
            initializeSharedResources()
            sharedRegistry!!
        }

    init {
        extensions(DatabaseTestExtension(registryProvider = { getDbTesterRegistry() }))
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     *
     * This method is inherited by subclasses and provides shared database initialization.
     */
    @BeforeAll
    fun setupSpec(): Unit =
        logger.info("Setting up H2 in-memory database for InheritanceSpecBase").also {
            sharedDataSource?.let { dataSource = it } ?: run {
                initializeSharedResources()
                dataSource = sharedDataSource!!
            }
            executeScript(dataSource, "ddl/feature/InheritanceSpecBase.sql")
            logger.info("Database setup completed")
        }

    /**
     * Gets the count of records in a table.
     *
     * @param tableName the table name
     * @return the record count
     */
    protected fun getRecordCount(tableName: String): Int =
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) as cnt FROM $tableName").use { rs ->
                    rs.next()
                    rs.getInt("cnt")
                }
            }
        }

    /**
     * Executes a SQL update statement.
     *
     * @param sql the SQL statement to execute
     */
    protected fun executeSql(sql: String): Unit =
        dataSource.connection
            .use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(sql)
                }
            }.let { }
}
