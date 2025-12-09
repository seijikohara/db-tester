package io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.spock.extension.DatabaseTestInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.test.context.BootstrapWith
import org.springframework.test.context.ContextConfiguration

/**
 * Spring Boot-aware Spock extension for database testing.
 *
 * <p>This global extension automatically integrates the database testing framework with Spring Boot.
 * It intercepts test execution to:
 *
 * <ul>
 *   <li>Retrieve the DataSourceRegistry from the Spring ApplicationContext
 *   <li>Execute database preparation before tests annotated with {@link Preparation}
 *   <li>Verify database expectations after tests annotated with {@link Expectation}
 * </ul>
 *
 * <p>Unlike the standard {@code db-tester-spock} extension which requires manual DataSource
 * registration, this Spring Boot extension automatically discovers and registers DataSources
 * from the Spring context.
 *
 * <p>This extension handles both Spring-managed specifications and non-Spring specifications.
 * Spring specs are detected using Spring's {@link AnnotatedElementUtils} which properly handles
 * meta-annotations like {@code @SpringBootTest} (meta-annotated with {@code @BootstrapWith}).
 * For non-Spring specs, it delegates to the base {@link DatabaseTestInterceptor}.
 *
 * <p>This extension is automatically registered via Spock's service loader mechanism
 * (META-INF/services/org.spockframework.runtime.extension.IGlobalExtension).
 * When this starter is used, the base {@code db-tester-spock} extension's service file
 * should not be registered to avoid duplicate processing.
 *
 * @see DataSourceRegistrar
 * @see DbTesterSpockAutoConfiguration
 */
class SpringBootDatabaseTestExtension implements IGlobalExtension {

	private static final Logger logger = LoggerFactory.getLogger(SpringBootDatabaseTestExtension)

	@Override
	void start() {
		logger.debug('SpringBootDatabaseTestExtension started')
	}

	@Override
	void visitSpec(SpecInfo spec) {
		def specClass = spec.reflection
		def classPreparation = specClass.getAnnotation(Preparation)
		def classExpectation = specClass.getAnnotation(Expectation)

		def isSpring = isSpringSpec(specClass)

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
					def interceptor = isSpring
							? new SpringBootDatabaseTestInterceptor(it.preparation as Preparation, it.expectation as Expectation)
							: new DatabaseTestInterceptor(it.preparation as Preparation, it.expectation as Expectation)
					it.feature.addInterceptor(interceptor)
				}
	}

	@Override
	void stop() {
		logger.debug('SpringBootDatabaseTestExtension stopped')
	}

	/**
	 * Checks if the specification is a Spring-managed test using Spring's annotation utilities.
	 *
	 * <p>This method uses {@link AnnotatedElementUtils} which properly handles:
	 * <ul>
	 *   <li>Direct annotations like {@code @ContextConfiguration}
	 *   <li>Meta-annotations like {@code @SpringBootTest} (which is meta-annotated with {@code @BootstrapWith})
	 *   <li>Composed annotations and annotation hierarchies
	 * </ul>
	 *
	 * @param specClass the specification class to check
	 * @return true if the class has Spring test annotations
	 */
	private static boolean isSpringSpec(Class<?> specClass) {
		// Check for @ContextConfiguration (direct or as meta-annotation)
		AnnotatedElementUtils.hasAnnotation(specClass, ContextConfiguration) ||
				// Check for @BootstrapWith (used by @SpringBootTest, @WebMvcTest, etc.)
				AnnotatedElementUtils.hasAnnotation(specClass, BootstrapWith)
	}
}
