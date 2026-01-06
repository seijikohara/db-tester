package example

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure.SpringBootDatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.simple.JdbcClient
import javax.sql.DataSource

/**
 * Integration test demonstrating multiple DataSource support with Kotest.
 *
 * This test verifies that:
 *
 * - Multiple DataSources can be defined and injected via Spring
 * - The `@Primary` DataSource is used for default operations
 * - Named DataSources are accessible via their bean names
 * - The `@DataSet` annotation works with the primary DataSource
 *
 * CSV files are located at:
 * - `src/test/resources/example/MultipleDataSourcesSpec/USERS.csv`
 */
@SpringBootTest(classes = [ExampleApplication::class, MultipleDataSourcesSpec.MultiDataSourceConfig::class])
class MultipleDataSourcesSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(MultipleDataSourcesSpec::class.java)
    }

    @Autowired
    @Qualifier("mainDb")
    private lateinit var mainDataSource: DataSource

    @Autowired
    @Qualifier("archiveDb")
    private lateinit var archiveDataSource: DataSource

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    /**
     * Verifies that multiple DataSources are injected correctly.
     */
    @Test
    fun `should have multiple DataSources injected`(): Unit =
        logger
            .info("Verifying multiple DataSource injection")
            .let {
                mainDataSource shouldNotBe null
                archiveDataSource shouldNotBe null
                (mainDataSource !== archiveDataSource) shouldBe true
                logger.info("Successfully verified multiple DataSource injection")
            }

    /**
     * Verifies that preparation works with the default (primary) DataSource.
     */
    @Test
    @DataSet
    fun `should prepare default DataSource`(): Unit =
        logger
            .info("Testing preparation with default (primary) DataSource")
            .let {
                JdbcClient
                    .create(mainDataSource)
                    .sql("SELECT COUNT(*) FROM USERS")
                    .query(Long::class.java)
                    .single()
                    .also { count ->
                        count shouldBe 2L
                        logger.info("Primary DataSource has {} users", count)
                    }
            }.let { }

    /**
     * Verifies that named DataSource can be accessed directly.
     */
    @Test
    fun `should access named DataSource directly`(): Unit =
        logger
            .info("Testing direct access to named DataSource (archiveDb)")
            .let {
                JdbcClient.create(archiveDataSource).also { jdbcClient ->
                    jdbcClient
                        .sql(
                            """
                            INSERT INTO ARCHIVED_USERS (ID, NAME, EMAIL, ARCHIVED_AT)
                            VALUES (1, 'Test', 'test@example.com', CURRENT_TIMESTAMP)
                            """.trimIndent(),
                        ).update()

                    jdbcClient
                        .sql("SELECT COUNT(*) FROM ARCHIVED_USERS")
                        .query(Long::class.java)
                        .single()
                        .also { count ->
                            count shouldBe 1L
                            logger.info("archiveDb has {} archived users", count)
                        }
                }
            }.let { }

    /**
     * Test configuration that defines multiple DataSources.
     */
    @TestConfiguration
    class MultiDataSourceConfig {
        @Bean
        @Primary
        fun mainDb(): DataSource =
            DataSourceBuilder
                .create()
                .url("jdbc:h2:mem:kotest_maindb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build()
                .also { dataSource ->
                    JdbcClient
                        .create(dataSource)
                        .sql(
                            """
                            CREATE TABLE IF NOT EXISTS USERS (
                                ID BIGINT PRIMARY KEY,
                                NAME VARCHAR(255) NOT NULL,
                                EMAIL VARCHAR(255) NOT NULL
                            )
                            """.trimIndent(),
                        ).update()
                }

        @Bean
        fun archiveDb(): DataSource =
            DataSourceBuilder
                .create()
                .url("jdbc:h2:mem:kotest_archivedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
                .driverClassName("org.h2.Driver")
                .username("sa")
                .password("")
                .build()
                .also { dataSource ->
                    JdbcClient
                        .create(dataSource)
                        .sql(
                            """
                            CREATE TABLE IF NOT EXISTS ARCHIVED_USERS (
                                ID BIGINT PRIMARY KEY,
                                NAME VARCHAR(255) NOT NULL,
                                EMAIL VARCHAR(255) NOT NULL,
                                ARCHIVED_AT TIMESTAMP NOT NULL
                            )
                            """.trimIndent(),
                        ).update()
                }
    }
}
