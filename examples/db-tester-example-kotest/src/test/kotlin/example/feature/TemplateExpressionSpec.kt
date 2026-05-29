package example.feature

import io.github.seijikohara.dbtester.api.annotation.ColumnStrategy
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.Strategy
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
import io.github.seijikohara.dbtester.kotest.extension.DatabaseTestSupport
import io.kotest.core.spec.style.AnnotationSpec
import org.h2.jdbcx.JdbcDataSource
import org.slf4j.LoggerFactory
import java.sql.SQLException
import javax.sql.DataSource

/**
 * Demonstrates template expression processing in CSV dataset values.
 *
 * Kotest [AnnotationSpec] requires top-level specification classes, so each expression family is a
 * dedicated specification rather than a nested class. The JUnit equivalent uses `@Nested` classes.
 *
 * Template expressions generate dynamic values at data load time. This specification covers:
 * - `${uuid}` - Random UUID generation
 * - `${sequence:N}` and `${sequence}` - Auto-incrementing sequence numbers
 * - `${now}` and `${now+Xd}` - Current and relative timestamp generation
 * - `${faker.xxx.yyy}` - Datafaker integration for realistic test data
 *
 * Dynamic values are validated using [Strategy.REGEX] and [Strategy.NOT_NULL] comparison strategies
 * in the expected dataset.
 */
object TemplateExpressionSpec

/**
 * Tests `${uuid}` template expression.
 *
 * The `${uuid}` expression generates a random UUID (version 4) for each occurrence. The expected
 * dataset uses [Strategy.REGEX] to validate the UUID format.
 */
@DatabaseTest
class TemplateExpressionUuidSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(TemplateExpressionUuidSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TemplateExpressionSpec_UUID;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TemplateExpressionUuidSpec::class.java.classLoader.getResource(scriptPath)
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up database for UUID expression test").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TemplateExpressionSpec.sql")
            logger.info("UUID expression test setup completed")
        }

    /**
     * Verifies that `${uuid}` generates valid UUID values.
     */
    @Test
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionUuidSpec/shouldGenerateUuid/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionUuidSpec/shouldGenerateUuid/expected/",
                columnStrategies = [
                    ColumnStrategy(
                        name = "UUID_VALUE",
                        strategy = Strategy.REGEX,
                        pattern = "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}",
                    ),
                ],
            ),
        ],
    )
    fun shouldGenerateUuid(): Unit = logger.info("UUID expression validated via REGEX strategy")
}

/**
 * Tests `${sequence}` template expressions.
 *
 * The `${sequence:N}` expression sets the counter to N and returns N. The `${sequence}` expression
 * increments the counter and returns the next value.
 */
@DatabaseTest
class TemplateExpressionSequenceSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(TemplateExpressionSequenceSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TemplateExpressionSpec_Sequence;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TemplateExpressionSequenceSpec::class.java.classLoader.getResource(scriptPath)
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up database for sequence expression test").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TemplateExpressionSpec.sql")
            logger.info("Sequence expression test setup completed")
        }

    /**
     * Verifies that `${sequence:N}` and `${sequence}` generate sequential values.
     */
    @Test
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionSequenceSpec/shouldGenerateSequence/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionSequenceSpec/shouldGenerateSequence/expected/",
            ),
        ],
    )
    fun shouldGenerateSequence(): Unit = logger.info("Sequence values verified via STRICT comparison")
}

/**
 * Tests `${now}` template expressions.
 *
 * The `${now}` expression generates the current timestamp. The `${now+Xd}` expression generates a
 * relative timestamp.
 */
@DatabaseTest
class TemplateExpressionTimestampSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(TemplateExpressionTimestampSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TemplateExpressionSpec_Timestamp;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TemplateExpressionTimestampSpec::class.java.classLoader.getResource(scriptPath)
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up database for timestamp expression test").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TemplateExpressionSpec.sql")
            logger.info("Timestamp expression test setup completed")
        }

    /**
     * Verifies that `${now}` and `${now+1d}` generate timestamp values.
     */
    @Test
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionTimestampSpec/shouldGenerateTimestamp/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionTimestampSpec/shouldGenerateTimestamp/expected/",
                columnStrategies = [ColumnStrategy(name = "CREATED_AT", strategy = Strategy.NOT_NULL)],
            ),
        ],
    )
    fun shouldGenerateTimestamp(): Unit = logger.info("Timestamps validated via NOT_NULL strategy")
}

/**
 * Tests `${faker.xxx.yyy}` template expressions.
 *
 * The `${faker.xxx.yyy}` expression uses the Datafaker library to generate realistic test data. The
 * expected dataset uses [Strategy.NOT_NULL] to validate the generated value.
 */
@DatabaseTest
class TemplateExpressionFakerSpec :
    AnnotationSpec(),
    DatabaseTestSupport {
    companion object {
        private val logger = LoggerFactory.getLogger(TemplateExpressionFakerSpec::class.java)

        private fun createDataSource(): DataSource =
            JdbcDataSource().apply {
                setURL("jdbc:h2:mem:TemplateExpressionSpec_Faker;DB_CLOSE_DELAY=-1")
                user = "sa"
                password = ""
            }

        private fun executeScript(
            dataSource: DataSource,
            scriptPath: String,
        ): Unit =
            (
                TemplateExpressionFakerSpec::class.java.classLoader.getResource(scriptPath)
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

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupDatabase(): Unit =
        logger.info("Setting up database for Datafaker expression test").also {
            dataSource = createDataSource()
            dbTesterRegistry.registerDefault(dataSource)
            executeScript(dataSource, "ddl/feature/TemplateExpressionSpec.sql")
            logger.info("Datafaker expression test setup completed")
        }

    /**
     * Verifies that `${faker.name.fullName}` generates non-null name values.
     */
    @Test
    @DataSet(
        operation = Operation.CLEAN_INSERT,
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionFakerSpec/shouldGenerateFakerData/",
            ),
        ],
    )
    @ExpectedDataSet(
        sources = [
            DataSetSource(
                resourceLocation = "classpath:example/feature/TemplateExpressionFakerSpec/shouldGenerateFakerData/expected/",
                columnStrategies = [ColumnStrategy(name = "NAME", strategy = Strategy.NOT_NULL)],
            ),
        ],
    )
    fun shouldGenerateFakerData(): Unit = logger.info("Faker names validated via NOT_NULL strategy")
}
