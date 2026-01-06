package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Annotation-driven Spock extension for Spring Boot database testing.
 *
 * <p>This extension is activated by the {@link SpringBootDatabaseTest @SpringBootDatabaseTest}
 * annotation on a specification class. It detects {@link DataSet @DataSet} and
 * {@link ExpectedDataSet @ExpectedDataSet} annotations on feature methods and adds Spring-aware
 * interceptors that automatically discover DataSources from the Spring ApplicationContext.
 *
 * @see SpringBootDatabaseTest
 * @see SpringBootDatabaseTestInterceptor
 * @see DataSet
 * @see ExpectedDataSet
 */
class SpringBootDatabaseTestExtension implements IAnnotationDrivenExtension<SpringBootDatabaseTest> {

	@Override
	void visitSpecAnnotation(SpringBootDatabaseTest annotation, SpecInfo spec) {
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
					it.feature.addInterceptor(new SpringBootDatabaseTestInterceptor(
							it.dataSet as DataSet,
							it.expectedDataSet as ExpectedDataSet
							))
				}
	}
}
