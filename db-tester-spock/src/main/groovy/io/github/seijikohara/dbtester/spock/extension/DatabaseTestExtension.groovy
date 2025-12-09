package io.github.seijikohara.dbtester.spock.extension

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import java.lang.annotation.Annotation
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

/**
 * Global Spock extension for database testing.
 *
 * <p>This extension automatically detects {@link Preparation} and {@link Expectation} annotations
 * on specifications and feature methods, adding the appropriate interceptors to handle database
 * setup and verification.
 *
 * <p>This extension is registered via the Spock extension mechanism (META-INF/services) and
 * automatically applies to all specifications in the classpath.
 *
 * <p>For Spring Boot integration, use {@code db-tester-spock-spring-boot-starter} which provides
 * automatic DataSource discovery from the Spring ApplicationContext. When Spring test annotations
 * are detected, this extension skips the spec to avoid conflicts with the Spring Boot extension.
 *
 * @see Preparation
 * @see Expectation
 * @see DatabaseTestInterceptor
 */
class DatabaseTestExtension implements IGlobalExtension {

	/**
	 * Spring test annotation class names to detect (checked via name to avoid Spring dependency).
	 * Includes both direct annotations and meta-annotation markers.
	 */
	private static final List<String> SPRING_TEST_ANNOTATION_NAMES = [
		'org.springframework.test.context.ContextConfiguration',
		'org.springframework.test.context.BootstrapWith'  // Meta-annotation used by @SpringBootTest, @WebMvcTest, etc.
	]

	@Override
	void start() {
		// No initialization needed
	}

	@Override
	void visitSpec(SpecInfo spec) {
		// Skip Spring-managed specs (handled by db-tester-spock-spring-boot-starter)
		if (isSpringSpec(spec)) {
			return
		}

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

	@Override
	void stop() {
		// No cleanup needed
	}

	/**
	 * Checks if the specification is a Spring-managed test by annotation name.
	 *
	 * <p>Uses annotation class names to avoid compile-time Spring dependency.
	 * Checks both direct annotations and meta-annotations (annotations on annotations).
	 */
	private static boolean isSpringSpec(SpecInfo spec) {
		def specClass = spec.reflection
		hasSpringAnnotation(specClass.annotations)
	}

	/**
	 * Recursively checks if any annotation matches Spring test annotations.
	 * This handles meta-annotations like @SpringBootTest which is annotated with @BootstrapWith.
	 */
	private static boolean hasSpringAnnotation(Annotation[] annotations, Set<String> visited = []) {
		annotations.any { annotation ->
			def annotationTypeName = annotation.annotationType().name

			// Avoid infinite recursion for cyclic annotations
			if (visited.contains(annotationTypeName)) {
				return false
			}

			// Check if this annotation is a Spring test annotation
			if (SPRING_TEST_ANNOTATION_NAMES.contains(annotationTypeName)) {
				return true
			}

			// Check meta-annotations (annotations on this annotation)
			def newVisited = visited + annotationTypeName
			hasSpringAnnotation(annotation.annotationType().annotations, newVisited)
		}
	}
}
