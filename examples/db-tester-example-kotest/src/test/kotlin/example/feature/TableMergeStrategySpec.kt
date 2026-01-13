package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.time.Duration
import javax.sql.DataSource

/**
 * Demonstrates different table merge strategies when multiple datasets contain the same table.
 *
 * This specification demonstrates the four merge strategies:
 * - [TableMergeStrategy.FIRST] - Use only the first occurrence of each table
 * - [TableMergeStrategy.LAST] - Use only the last occurrence of each table
 * - [TableMergeStrategy.UNION] - Merge tables, removing duplicate rows
 * - [TableMergeStrategy.UNION_ALL] - Merge tables, keeping all rows (default)
 *
 * Note: Multiple @DataSet annotations in a single @Preparation are demonstrated
 * in the JUnit TableMergeStrategyTest. This Kotest specification demonstrates
 * single dataset loading with different merge strategy configurations.
 */
object TableMergeStrategySpec

/**
 * Tests FIRST merge strategy configuration.
 *
 * Demonstrates configuring [TableMergeStrategy.FIRST] in [ConventionSettings].
 */
@DatabaseTest
class FirstStrategySpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(FirstStrategySpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration
                .builder()
                .conventions(
                    ConventionSettings
                        .builder()
                        .baseDirectory(null)
                        .expectationSuffix("/expected")
                        .scenarioMarker("[Scenario]")
                        .dataFormat(DataFormat.CSV)
                        .tableMergeStrategy(TableMergeStrategy.FIRST)
                        .loadOrderFileName(ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME)
                        .globalExcludeColumns(emptySet())
                        .globalColumnStrategies(emptyMap())
                        .rowOrdering(RowOrdering.ORDERED)
                        .queryTimeout(null)
                        .retryCount(0)
                        .retryDelay(Duration.ofMillis(100))
                        .transactionMode(TransactionMode.SINGLE_TRANSACTION)
                        .build(),
                ).build()

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TableMergeStrategySpec_FIRST;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                FirstStrategySpec::class.java.classLoader.getResource(scriptPath)
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
    override val dbTesterConfiguration: Configuration = sharedConfiguration
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for FirstStrategySpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TableMergeStrategySpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies FIRST strategy configuration is applied.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/FirstStrategySpec/dataset1/")],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/FirstStrategySpec/should use only first dataset/expected/",
            ),
        ],
    )
    fun `should use only first dataset`(): Unit =
        logger.info("Running FIRST strategy test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO MERGE_TABLE (ID, NAME)
                VALUES (3, 'Charlie')
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}

/**
 * Tests LAST merge strategy configuration.
 *
 * Demonstrates configuring [TableMergeStrategy.LAST] in [ConventionSettings].
 */
@DatabaseTest
class LastStrategySpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(LastStrategySpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration
                .builder()
                .conventions(
                    ConventionSettings
                        .builder()
                        .baseDirectory(null)
                        .expectationSuffix("/expected")
                        .scenarioMarker("[Scenario]")
                        .dataFormat(DataFormat.CSV)
                        .tableMergeStrategy(TableMergeStrategy.LAST)
                        .loadOrderFileName(ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME)
                        .globalExcludeColumns(emptySet())
                        .globalColumnStrategies(emptyMap())
                        .rowOrdering(RowOrdering.ORDERED)
                        .queryTimeout(null)
                        .retryCount(0)
                        .retryDelay(Duration.ofMillis(100))
                        .transactionMode(TransactionMode.SINGLE_TRANSACTION)
                        .build(),
                ).build()

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TableMergeStrategySpec_LAST;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                LastStrategySpec::class.java.classLoader.getResource(scriptPath)
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
    override val dbTesterConfiguration: Configuration = sharedConfiguration
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for LastStrategySpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TableMergeStrategySpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies LAST strategy configuration is applied.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/LastStrategySpec/dataset2/")],
    )
    @ExpectedDataSet(
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/LastStrategySpec/should use only last dataset/expected/")],
    )
    fun `should use only last dataset`(): Unit =
        logger.info("Running LAST strategy test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO MERGE_TABLE (ID, NAME)
                VALUES (1, 'Alice')
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}

