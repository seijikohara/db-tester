package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Annotation-driven Spock extension for Spring Boot database testing.
 *
 * <p>This extension is activated by the {@link SpringBootDatabaseTest @SpringBootDatabaseTest}
 * annotation on a specification class. It detects {@link Preparation @Preparation} and
 * {@link Expectation @Expectation} annotations on feature methods and adds Spring-aware
 * interceptors that automatically discover DataSources from the Spring ApplicationContext.
 *
 * @see SpringBootDatabaseTest
 * @see SpringBootDatabaseTestInterceptor
 * @see Preparation
 * @see Expectation
 */
class SpringBootDatabaseTestExtension implements IAnnotationDrivenExtension<SpringBootDatabaseTest> {

	@Override
	void visitSpecAnnotation(SpringBootDatabaseTest annotation, SpecInfo spec) {
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
					it.feature.addInterceptor(new SpringBootDatabaseTestInterceptor(
							it.preparation as Preparation,
							it.expectation as Expectation
							))
				}
	}
}
