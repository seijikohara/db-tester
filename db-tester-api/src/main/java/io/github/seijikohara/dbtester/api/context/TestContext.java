package io.github.seijikohara.dbtester.api.context;

import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import java.lang.reflect.Method;

/**
 * Immutable snapshot of the information required to execute a single test phase.
 *
 * <p>This record captures the essential context of a running test, independent of any specific
 * testing framework. Framework-specific extensions (JUnit Jupiter, Spock, etc.) are responsible for
 * creating instances of this record from their respective context objects.
 *
 * @param testClass class that owns the running test
 * @param testMethod concrete test method
 * @param configuration active framework configuration
 * @param registry registry providing registered data sources
 */
public record TestContext(
    Class<?> testClass,
    Method testMethod,
    Configuration configuration,
    DataSourceRegistry registry) {}
