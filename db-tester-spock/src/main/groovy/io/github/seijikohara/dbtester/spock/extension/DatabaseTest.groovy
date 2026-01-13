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
 * <p><b>Important:</b> The specification class must implement {@link DatabaseTestSupport} to provide
 * the {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry DataSourceRegistry}.
 *
 * <p><b>Usage:</b>
 * <pre>{@code
 * @DatabaseTest
 * class MySpec extends Specification implements DatabaseTestSupport {
 *
 *     DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()
 *
 *     def setupSpec() {
 *         dbTesterRegistry.registerDefault(createDataSource())
 *     }
 *
 *     @DataSet
 *     @ExpectedDataSet
 *     def "should verify database state"() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * <p><b>With custom configuration:</b>
 * <pre>{@code
 * @DatabaseTest
 * class MySpec extends Specification implements DatabaseTestSupport {
 *
 *     DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()
 *
 *     Configuration dbTesterConfiguration = Configuration.builder()
 *         .conventions(ConventionSettings.builder()
 *             .dataFormat(DataFormat.TSV)
 *             .build())
 *         .build()
 *
 *     @DataSet
 *     def "should load TSV data"() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * @see DatabaseTestExtension
 * @see DatabaseTestSupport
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtensionAnnotation(DatabaseTestExtension)
@interface DatabaseTest {
}
