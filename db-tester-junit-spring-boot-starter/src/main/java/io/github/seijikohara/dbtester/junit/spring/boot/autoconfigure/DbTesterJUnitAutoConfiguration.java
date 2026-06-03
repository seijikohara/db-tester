package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrar;
import io.github.seijikohara.dbtester.spring.support.DbTesterConfigurationFactory;
import io.github.seijikohara.dbtester.spring.support.DbTesterProperties;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for DB Tester Spring Boot integration with JUnit 6.
 *
 * <p>This auto-configuration is activated when:
 *
 * <ul>
 *   <li>{@link DataSourceRegistry} is on the classpath
 *   <li>{@code db-tester.enabled} property is true (default)
 * </ul>
 *
 * <p>The configuration provides a {@link DataSourceRegistrar} bean that automatically registers
 * Spring-managed {@link DataSource} beans with the {@link DataSourceRegistry} used by the database
 * testing framework.
 *
 * @see DataSourceRegistry
 * @see DatabaseTestExtension
 * @see DataSourceRegistrar
 */
@AutoConfiguration(
    afterName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(
    prefix = "db-tester",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(DbTesterProperties.class)
public class DbTesterJUnitAutoConfiguration {

  /**
   * Creates a new auto-configuration instance.
   *
   * <p>This constructor is called by Spring Boot's auto-configuration mechanism.
   */
  public DbTesterJUnitAutoConfiguration() {
    // Default constructor for Spring auto-configuration
  }

  /**
   * Creates a Configuration bean from the DB Tester properties.
   *
   * <p>If no custom configuration is provided, this method builds a Configuration from the
   * properties defined in {@code application.properties} or {@code application.yml}.
   *
   * @param properties the DB Tester properties
   * @return the configuration built from properties
   */
  @Bean
  @ConditionalOnMissingBean
  public Configuration dbTesterConfiguration(final DbTesterProperties properties) {
    return DbTesterConfigurationFactory.toConfiguration(properties);
  }

  /**
   * Creates a {@link DataSourceRegistrar} bean.
   *
   * <p>The registrar is responsible for registering Spring-managed {@link DataSource} beans with
   * the {@link DataSourceRegistry}. It provides a bridge between the Spring application context and
   * the database testing framework.
   *
   * @param properties the database tester configuration properties
   * @return a new DataSourceRegistrar instance
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSourceRegistrar dataSourceRegistrar(final DbTesterProperties properties) {
    return new DataSourceRegistrar(properties);
  }
}
