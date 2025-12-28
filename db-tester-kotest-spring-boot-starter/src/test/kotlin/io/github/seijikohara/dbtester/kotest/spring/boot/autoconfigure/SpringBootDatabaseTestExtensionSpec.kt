package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.kotest.core.extensions.SpecExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Unit tests for [SpringBootDatabaseTestExtension].
 *
 * This specification verifies the Spring Boot-aware Kotest extension
 * that handles database testing with automatic DataSource registration.
 */
class SpringBootDatabaseTestExtensionSpec : AnnotationSpec() {
    /** The extension under test. */
    private lateinit var extension: SpringBootDatabaseTestExtension

    @BeforeEach
    fun setup(): Unit = run { extension = SpringBootDatabaseTestExtension() }

    @Test
    fun `should create instance`(): Unit =
        SpringBootDatabaseTestExtension().let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should implement SpecExtension interface`(): Unit = run { extension.shouldBeInstanceOf<SpecExtension>() }

    @Test
    fun `should implement TestCaseExtension interface`(): Unit = run { extension.shouldBeInstanceOf<TestCaseExtension>() }

    @Test
    fun `should create multiple independent extensions`(): Unit =
        SpringBootDatabaseTestExtension().let { extension1 ->
            SpringBootDatabaseTestExtension().let { extension2 ->
                (extension1 === extension2) shouldBe false
                extension1.shouldBeInstanceOf<SpecExtension>()
                extension2.shouldBeInstanceOf<SpecExtension>()
                extension1.shouldBeInstanceOf<TestCaseExtension>()
                extension2.shouldBeInstanceOf<TestCaseExtension>()
            }
        }

    @Test
    fun `should implement both SpecExtension and TestCaseExtension`(): Unit =
        extension.let { ext ->
            ext.shouldBeInstanceOf<SpecExtension>()
            ext.shouldBeInstanceOf<TestCaseExtension>()
        }
}
