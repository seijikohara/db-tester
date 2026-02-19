package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.AnnotationUtils
import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
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
		def classDataSet = AnnotationUtils.findOnElement(DataSet, specClass)
		def classExpectedDataSet = AnnotationUtils.findOnElement(ExpectedDataSet, specClass)
		def classExportDataSet = AnnotationUtils.findOnElement(ExportDataSet, specClass)

		spec.allFeatures
				.collect { feature ->
					def method = feature.featureMethod.reflection
					[
						feature        : feature,
						dataSet        : AnnotationUtils.findOnElement(DataSet, method) ?: classDataSet,
						expectedDataSet: AnnotationUtils.findOnElement(ExpectedDataSet, method) ?: classExpectedDataSet,
						exportDataSet  : AnnotationUtils.findOnElement(ExportDataSet, method) ?: classExportDataSet
					]
				}
				.findAll { it.dataSet || it.expectedDataSet || it.exportDataSet }
				.each {
					it.feature.addInterceptor(new SpringBootDatabaseTestInterceptor(
							it.dataSet as DataSet,
							it.expectedDataSet as ExpectedDataSet,
							it.exportDataSet as ExportDataSet
							))
				}
	}
}
