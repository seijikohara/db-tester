package example

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure.SpringBootDatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.sql.Date

/**
 * Demonstrates configuration of DB Tester via application.properties.
 *
 * This test demonstrates how to configure DB Tester using Spring Boot's property binding instead
 * of the programmatic Configuration API. The configuration is defined in
 * `application-custom-config.properties`.
 *
 * ## Configuration via application.properties
 *
 * The following properties are configured in `application-custom-config.properties`:
 *
 * ```properties
 * # Convention settings
 * db-tester.convention.expectation-suffix=/verify
 * db-tester.convention.scenario-marker=[TestCase]
 * db-tester.convention.data-format=CSV
 * db-tester.convention.table-merge-strategy=UNION_ALL
 *
 * # Operation settings
 * db-tester.operation.preparation=CLEAN_INSERT
 * db-tester.operation.expectation=NONE
 * ```
 *
 * ## Key Features Demonstrated
 *
 * - Custom expectation suffix: `/verify` instead of default `/expected`
 * - Custom scenario marker: `[TestCase]` instead of default `[Scenario]`
 * - Configuration via properties without programmatic API
 * - Spring profile-based configuration switching
 *
 * CSV files are located at:
 * - `src/test/resources/example/PropertiesConfigurationSpec/CONFIG_ITEMS.csv`
 * - `src/test/resources/example/PropertiesConfigurationSpec/verify/CONFIG_ITEMS.csv`
 */
@SpringBootTest(classes = [ExampleApplication::class])
@ActiveProfiles("custom-config")
class PropertiesConfigurationSpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(PropertiesConfigurationSpec::class.java)
    }

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var configuration: Configuration

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    /**
     * Verifies that Configuration is properly injected from properties.
     *
     * This test validates that the custom properties defined in
     * `application-custom-config.properties` are correctly bound to the Configuration bean.
     */
    @Test
    fun `should inject Configuration from properties`(): Unit =
        logger
            .info("Verifying Configuration injection from properties")
            .let {
                configuration shouldNotBe null
                configuration.conventions() shouldNotBe null
                configuration.operations() shouldNotBe null

                // Verify convention settings
                configuration.conventions().expectationSuffix() shouldBe "/verify"
                configuration.conventions().scenarioMarker() shouldBe "[TestCase]"
                configuration.conventions().dataFormat() shouldBe DataFormat.CSV
                configuration.conventions().tableMergeStrategy() shouldBe TableMergeStrategy.UNION_ALL

                // Verify operation settings
                configuration.operations().preparation() shouldBe Operation.CLEAN_INSERT
                configuration.operations().expectation() shouldBe Operation.NONE

                logger.info("Configuration verification completed successfully")
            }

    /**
     * Demonstrates custom scenario marker usage configured via properties.
     *
     * CSV files use `[TestCase]` column instead of default `[Scenario]` to filter rows
     * by test method name. This configuration is set via `db-tester.convention.scenario-marker`
     * property.
     *
     * Test flow:
     * - Preparation: Loads rows matching `should use custom scenario marker from properties`
     * - Execution: Inserts a new record
     * - Expectation: Verifies final state from `verify/CONFIG_ITEMS.csv`
     */
    @Test
    @Preparation
    @Expectation
    fun `should use custom scenario marker from properties`(): Unit =
        logger
            .info("Testing custom scenario marker [TestCase] configured via properties")
            .let {
                jdbcTemplate.update(
                    "INSERT INTO CONFIG_ITEMS (ID, NAME, STATUS, CREATED_DATE) VALUES (?, ?, ?, ?)",
                    2,
                    "Beta Feature",
                    "ENABLED",
                    Date.valueOf("2024-02-01"),
                )
                logger.info("Custom scenario marker test completed")
            }

    /**
     * Demonstrates custom expectation suffix usage configured via properties.
     *
     * Expected data is loaded from `/verify` directory instead of default `/expected`.
     * This configuration is set via `db-tester.convention.expectation-suffix` property.
     *
     * Test flow:
     * - Preparation: Loads initial data
     * - Execution: Updates record status
     * - Expectation: Verifies from `verify/CONFIG_ITEMS.csv` instead of `expected/`
     */
    @Test
    @Preparation
    @Expectation
    fun `should use custom expectation suffix from properties`(): Unit =
        logger
            .info("Testing custom expectation suffix /verify configured via properties")
            .let {
                jdbcTemplate.update("UPDATE CONFIG_ITEMS SET STATUS = ? WHERE ID = ?", "DISABLED", 1)
                logger.info("Custom expectation suffix test completed")
            }
}
