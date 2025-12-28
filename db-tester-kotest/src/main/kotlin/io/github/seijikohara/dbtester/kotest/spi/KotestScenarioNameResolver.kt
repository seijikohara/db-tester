package io.github.seijikohara.dbtester.kotest.spi

import io.github.seijikohara.dbtester.api.scenario.ScenarioName
import io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
import io.kotest.core.spec.style.AnnotationSpec
import java.lang.reflect.Method

/**
 * Kotest Framework implementation of [ScenarioNameResolver].
 *
 * This resolver provides scenario name resolution for Kotest Framework tests.
 * In Kotest AnnotationSpec, test methods can use backtick-escaped names
 * (e.g., `should load and verify product data`) which are preserved as-is
 * in the method name at the JVM level.
 *
 * For Kotest tests using AnnotationSpec, this resolver checks if the method
 * is annotated with `@Test` from AnnotationSpec and is declared in a class
 * that extends AnnotationSpec.
 *
 * This resolver uses priority 100, which is higher than the default JUnit resolver
 * (priority 0), ensuring that Kotest-specific resolution is used when applicable.
 *
 * @see ScenarioNameResolver
 * @see AnnotationSpec
 */
class KotestScenarioNameResolver : ScenarioNameResolver {
    /** Companion object containing class-level constants. */
    companion object {
        /** Priority for Kotest resolver, higher than default JUnit resolver. */
        private const val KOTEST_PRIORITY = 100
    }

    /**
     * Resolves the scenario name from a Kotest test method.
     *
     * Returns the method name directly, which for Kotest backtick methods
     * will be the human-readable name (e.g., "should load and verify product data").
     *
     * @param testMethod the test method to resolve the scenario name from
     * @return the scenario name based on the method name
     */
    override fun resolve(testMethod: Method): ScenarioName = ScenarioName(testMethod.name)

    /**
     * Determines if this resolver can handle the given test method.
     *
     * Returns `true` if the method is declared in a class that extends [AnnotationSpec]
     * or if the method has the Kotest `@Test` annotation.
     *
     * @param testMethod the test method to check
     * @return `true` if this resolver can handle the method
     */
    override fun canResolve(testMethod: Method): Boolean =
        when {
            AnnotationSpec::class.java.isAssignableFrom(testMethod.declaringClass) -> true
            else ->
                testMethod.annotations.any { annotation ->
                    annotation.annotationClass.qualifiedName == "io.kotest.core.spec.style.AnnotationSpec\$Test"
                }
        }

    /**
     * Returns the priority of this resolver.
     *
     * Returns a priority higher than the default JUnit resolver to ensure
     * Kotest-specific resolution is used for Kotest tests.
     *
     * @return priority value of 100
     */
    override fun priority(): Int = KOTEST_PRIORITY
}