/**
 * Tests UNION merge strategy configuration.
 *
 * Demonstrates configuring [TableMergeStrategy.UNION] in [ConventionSettings].
 */
@DatabaseTest
class UnionStrategySpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(UnionStrategySpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration
                .builder()
                .conventions(
                    ConventionSettings
                        .builder()
                        .baseDirectory(null)
                        .expectationSuffix("/expected")
                        .scenarioMarker("[Scenario]")
                        .dataFormat(DataFormat.CSV)
                        .tableMergeStrategy(TableMergeStrategy.UNION)
                        .loadOrderFileName(ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME)
                        .globalExcludeColumns(emptySet())
                        .globalColumnStrategies(emptyMap())
                        .rowOrdering(RowOrdering.ORDERED)
                        .queryTimeout(null)
                        .retryCount(0)
                        .retryDelay(Duration.ofMillis(100))
                        .transactionMode(TransactionMode.SINGLE_TRANSACTION)
                        .build(),
                ).build()

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TableMergeStrategySpec_UNION;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                UnionStrategySpec::class.java.classLoader.getResource(scriptPath)
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
    override val dbTesterConfiguration: Configuration = sharedConfiguration
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for UnionStrategySpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TableMergeStrategySpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies UNION strategy configuration is applied.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/UnionStrategySpec/dataset1/")],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/UnionStrategySpec/should merge and remove duplicates/expected/",
            ),
        ],
    )
    fun `should merge and remove duplicates`(): Unit =
        logger.info("Running UNION strategy test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO MERGE_TABLE (ID, NAME)
                VALUES (3, 'Charlie')
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}

/**
 * Tests UNION_ALL merge strategy configuration (default).
 *
 * Demonstrates configuring [TableMergeStrategy.UNION_ALL] in [ConventionSettings].
 */
@DatabaseTest
class UnionAllStrategySpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(UnionAllStrategySpec::class.java)

        private val sharedConfiguration: Configuration =
            Configuration
                .builder()
                .conventions(
                    ConventionSettings
                        .builder()
                        .baseDirectory(null)
                        .expectationSuffix("/expected")
                        .scenarioMarker("[Scenario]")
                        .dataFormat(DataFormat.CSV)
                        .tableMergeStrategy(TableMergeStrategy.UNION_ALL)
                        .loadOrderFileName(ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME)
                        .globalExcludeColumns(emptySet())
                        .globalColumnStrategies(emptyMap())
                        .rowOrdering(RowOrdering.ORDERED)
                        .queryTimeout(null)
                        .retryCount(0)
                        .retryDelay(Duration.ofMillis(100))
                        .transactionMode(TransactionMode.SINGLE_TRANSACTION)
                        .build(),
                ).build()

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TableMergeStrategySpec_UNION_ALL;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                UnionAllStrategySpec::class.java.classLoader.getResource(scriptPath)
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
    override val dbTesterConfiguration: Configuration = sharedConfiguration
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up H2 in-memory database for UnionAllStrategySpec").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TableMergeStrategySpec.sql")
            logger.info("Database setup completed")
        }

    /**
     * Verifies UNION_ALL strategy configuration is applied.
     */
    @Test
    @DataSet(
        operation = Operation.INSERT,
        sources = [DataSetSource(resourceLocation = "classpath:example/feature/UnionAllStrategySpec/dataset1/")],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/UnionAllStrategySpec/should merge and keep all rows/expected/",
            ),
        ],
    )
    fun `should merge and keep all rows`(): Unit =
        logger.info("Running UNION_ALL strategy test").also {
            executeSql(
                dataSource,
                """
                INSERT INTO MERGE_TABLE (ID, NAME)
                VALUES (3, 'Charlie')
                """.trimIndent(),
            )
            logger.info("Data inserted successfully")
        }
}
