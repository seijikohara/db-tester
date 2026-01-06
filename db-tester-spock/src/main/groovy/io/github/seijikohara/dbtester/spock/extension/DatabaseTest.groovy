package io.github.seijikohara.dbtester.spock.extension

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.spockframework.runtime.extension.ExtensionAnnotation

/**
 * Enables database testing support for a Spock specification.
 *
 * <p>Apply this annotation to a specification class to enable automatic processing of
 * {@link io.github.seijikohara.dbtester.api.annotation.DataSet @DataSet} and
 * {@link io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet @ExpectedDataSet} annotations
 * on feature methods.
 *
 * <p>Example usage:
 * <pre>{@code
 * @DatabaseTest
 * class UserRepositorySpec extends Specification {
 *
 *     DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()
 *             .register(dataSource)
 *
 *     @DataSet
 *     @ExpectedDataSet
 *     def "can insert and retrieve user"() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * <p>The specification must provide a {@code dbTesterRegistry} property or field containing
 * a {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry DataSourceRegistry}
 * with registered data sources.
 *
 * <p>Optionally, a {@code dbTesterConfiguration} property or field can be provided to
 * customize the {@link io.github.seijikohara.dbtester.api.config.Configuration Configuration}.
 *
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 * @see io.github.seijikohara.dbtester.api.config.DataSourceRegistry
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(DatabaseTestExtension)
@interface DatabaseTest {
}
