package io.github.seijikohara.dbtester.junit.jupiter.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Enables database testing support for a JUnit Jupiter test class.
 *
 * <p>This is a composed annotation that combines {@link ExtendWith
 * ExtendWith(DatabaseTestExtension.class)}, reducing boilerplate for test authors.
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * @DatabaseTest
 * class UserRepositoryTest {
 *
 *     @BeforeAll
 *     static void setUp(ExtensionContext context) {
 *         DataSource dataSource = createDataSource();
 *         DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
 *     }
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
 * <p>This annotation is {@link Inherited}, so subclasses of annotated test classes automatically
 * inherit the database testing extension.
 *
 * @see DatabaseTestExtension
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@ExtendWith(DatabaseTestExtension.class)
public @interface DatabaseTest {}
