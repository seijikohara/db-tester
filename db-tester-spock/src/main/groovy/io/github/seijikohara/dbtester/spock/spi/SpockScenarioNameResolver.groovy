package io.github.seijikohara.dbtester.spock.spi

import io.github.seijikohara.dbtester.api.scenario.ScenarioName
import io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
import java.lang.reflect.Method
import org.spockframework.runtime.model.FeatureMetadata

/**
 * Spock Framework implementation of {@link ScenarioNameResolver}.
 *
 * <p>This resolver provides scenario name resolution for Spock Framework tests. In Spock, test
 * methods (features) have descriptive names with spaces that are stored in the {@link
 * FeatureMetadata} annotation, while the actual Java method name is an internal identifier like
 * {@code $spock_feature_0_0}.
 *
 * <p>For Spock tests, this resolver checks if the method has a {@link FeatureMetadata} annotation.
 * If present, returns the {@link FeatureMetadata#name()} value; otherwise falls back to the method
 * name.
 *
 * <p>This resolver uses priority 100, which is higher than the default JUnit resolver (priority 0),
 * ensuring that Spock feature names are used when the {@code @FeatureMetadata} annotation is
 * present.
 *
 * @see ScenarioNameResolver
 * @see FeatureMetadata
 */
final class SpockScenarioNameResolver implements ScenarioNameResolver {

	/** Priority for Spock resolver, higher than default JUnit resolver. */
	private static final int SPOCK_PRIORITY = 100

	/**
	 * Resolves the scenario name from a Spock feature method.
	 *
	 * <p>Extracts the human-readable feature name from the {@link FeatureMetadata} annotation. If the
	 * annotation is not present (which shouldn't happen for Spock features), falls back to the method
	 * name.
	 *
	 * @param testMethod the test method to resolve the scenario name from
	 * @return the scenario name based on the feature metadata or method name
	 */
	@Override
	ScenarioName resolve(Method testMethod) {
		def metadata = testMethod.getAnnotation(FeatureMetadata)
		if (metadata != null) {
			return new ScenarioName(metadata.name())
		}
		// Fallback for non-annotated methods (shouldn't happen for Spock features)
		return new ScenarioName(testMethod.name)
	}

	/**
	 * Determines if this resolver can handle the given test method.
	 *
	 * <p>Returns {@code true} only if the method has a {@link FeatureMetadata} annotation, indicating
	 * it is a Spock feature method.
	 *
	 * @param testMethod the test method to check
	 * @return {@code true} if the method has {@code @FeatureMetadata}, {@code false} otherwise
	 */
	@Override
	boolean canResolve(Method testMethod) {
		return testMethod.isAnnotationPresent(FeatureMetadata)
	}

	/**
	 * Returns the priority of this resolver.
	 *
	 * <p>Returns a priority higher than the default JUnit resolver to ensure Spock-specific
	 * resolution is used for Spock features.
	 *
	 * @return priority value of 100
	 */
	@Override
	int priority() {
		return SPOCK_PRIORITY
	}
}
