package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Global Spock extension for database testing.
 *
 * <p>This extension automatically detects {@link Preparation} and {@link Expectation} annotations
 * from the db-tester API module on specifications and feature methods, adding the appropriate
 * interceptors to handle database setup and verification.
 *
 * <p>This extension is registered via the Spock extension mechanism (META-INF/services) and
 * automatically applies to all specifications in the classpath.
 *
 * @see Preparation
 * @see Expectation
 */
class DatabaseTestExtension implements IGlobalExtension {

	/**
	 * Performs initialization when the extension starts.
	 *
	 * <p>This implementation performs no initialization as none is required.
	 */
	@Override
	void start() {
		// No initialization needed
	}

	/**
	 * Visits a specification and registers database test interceptors for annotated features.
	 *
	 * <p>This method checks for class-level and method-level {@link Preparation} and
	 * {@link Expectation} annotations, adding {@link DatabaseTestInterceptor} instances
	 * to features that require database setup or verification.
	 *
	 * <p>Spring Boot tests are skipped as they are handled by
	 * {@code SpringBootDatabaseTestExtension} from the {@code db-tester-spock-spring-boot-starter}
	 * module.
	 *
	 * @param spec the specification info to visit (must not be null)
	 */
	@Override
	void visitSpec(SpecInfo spec) {
		// Skip Spring Boot tests - they are handled by SpringBootDatabaseTestExtension
		// from db-tester-spock-spring-boot-starter
		if (isSpringBootTest(spec)) {
			return
		}

		// Check for class-level annotations
		def classPreparation = spec.reflection.getAnnotation(Preparation)
		def classExpectation = spec.reflection.getAnnotation(Expectation)

		// Visit each feature method
		spec.allFeatures.each { feature ->
			visitFeature(feature, classPreparation, classExpectation)
		}
	}

	/**
	 * Checks if the specification is a Spring Boot test.
	 *
	 * <p>Spring Boot tests should be handled by {@code SpringBootDatabaseTestExtension}
	 * from the {@code db-tester-spock-spring-boot-starter} module.
	 *
	 * @param spec the specification info (must not be null)
	 * @return {@code true} if this is a Spring Boot test, {@code false} otherwise
	 */
	private boolean isSpringBootTest(SpecInfo spec) {
		def specClass = spec.reflection

		// Check for common Spring test annotations
		return specClass.annotations.any { annotation ->
			def annotationType = annotation.annotationType()
			annotationType.name.contains('SpringBootTest') ||
					annotationType.name.contains('ContextConfiguration') ||
					annotationType.name.contains('SpringJUnitConfig')
		}
	}

	/**
	 * Performs cleanup when the extension stops.
	 *
	 * <p>This implementation performs no cleanup as none is required.
	 */
	@Override
	void stop() {
		// No cleanup needed
	}

	/**
	 * Visits a feature method and adds interceptors for any database testing annotations.
	 *
	 * @param feature the feature method info
	 * @param classPreparation class-level preparation annotation (may be null)
	 * @param classExpectation class-level expectation annotation (may be null)
	 */
	private void visitFeature(FeatureInfo feature, Preparation classPreparation, Expectation classExpectation) {
		// Method-level annotations take precedence
		def methodPreparation = feature.featureMethod.reflection.getAnnotation(Preparation)
		def methodExpectation = feature.featureMethod.reflection.getAnnotation(Expectation)

		// Use method-level annotation if present, otherwise use class-level
		def effectivePreparation = methodPreparation ?: classPreparation
		def effectiveExpectation = methodExpectation ?: classExpectation

		// Add interceptors if annotations are present
		if (effectivePreparation != null || effectiveExpectation != null) {
			feature.addInterceptor(new DatabaseTestInterceptor(effectivePreparation, effectiveExpectation))
		}
	}
}
