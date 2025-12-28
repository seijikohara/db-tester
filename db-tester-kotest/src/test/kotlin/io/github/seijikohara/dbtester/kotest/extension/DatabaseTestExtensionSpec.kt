package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Unit tests for [DatabaseTestExtension].
 *
 * This specification verifies the Kotest extension that handles
 * database testing annotations on test methods.
 */
class DatabaseTestExtensionSpec : AnnotationSpec() {
    /** The extension under test. */
    private lateinit var extension: DatabaseTestExtension

    /** Test registry provider. */
    private val registryProvider: () -> DataSourceRegistry = { DataSourceRegistry() }

    @BeforeEach
    fun setup(): Unit = run { extension = DatabaseTestExtension(registryProvider) }

    @Test
    fun `should create instance`(): Unit =
        DatabaseTestExtension(registryProvider).let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should implement TestCaseExtension interface`(): Unit = run { extension.shouldBeInstanceOf<TestCaseExtension>() }

    @Test
    fun `should create instance with custom configuration provider`(): Unit =
        DatabaseTestExtension(
            registryProvider = registryProvider,
            configurationProvider = { Configuration.defaults() },
        ).let { instance ->
            instance shouldNotBe null
            instance.shouldBeInstanceOf<TestCaseExtension>()
        }

    @Test
    fun `should create multiple independent extensions`(): Unit =
        DatabaseTestExtension(registryProvider).let { extension1 ->
            DatabaseTestExtension(registryProvider).let { extension2 ->
                (extension1 === extension2) shouldBe false
                extension1.shouldBeInstanceOf<TestCaseExtension>()
                extension2.shouldBeInstanceOf<TestCaseExtension>()
            }
        }

    @Test
    fun `should accept different registry providers`(): Unit =
        DataSourceRegistry().let { registry1 ->
            DataSourceRegistry().let { registry2 ->
                DatabaseTestExtension(registryProvider = { registry1 }).let { ext1 ->
                    DatabaseTestExtension(registryProvider = { registry2 }).let { ext2 ->
                        ext1 shouldNotBe null
                        ext2 shouldNotBe null
                    }
                }
            }
        }
}
