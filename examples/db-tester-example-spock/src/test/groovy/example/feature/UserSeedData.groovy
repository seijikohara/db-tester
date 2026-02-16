package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Composed annotation that wraps {@link DataSet} to load user seed data.
 *
 * <p>Demonstrates a meta-annotation that encapsulates a specific {@code @DataSet} resource
 * location, reducing repetition across tests that share the same preparation data.
 */
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
resourceLocation = 'classpath:example/feature/ComposedAnnotationSpec/user-seed/'))
@interface UserSeedData {}
