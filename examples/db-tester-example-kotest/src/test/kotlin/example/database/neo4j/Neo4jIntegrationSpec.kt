package example.database.neo4j

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import org.neo4j.jdbc.Neo4jDataSource
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.neo4j.Neo4jContainer
import java.sql.DriverManager
import javax.sql.DataSource

/**
 * Neo4j Graph Database integration test using Testcontainers.
 *
 * This specification validates that the framework works correctly with Neo4j graph database
 * using Testcontainers. This is a smoke test to ensure Neo4j compatibility with the DB Tester
 * framework.
 *
 * Neo4j is a graph database that stores data as nodes and relationships. The JDBC driver
 * returns query results in tabular format compatible with standard JDBC operations.
 *
 * @see <a href="https://java.testcontainers.org/test_framework_integration/spock/">Testcontainers Spock Integration</a>
 */
class Neo4jIntegrationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(Neo4jIntegrationSpec::class.java)

        private val neo4j: Neo4jContainer =
            Neo4jContainer("neo4j:5-community")
                .withoutAuthentication()

        /**
         * Creates a Neo4j DataSource from the Testcontainer.
         *
         * @param container the Neo4j container
         * @return configured DataSource
         */
        private fun createDataSource(container: Neo4jContainer): DataSource =
            Neo4jDataSource().apply {
                // Neo4j JDBC driver 6.x URL format: jdbc:neo4j://<host>:<port>
                // enableSQLTranslation: Automatically translate SQL to Cypher
                setUrl(container.getBoltUrl().replace("bolt://", "jdbc:neo4j://") + "?enableSQLTranslation=true")
            }

        /**
         * Executes a Cypher script from classpath.
         *
         * @param container the Neo4j container for direct connection
         * @param scriptPath the classpath resource path
         */
        private fun executeScript(
            container: Neo4jContainer,
            scriptPath: String,
        ): Unit =
            (
                Neo4jIntegrationSpec::class.java.classLoader.getResource(scriptPath)
                    ?: throw IllegalStateException("Script not found: $scriptPath")
            ).let { resource ->
                // Use Cypher natively without SQL translation for DDL script
                val jdbcUrl = container.getBoltUrl().replace("bolt://", "jdbc:neo4j://")
                DriverManager.getConnection(jdbcUrl).use { connection ->
                    connection.createStatement().use { statement ->
                        resource
                            .readText()
                            .split(";")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && !it.startsWith("//") }
                            .forEach { cypher ->
                                runCatching { statement.execute(cypher) }
                                    .onFailure { e ->
                                        throw RuntimeException("Failed to execute Cypher: $cypher", e)
                                    }
                            }
                    }
                }
            }
    }

    private val registry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    /**
     * Sets up Neo4j database connection and schema using Testcontainers.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up Neo4j Testcontainer").also {
            (neo4j as GenericContainer<*>).start()
            val dataSource = createDataSource(neo4j)
            registry.registerDefault(dataSource)
            executeScript(neo4j, "ddl/database/neo4j/neo4j-integration.cypher")
            logger.info("Neo4j database setup completed")
        }

    /**
     * Cleans up database resources after all tests complete.
     */
    @AfterAll
    fun cleanupDatabase(): Unit = (neo4j as GenericContainer<*>).stop()

    /**
     * Smoke test verifying basic framework functionality with Neo4j.
     *
     * This test validates:
     * - Data can be loaded from CSV into Neo4j nodes
     * - Data can be verified against expected CSV via Cypher queries
     * - Framework handles graph database results correctly
     */
    @Test
    @Preparation(dataSets = [DataSet(scenarioNames = ["smokeTest"])])
    @Expectation(dataSets = [DataSet(scenarioNames = ["smokeTest"])])
    fun `should execute basic database operations on Neo4j`(): Unit = logger.info("Running Neo4j integration smoke test")
}
