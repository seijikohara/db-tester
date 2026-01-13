package io.github.seijikohara.dbtester.kotest.extension

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.annotation.DatabaseTest
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
    fun setup(): Unit = run { extension = DatabaseTestExtension(registryProvider = registryProvider) }

    @Test
    fun `should create instance with explicit registry provider`(): Unit =
        DatabaseTestExtension(registryProvider = registryProvider).let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should create instance with no arguments for annotation-based usage`(): Unit =
        DatabaseTestExtension().let { instance ->
            instance shouldNotBe null
            instance.shouldBeInstanceOf<TestCaseExtension>()
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
        DatabaseTestExtension(registryProvider = registryProvider).let { extension1 ->
            DatabaseTestExtension(registryProvider = registryProvider).let { extension2 ->
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

    @Test
    fun `should support interface-based property discovery`(): Unit =
        DatabaseTestExtension().let { instance ->
            instance shouldNotBe null
            instance.shouldBeInstanceOf<TestCaseExtension>()
        }
}

/**
 * Unit tests for [DatabaseTestSupport] interface.
 *
 * This specification verifies that the DatabaseTestSupport interface
 * provides the expected contract for database testing support.
 */
class DatabaseTestSupportSpec : AnnotationSpec() {
    @Test
    fun `should provide default configuration when not overridden`(): Unit =
        object : DatabaseTestSupport {
            override val dbTesterRegistry: DataSourceRegistry = DataSourceRegistry()
        }.let { support ->
            support.dbTesterConfiguration shouldBe Configuration.defaults()
        }

    @Test
    fun `should allow custom configuration override`(): Unit =
        Configuration.defaults().let { customConfig ->
            object : DatabaseTestSupport {
                override val dbTesterRegistry: DataSourceRegistry = DataSourceRegistry()
                override val dbTesterConfiguration: Configuration = customConfig
            }.let { support ->
                support.dbTesterConfiguration shouldBe customConfig
            }
        }

    @Test
    fun `should require registry implementation`(): Unit =
        DataSourceRegistry().let { customRegistry ->
            object : DatabaseTestSupport {
                override val dbTesterRegistry: DataSourceRegistry = customRegistry
            }.let { support ->
                support.dbTesterRegistry shouldBe customRegistry
            }
        }
}

/**
 * Unit tests for [DatabaseTest] annotation.
 *
 * This specification verifies that the DatabaseTest annotation
 * is correctly defined with the expected meta-annotations.
 */
class DatabaseTestAnnotationSpec : AnnotationSpec() {
    @Test
    fun `should have runtime retention`(): Unit =
        DatabaseTest::class
            .annotations
            .filterIsInstance<Retention>()
            .firstOrNull()
            .let { retention ->
                retention shouldNotBe null
                retention?.value shouldBe AnnotationRetention.RUNTIME
            }

    @Test
    fun `should target classes`(): Unit =
        DatabaseTest::class
            .annotations
            .filterIsInstance<Target>()
            .firstOrNull()
            .let { target ->
                target shouldNotBe null
                target?.allowedTargets?.toList()?.contains(AnnotationTarget.CLASS) shouldBe true
            }

    @Test
    fun `should be documented`(): Unit =
        DatabaseTest::class
            .annotations
            .filterIsInstance<MustBeDocumented>()
            .firstOrNull()
            .let { mustBeDocumented ->
                mustBeDocumented shouldNotBe null
            }
}
