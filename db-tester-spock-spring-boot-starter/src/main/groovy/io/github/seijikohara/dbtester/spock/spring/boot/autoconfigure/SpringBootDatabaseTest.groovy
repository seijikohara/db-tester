package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.spockframework.runtime.extension.ExtensionAnnotation

/**
 * Enables Spring Boot-integrated database testing support for a Spock specification.
 *
 * <p>Apply this annotation to a Spring Boot test specification to enable automatic processing of
 * {@link io.github.seijikohara.dbtester.api.annotation.DataSet @DataSet} and
 * {@link io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet @ExpectedDataSet} annotations
 * on feature methods, with automatic DataSource discovery from the Spring ApplicationContext.
 *
 * <p>Example usage:
 * <pre>{@code
 * @SpringBootTest
 * @SpringBootDatabaseTest
 * class UserRepositorySpec extends Specification {
 *
 *     @DataSet
 *     @ExpectedDataSet
 *     def "can insert and retrieve user"() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * <p>Unlike {@link io.github.seijikohara.dbtester.spock.extension.DatabaseTest @DatabaseTest},
 * this annotation automatically discovers and registers DataSources from the Spring context,
 * eliminating the need for manual {@code DataSourceRegistry} configuration.
 *
 * <p>Configuration can be customized via {@code application.properties} or
 * {@code application.yml} using the {@code db-tester.*} prefix.
 *
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 * @see DbTesterSpockAutoConfiguration
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(SpringBootDatabaseTestExtension)
@interface SpringBootDatabaseTest {
}
