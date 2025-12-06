package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo
import org.springframework.context.ApplicationContext
import org.springframework.test.context.TestContextManager

/**
 * Spring Boot-aware Spock extension for database testing.
 *
 * <p>This global extension automatically integrates the database testing framework with Spring Boot.
 * It intercepts test execution to:
 *
 * <ul>
 *   <li>Retrieve the {@link DataSourceRegistry} from the Spring {@link ApplicationContext}
 *   <li>Execute database preparation before tests annotated with {@link Preparation}
 *   <li>Verify database expectations after tests annotated with {@link Expectation}
 * </ul>
 *
 * <p>Unlike the standard {@code db-tester-spock} extension which requires manual DataSource
 * registration, this Spring Boot extension automatically discovers and registers DataSources
 * from the Spring context using Spring's {@link TestContextManager} to ensure the
 * ApplicationContext is available before attempting to retrieve beans.
 *
 * <p>This extension is automatically registered via Spock's service loader mechanism
 * (META-INF/services/org.spockframework.runtime.extension.IGlobalExtension).
 *
 * @see DataSourceRegistrar
 * @see DbTesterSpockAutoConfiguration
 */
class SpringBootDatabaseTestExtension implements IGlobalExtension {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootDatabaseTestExtension)

	/**
	 * Performs initialization when the extension starts.
	 *
	 * <p>Logs a debug message indicating the extension has started.
	 */
	@Override
	void start() {
		logger.debug('SpringBootDatabaseTestExtension started')
	}

	/**
	 * Visits a specification and registers database test interceptors for annotated features.
	 *
	 * <p>This method checks for class-level and method-level {@link Preparation} and
	 * {@link Expectation} annotations, adding {@link SpringBootDatabaseTestInterceptor}
	 * instances to features that require database setup or verification.
	 *
	 * <p>Non-Spring specifications (those without Spring test annotations) are skipped.
	 *
	 * @param spec the specification info to visit (must not be null)
	 */
	@Override
	void visitSpec(SpecInfo spec) {
		// Check if the spec uses Spring (has @SpringBootTest or similar)
		if (!isSpringSpec(spec)) {
			return
		}

		// Check for class-level annotations
		def classPreparation = spec.reflection.getAnnotation(Preparation)
		def classExpectation = spec.reflection.getAnnotation(Expectation)

		// Visit each feature method
		spec.allFeatures.each { feature ->
			visitFeature(spec, feature, classPreparation, classExpectation)
		}
	}

	/**
	 * Performs cleanup when the extension stops.
	 *
	 * <p>Logs a debug message indicating the extension has stopped.
	 */
	@Override
	void stop() {
		logger.debug('SpringBootDatabaseTestExtension stopped')
	}

	/**
	 * Checks if the specification is a Spring-managed test.
	 *
	 * @param spec the specification info (must not be null)
	 * @return {@code true} if the spec uses Spring test annotations, {@code false} otherwise
	 */
	private boolean isSpringSpec(SpecInfo spec) {
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
	 * Visits a feature method and adds interceptors for database testing annotations.
	 *
	 * @param spec the specification info (must not be null)
	 * @param feature the feature method info (must not be null)
	 * @param classPreparation the class-level preparation annotation (may be null)
	 * @param classExpectation the class-level expectation annotation (may be null)
	 */
	private void visitFeature(SpecInfo spec, FeatureInfo feature, Preparation classPreparation, Expectation classExpectation) {
		// Method-level annotations take precedence
		def methodPreparation = feature.featureMethod.reflection.getAnnotation(Preparation)
		def methodExpectation = feature.featureMethod.reflection.getAnnotation(Expectation)

		// Use method-level annotation if present, otherwise use class-level
		def effectivePreparation = methodPreparation ?: classPreparation
		def effectiveExpectation = methodExpectation ?: classExpectation

		// Add interceptors if annotations are present
		if (effectivePreparation != null || effectiveExpectation != null) {
			feature.addInterceptor(new SpringBootDatabaseTestInterceptor(effectivePreparation, effectiveExpectation))
		}
	}
}
