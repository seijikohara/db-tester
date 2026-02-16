package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Composed annotation that wraps {@link ExpectedDataSet} to exclude audit columns.
 *
 * <p>Demonstrates a meta-annotation that encapsulates a specific {@code @ExpectedDataSet}
 * configuration with column exclusions, allowing tests to verify data without matching audit
 * timestamps.
 */
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@ExpectedDataSet(sources = @DataSetSource(
excludeColumns = ['CREATED_AT', 'UPDATED_AT'],
resourceLocation = 'classpath:example/feature/ComposedAnnotationSpec/verify-users/expected/'))
@interface VerifyIgnoringAuditColumns {}
