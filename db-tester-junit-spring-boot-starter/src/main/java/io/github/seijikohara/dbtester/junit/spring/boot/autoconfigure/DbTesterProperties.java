package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for DB Tester Spring Boot integration.
 *
 * <p>These properties control how DataSource beans from the Spring application context are
 * registered with the {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry}.
 *
 * <p>Properties are prefixed with {@code db-tester}.
 */
@ConfigurationProperties(prefix = "db-tester")
public class DbTesterProperties {

  /** Creates a new instance with default property values. */
  public DbTesterProperties() {
    // Default constructor for Spring Boot configuration binding
  }

  /** Whether DB Tester is enabled. Defaults to true. */
  private boolean enabled = true;

  /** Whether to automatically register DataSource beans. Defaults to true. */
  private boolean autoRegisterDataSources = true;

  /**
   * Returns whether DB Tester is enabled.
   *
   * @return true if enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether DB Tester is enabled.
   *
   * @param enabled true to enable
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns whether to automatically register DataSource beans with the DataSourceRegistry.
   *
   * @return true if auto-registration is enabled
   */
  public boolean isAutoRegisterDataSources() {
    return autoRegisterDataSources;
  }

  /**
   * Sets whether to automatically register DataSource beans with the DataSourceRegistry.
   *
   * @param autoRegisterDataSources true to enable auto-registration
   */
  public void setAutoRegisterDataSources(final boolean autoRegisterDataSources) {
    this.autoRegisterDataSources = autoRegisterDataSources;
  }
}
