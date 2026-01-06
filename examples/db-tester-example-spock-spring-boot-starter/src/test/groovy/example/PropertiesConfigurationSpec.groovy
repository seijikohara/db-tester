package example

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure.SpringBootDatabaseTest
import java.sql.Date
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

/**
 * Demonstrates configuration of DB Tester via application.properties.
 *
 * <p>This test demonstrates how to configure DB Tester using Spring Boot's property binding instead
 * of the programmatic Configuration API. The configuration is defined in {@code
 * application-custom-config.properties}.
 *
 * <h2>Configuration via application.properties</h2>
 *
 * <p>The following properties are configured in {@code application-custom-config.properties}:
 *
 * <pre>{@code
 * # Convention settings
 * db-tester.convention.expectation-suffix=/verify
 * db-tester.convention.scenario-marker=[TestCase]
 * db-tester.convention.data-format=CSV
 * db-tester.convention.table-merge-strategy=UNION_ALL
 *
 * # Operation settings
 * db-tester.operation.preparation=CLEAN_INSERT
 * db-tester.operation.expectation=NONE
 * }</pre>
 *
 * <h2>Key Features Demonstrated</h2>
 *
 * <ul>
 *   <li>Custom expectation suffix: {@code /verify} instead of default {@code /expected}
 *   <li>Custom scenario marker: {@code [TestCase]} instead of default {@code [Scenario]}
 *   <li>Configuration via properties without programmatic API
 *   <li>Spring profile-based configuration switching
 * </ul>
 *
 * <p>CSV files are located at:
 *
 * <ul>
 *   <li>{@code src/test/resources/example/PropertiesConfigurationSpec/CONFIG_ITEMS.csv}
 *   <li>{@code src/test/resources/example/PropertiesConfigurationSpec/verify/CONFIG_ITEMS.csv}
 * </ul>
 */
@SpringBootTest(classes = ExampleApplication)
@SpringBootDatabaseTest
@ActiveProfiles("custom-config")
class PropertiesConfigurationSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesConfigurationSpec)

	@Autowired
	JdbcTemplate jdbcTemplate

	@Autowired
	Configuration configuration

	/**
	 * Verifies that Configuration is properly injected from properties.
	 *
	 * <p>This test validates that the custom properties defined in {@code
	 * application-custom-config.properties} are correctly bound to the Configuration bean.
	 */
	def "should inject Configuration from properties"() {
		expect:
		logger.info("Verifying Configuration injection from properties")

		configuration != null
		configuration.conventions() != null
		configuration.operations() != null

		// Verify convention settings
		configuration.conventions().expectationSuffix() == "/verify"
		configuration.conventions().scenarioMarker() == "[TestCase]"
		configuration.conventions().dataFormat() == DataFormat.CSV
		configuration.conventions().tableMergeStrategy() == TableMergeStrategy.UNION_ALL

		// Verify operation settings
		configuration.operations().preparation() == Operation.CLEAN_INSERT
		configuration.operations().expectation() == Operation.NONE

		logger.info("Configuration verification completed successfully")
	}

	/**
	 * Demonstrates custom scenario marker usage configured via properties.
	 *
	 * <p>CSV files use {@code [TestCase]} column instead of default {@code [Scenario]} to filter rows
	 * by test method name. This configuration is set via {@code db-tester.convention.scenario-marker}
	 * property.
	 *
	 * <p>Test flow:
	 *
	 * <ul>
	 *   <li>Preparation: Loads rows matching {@code should use custom scenario marker from properties}
	 *   <li>Execution: Inserts a new record
	 *   <li>Expectation: Verifies final state from {@code verify/CONFIG_ITEMS.csv}
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def "should use custom scenario marker from properties"() {
		when:
		logger.info("Testing custom scenario marker [TestCase] configured via properties")

		jdbcTemplate.update(
				"INSERT INTO CONFIG_ITEMS (ID, NAME, STATUS, CREATED_DATE) VALUES (?, ?, ?, ?)",
				2, "Beta Feature", "ENABLED", Date.valueOf("2024-02-01")
				)

		then:
		logger.info("Custom scenario marker test completed")
		noExceptionThrown()
	}

	/**
	 * Demonstrates custom expectation suffix usage configured via properties.
	 *
	 * <p>Expected data is loaded from {@code /verify} directory instead of default {@code /expected}.
	 * This configuration is set via {@code db-tester.convention.expectation-suffix} property.
	 *
	 * <p>Test flow:
	 *
	 * <ul>
	 *   <li>Preparation: Loads initial data
	 *   <li>Execution: Updates record status
	 *   <li>Expectation: Verifies from {@code verify/CONFIG_ITEMS.csv} instead of {@code expected/}
	 * </ul>
	 */
	@DataSet
	@ExpectedDataSet
	def "should use custom expectation suffix from properties"() {
		when:
		logger.info("Testing custom expectation suffix /verify configured via properties")

		jdbcTemplate.update("UPDATE CONFIG_ITEMS SET STATUS = ? WHERE ID = ?", "DISABLED", 1)

		then:
		logger.info("Custom expectation suffix test completed")
		noExceptionThrown()
	}
}
