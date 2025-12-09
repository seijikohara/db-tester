package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure.SpringBootDatabaseTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

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
 *   <li>{@code src/test/resources/example/PropertiesConfigurationTest/CONFIG_ITEMS.csv}
 *   <li>{@code src/test/resources/example/PropertiesConfigurationTest/verify/CONFIG_ITEMS.csv}
 * </ul>
 */
@SpringBootTest(classes = ExampleApplication.class)
@ExtendWith(SpringBootDatabaseTestExtension.class)
@ActiveProfiles("custom-config")
public class PropertiesConfigurationTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(PropertiesConfigurationTest.class);

  /** JdbcTemplate for executing SQL operations. */
  private final JdbcTemplate jdbcTemplate;

  /** Configuration injected from Spring context. */
  private final Configuration configuration;

  /**
   * Creates a new test instance with the required dependencies.
   *
   * @param jdbcTemplate the JDBC template for database operations
   * @param configuration the DB Tester configuration
   */
  @Autowired
  public PropertiesConfigurationTest(
      final JdbcTemplate jdbcTemplate, final Configuration configuration) {
    this.jdbcTemplate = jdbcTemplate;
    this.configuration = configuration;
  }

  /**
   * Verifies that Configuration is properly injected from properties.
   *
   * <p>This test validates that the custom properties defined in {@code
   * application-custom-config.properties} are correctly bound to the Configuration bean.
   */
  @Test
  void shouldInjectConfigurationFromProperties() {
    logger.info("Verifying Configuration injection from properties");

    assertNotNull(configuration, "Configuration should be injected");
    assertNotNull(configuration.conventions(), "Conventions should not be null");
    assertNotNull(configuration.operations(), "Operations should not be null");

    // Verify convention settings
    assertEquals(
        "/verify",
        configuration.conventions().expectationSuffix(),
        "Expectation suffix should be /verify");
    assertEquals(
        "[TestCase]",
        configuration.conventions().scenarioMarker(),
        "Scenario marker should be [TestCase]");
    assertEquals(
        DataFormat.CSV, configuration.conventions().dataFormat(), "Data format should be CSV");
    assertEquals(
        TableMergeStrategy.UNION_ALL,
        configuration.conventions().tableMergeStrategy(),
        "Table merge strategy should be UNION_ALL");

    // Verify operation settings
    assertEquals(
        Operation.CLEAN_INSERT,
        configuration.operations().preparation(),
        "Preparation operation should be CLEAN_INSERT");
    assertEquals(
        Operation.NONE,
        configuration.operations().expectation(),
        "Expectation operation should be NONE");

    logger.info("Configuration verification completed successfully");
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
   *   <li>Preparation: Loads rows matching {@code shouldUseCustomScenarioMarkerFromProperties}
   *   <li>Execution: Inserts a new record
   *   <li>Expectation: Verifies final state from {@code verify/CONFIG_ITEMS.csv}
   * </ul>
   */
  @Test
  @Preparation
  @Expectation
  void shouldUseCustomScenarioMarkerFromProperties() {
    logger.info("Testing custom scenario marker [TestCase] configured via properties");

    jdbcTemplate.update(
        "INSERT INTO CONFIG_ITEMS (ID, NAME, STATUS, CREATED_DATE) VALUES (?, ?, ?, ?)",
        2,
        "Beta Feature",
        "ENABLED",
        java.sql.Date.valueOf("2024-02-01"));

    logger.info("Custom scenario marker test completed");
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
  @Test
  @Preparation
  @Expectation
  void shouldUseCustomExpectationSuffixFromProperties() {
    logger.info("Testing custom expectation suffix /verify configured via properties");

    jdbcTemplate.update("UPDATE CONFIG_ITEMS SET STATUS = ? WHERE ID = ?", "DISABLED", 1);

    logger.info("Custom expectation suffix test completed");
  }
}
