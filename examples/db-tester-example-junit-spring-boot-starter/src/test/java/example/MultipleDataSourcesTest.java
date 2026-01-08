package example;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure.SpringBootDatabaseTestExtension;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Integration test demonstrating multiple DataSource support.
 *
 * <p>This test verifies that:
 *
 * <ul>
 *   <li>Multiple DataSources are automatically registered by bean name
 *   <li>The {@code @Primary} DataSource is registered as the default
 *   <li>Named DataSources can be accessed via their bean names in test annotations
 * </ul>
 */
@SpringBootTest(
    classes = {ExampleApplication.class, MultipleDataSourcesTest.MultiDataSourceConfig.class})
@ExtendWith(SpringBootDatabaseTestExtension.class)
@DisplayName("MultipleDataSourcesTest")
class MultipleDataSourcesTest {

  /** Logger for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(MultipleDataSourcesTest.class);

  /** Primary DataSource (mainDb). */
  private final DataSource mainDataSource;

  /** Secondary DataSource (archiveDb). */
  private final DataSource archiveDataSource;

  /**
   * Creates a new test instance with the required DataSources.
   *
   * @param mainDataSource the primary DataSource
   * @param archiveDataSource the secondary DataSource
   */
  @Autowired
  MultipleDataSourcesTest(
      @Qualifier("mainDb") final DataSource mainDataSource,
      @Qualifier("archiveDb") final DataSource archiveDataSource) {
    this.mainDataSource = mainDataSource;
    this.archiveDataSource = archiveDataSource;
  }

  /**
   * Verifies that both DataSources are registered and accessible.
   *
   * @param context the extension context for accessing the registry
   */
  @Test
  @Tag("normal")
  @DisplayName("should register multiple DataSources")
  void shouldRegisterMultipleDataSources(
      final org.junit.jupiter.api.extension.ExtensionContext context) {
    // Given
    final DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);

    // When & Then
    assertAll(
        "Multiple DataSource registration",
        () -> assertNotNull(registry.get("mainDb"), "mainDb should be registered"),
        () -> assertNotNull(registry.get("archiveDb"), "archiveDb should be registered"),
        () -> assertNotNull(registry.getDefault(), "Default DataSource should be registered"),
        () ->
            assertEquals(
                registry.get("mainDb"),
                registry.getDefault(),
                "Default should be the @Primary DataSource"));

    logger.info("Successfully verified multiple DataSource registration");
  }

  /**
   * Verifies that @DataSet works with the default (primary) DataSource.
   *
   * <p>CSV files located at: {@code src/test/resources/example/MultipleDataSourcesTest/USERS.csv}
   */
  @Test
  @Tag("normal")
  @DisplayName("should prepare default DataSource with DataSet")
  @DataSet
  void shouldPrepareDefaultDataSource() {
    logger.info("Testing preparation with default DataSource");

    // When
    final var jdbcClient = JdbcClient.create(mainDataSource);
    final var count = jdbcClient.sql("SELECT COUNT(*) FROM USERS").query(Long.class).single();

    // Then
    assertEquals(2L, count, "Default DataSource should have 2 users from preparation");
    logger.info("Default DataSource has {} users", count);
  }

  /**
   * Verifies that named DataSources are accessible via JDBC.
   *
   * <p>This test demonstrates that the archiveDb DataSource is properly registered and accessible,
   * allowing direct database operations on the secondary database.
   */
  @Test
  @Tag("normal")
  @DisplayName("should access named DataSource directly")
  void shouldAccessNamedDataSource() {
    logger.info("Testing direct access to named DataSource (archiveDb)");

    // Given
    final var jdbcClient = JdbcClient.create(archiveDataSource);

    // When
    jdbcClient
        .sql(
            "INSERT INTO ARCHIVED_USERS (ID, NAME, EMAIL, ARCHIVED_AT) VALUES (1, 'Test', 'test@example.com', CURRENT_TIMESTAMP)")
        .update();

    // Then
    final var count =
        jdbcClient.sql("SELECT COUNT(*) FROM ARCHIVED_USERS").query(Long.class).single();

    assertEquals(1L, count, "archiveDb should have 1 archived user");
    logger.info("archiveDb has {} archived users", count);
  }

  /** Test configuration that defines multiple DataSources. */
  @TestConfiguration
  static class MultiDataSourceConfig {

    /** Creates a new configuration instance. */
    MultiDataSourceConfig() {
      // Default constructor
    }

    /**
     * Creates the primary DataSource (mainDb).
     *
     * @return the main DataSource
     */
    @Bean
    @Primary
    DataSource mainDb() {
      final var dataSource =
          DataSourceBuilder.create()
              .url("jdbc:h2:mem:maindb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
              .driverClassName("org.h2.Driver")
              .username("sa")
              .password("")
              .build();

      // Initialize schema
      JdbcClient.create(dataSource)
          .sql(
              """
              CREATE TABLE IF NOT EXISTS USERS (
                ID BIGINT PRIMARY KEY,
                NAME VARCHAR(255) NOT NULL,
                EMAIL VARCHAR(255) NOT NULL
              )
              """)
          .update();

      return dataSource;
    }

    /**
     * Creates the secondary DataSource (archiveDb).
     *
     * @return the archive DataSource
     */
    @Bean
    DataSource archiveDb() {
      final var dataSource =
          DataSourceBuilder.create()
              .url("jdbc:h2:mem:archivedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
              .driverClassName("org.h2.Driver")
              .username("sa")
              .password("")
              .build();

      // Initialize schema
      JdbcClient.create(dataSource)
          .sql(
              """
              CREATE TABLE IF NOT EXISTS ARCHIVED_USERS (
                ID BIGINT PRIMARY KEY,
                NAME VARCHAR(255) NOT NULL,
                EMAIL VARCHAR(255) NOT NULL,
                ARCHIVED_AT TIMESTAMP NOT NULL
              )
              """)
          .update();

      return dataSource;
    }
  }
}
