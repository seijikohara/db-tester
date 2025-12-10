package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Annotation-driven Spock extension for database testing.
 *
 * <p>This extension is activated by the {@link DatabaseTest @DatabaseTest} annotation on a
 * specification class. It detects {@link Preparation @Preparation} and
 * {@link Expectation @Expectation} annotations on feature methods and adds the appropriate
 * interceptors to handle database setup and verification.
 *
 * @see DatabaseTest
 * @see Preparation
 * @see Expectation
 * @see DatabaseTestInterceptor
 */
class DatabaseTestExtension implements IAnnotationDrivenExtension<DatabaseTest> {

	@Override
	void visitSpecAnnotation(DatabaseTest annotation, SpecInfo spec) {
		def specClass = spec.reflection
		def classPreparation = specClass.getAnnotation(Preparation)
		def classExpectation = specClass.getAnnotation(Expectation)

		spec.allFeatures
				.collect { feature ->
					def method = feature.featureMethod.reflection
					[
						feature    : feature,
						preparation: method.getAnnotation(Preparation) ?: classPreparation,
						expectation: method.getAnnotation(Expectation) ?: classExpectation
					]
				}
				.findAll { it.preparation || it.expectation }
				.each {
					it.feature.addInterceptor(new DatabaseTestInterceptor(
							it.preparation as Preparation,
							it.expectation as Expectation
							))
				}
	}
}
