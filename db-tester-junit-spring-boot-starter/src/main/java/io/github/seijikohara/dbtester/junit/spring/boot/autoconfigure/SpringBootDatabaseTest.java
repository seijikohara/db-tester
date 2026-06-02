package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Enables Spring Boot-integrated database testing for a JUnit Jupiter test class.
 *
 * <p>Apply this annotation to a {@link org.springframework.boot.test.context.SpringBootTest} class
 * to process {@link io.github.seijikohara.dbtester.api.annotation.DataSet @DataSet} and {@link
 * io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet @ExpectedDataSet} annotations, with
 * automatic DataSource discovery from the Spring ApplicationContext.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @SpringBootTest
 * @SpringBootDatabaseTest
 * class UserRepositoryTest {
 *
 *     @Test
 *     @DataSet
 *     @ExpectedDataSet
 *     void shouldVerifyDatabaseState() {
 *         // test implementation
 *     }
 * }
 * }</pre>
 *
 * <p>Unlike {@link
 * io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTest @DatabaseTest}, this
 * annotation discovers and registers DataSources from the Spring context, so no manual {@code
 * DataSourceRegistry} configuration is required.
 *
 * @see SpringBootDatabaseTestExtension
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ExtendWith(SpringBootDatabaseTestExtension.class)
public @interface SpringBootDatabaseTest {}
