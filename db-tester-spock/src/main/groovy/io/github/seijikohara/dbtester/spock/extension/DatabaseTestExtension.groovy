package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.api.annotation.ExportDataSet
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Annotation-driven Spock extension for database testing.
 *
 * <p>This extension is activated by the {@link DatabaseTest @DatabaseTest} annotation on a
 * specification class. It detects {@link DataSet @DataSet},
 * {@link ExpectedDataSet @ExpectedDataSet}, and {@link ExportDataSet @ExportDataSet} annotations
 * on feature methods and adds the appropriate interceptors to handle database setup, verification,
 * and export.
 *
 * @see DatabaseTest
 * @see DataSet
 * @see ExpectedDataSet
 * @see ExportDataSet
 * @see DatabaseTestInterceptor
 */
class DatabaseTestExtension implements IAnnotationDrivenExtension<DatabaseTest> {

	/**
	 * Visits the specification and registers interceptors for annotated feature methods.
	 *
	 * <p>This method scans all feature methods in the specification for {@link DataSet} and
	 * {@link ExpectedDataSet} annotations. For each feature method with at least one of these
	 * annotations, it registers a {@link DatabaseTestInterceptor} to handle database operations.
	 *
	 * @param annotation the {@link DatabaseTest} annotation on the specification class
	 * @param spec the specification info containing metadata about the test class
	 */
	@Override
	void visitSpecAnnotation(DatabaseTest annotation, SpecInfo spec) {
		def specClass = spec.reflection
		def classDataSet = specClass.getAnnotation(DataSet)
		def classExpectedDataSet = specClass.getAnnotation(ExpectedDataSet)
		def classExportDataSet = specClass.getAnnotation(ExportDataSet)

		spec.allFeatures
				.collect { feature ->
					def method = feature.featureMethod.reflection
					[
						feature        : feature,
						dataSet        : method.getAnnotation(DataSet) ?: classDataSet,
						expectedDataSet: method.getAnnotation(ExpectedDataSet) ?: classExpectedDataSet,
						exportDataSet  : method.getAnnotation(ExportDataSet) ?: classExportDataSet
					]
				}
				.findAll { it.dataSet || it.expectedDataSet || it.exportDataSet }
				.each {
					it.feature.addInterceptor(new DatabaseTestInterceptor(
							it.dataSet as DataSet,
							it.expectedDataSet as ExpectedDataSet,
							it.exportDataSet as ExportDataSet
							))
				}
	}
}
