package io.github.seijikohara.dbtester.kotest.spi

import io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Unit tests for [KotestScenarioNameResolver].
 *
 * This specification verifies the Kotest-specific scenario name resolution
 * which extracts feature names from test method names.
 */
class KotestScenarioNameResolverSpec : AnnotationSpec() {
    /** The resolver under test. */
    private lateinit var resolver: KotestScenarioNameResolver

    @BeforeEach
    fun setup(): Unit = run { resolver = KotestScenarioNameResolver() }

    @Test
    fun `should create instance`(): Unit =
        KotestScenarioNameResolver().let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should implement ScenarioNameResolver interface`(): Unit = run { resolver.shouldBeInstanceOf<ScenarioNameResolver>() }

    @Test
    fun `should return priority 100`(): Unit =
        resolver.priority().let { priority ->
            priority shouldBe 100
        }

    @Test
    fun `should return consistent priority`(): Unit = run { (resolver.priority() == resolver.priority()) shouldBe true }

    @Test
    fun `should return false for canResolve when not AnnotationSpec`(): Unit =
        SampleClass::class.java.getMethod("regularMethod").let { method ->
            resolver.canResolve(method).let { canResolve ->
                canResolve shouldBe false
            }
        }

    @Test
    fun `should resolve method name as scenario name`(): Unit =
        SampleClass::class.java.getMethod("testMethod").let { method ->
            resolver.resolve(method).let { scenarioName ->
                scenarioName.value() shouldBe "testMethod"
            }
        }

    @Test
    fun `should have higher priority than JUnit resolver`(): Unit =
        resolver.priority().let { kotestPriority ->
            ScenarioNameResolver.DEFAULT_PRIORITY.let { junitDefaultPriority ->
                (kotestPriority > junitDefaultPriority) shouldBe true
            }
        }

    @Test
    fun `should return non-null ScenarioName for regular method`(): Unit =
        SampleClass::class.java.getMethod("testMethod").let { method ->
            resolver.resolve(method).let { scenarioName ->
                scenarioName shouldNotBe null
            }
        }

    @Test
    fun `should handle testMethod name`(): Unit =
        SampleClass::class.java.getMethod("testMethod").let { method ->
            resolver.resolve(method).value() shouldBe "testMethod"
        }

    @Test
    fun `should handle regularMethod name`(): Unit =
        SampleClass::class.java.getMethod("regularMethod").let { method ->
            resolver.resolve(method).value() shouldBe "regularMethod"
        }

    @Test
    fun `should handle anotherMethod name`(): Unit =
        SampleClass::class.java.getMethod("anotherMethod").let { method ->
            resolver.resolve(method).value() shouldBe "anotherMethod"
        }

    @Test
    fun `should return true for canResolve when AnnotationSpec subclass`(): Unit =
        SampleAnnotationSpec::class.java.getMethod("testInAnnotationSpec").let { method ->
            resolver.canResolve(method).let { canResolve ->
                canResolve shouldBe true
            }
        }

    /**
     * Sample class for testing regular methods.
     */
    class SampleClass {
        /** Sample test method without annotation. */
        fun testMethod(): Unit = Unit

        /** Sample regular method. */
        fun regularMethod(): Unit = Unit

        /** Another sample method. */
        fun anotherMethod(): Unit = Unit
    }

    /**
     * Sample AnnotationSpec subclass for testing canResolve.
     */
    class SampleAnnotationSpec : AnnotationSpec() {
        /** Test method in AnnotationSpec. */
        fun testInAnnotationSpec(): Unit = Unit
    }
}
