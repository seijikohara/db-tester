package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Annotation-driven Spock extension for database testing.
 *
 * <p>This extension is activated by the {@link DatabaseTest @DatabaseTest} annotation on a
 * specification class. It detects {@link DataSet @DataSet} and
 * {@link ExpectedDataSet @ExpectedDataSet} annotations on feature methods and adds the appropriate
 * interceptors to handle database setup and verification.
 *
 * @see DatabaseTest
 * @see DataSet
 * @see ExpectedDataSet
 * @see DatabaseTestInterceptor
 */
class DatabaseTestExtension implements IAnnotationDrivenExtension<DatabaseTest> {

	@Override
	void visitSpecAnnotation(DatabaseTest annotation, SpecInfo spec) {
		def specClass = spec.reflection
		def classDataSet = specClass.getAnnotation(DataSet)
		def classExpectedDataSet = specClass.getAnnotation(ExpectedDataSet)

		spec.allFeatures
				.collect { feature ->
					def method = feature.featureMethod.reflection
					[
						feature        : feature,
						dataSet        : method.getAnnotation(DataSet) ?: classDataSet,
						expectedDataSet: method.getAnnotation(ExpectedDataSet) ?: classExpectedDataSet
					]
				}
				.findAll { it.dataSet || it.expectedDataSet }
				.each {
					it.feature.addInterceptor(new DatabaseTestInterceptor(
							it.dataSet as DataSet,
							it.expectedDataSet as ExpectedDataSet
							))
				}
	}
}
