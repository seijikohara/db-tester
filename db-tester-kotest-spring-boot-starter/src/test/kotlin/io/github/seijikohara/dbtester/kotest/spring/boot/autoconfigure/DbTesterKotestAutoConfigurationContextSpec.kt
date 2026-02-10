package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/**
 * Auto-configuration context tests for [DbTesterKotestAutoConfiguration].
 *
 * These tests use [ApplicationContextRunner] to verify conditional auto-configuration
 * behavior, including property-based activation, bean registration, and custom bean overrides.
 */
class DbTesterKotestAutoConfigurationContextSpec : AnnotationSpec() {
    /** Base context runner with the auto-configuration registered. */
    private val contextRunner =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DbTesterKotestAutoConfiguration::class.java))

    @Test
    fun `should register all beans with default properties`() {
        contextRunner.run { context ->
            context.containsBean("dbTesterConfiguration") shouldBe true
            context.containsBean("dbTesterDataSourceRegistry") shouldBe true
            context.containsBean("dataSourceRegistrar") shouldBe true
        }
    }

    @Test
    fun `should register Configuration bean with correct type`() {
        contextRunner.run { context ->
            context.getBean("dbTesterConfiguration").shouldBeInstanceOf<Configuration>()
        }
    }

    @Test
    fun `should register DataSourceRegistry bean with correct type`() {
        contextRunner.run { context ->
            context.getBean("dbTesterDataSourceRegistry").shouldBeInstanceOf<DataSourceRegistry>()
        }
    }

    @Test
    fun `should register DataSourceRegistrar bean with correct type`() {
        contextRunner.run { context ->
            context.getBean("dataSourceRegistrar").shouldBeInstanceOf<DataSourceRegistrar>()
        }
    }

    @Test
    fun `should not register beans when disabled`() {
        contextRunner
            .withPropertyValues("db-tester.enabled=false")
            .run { context ->
                context.containsBean("dbTesterConfiguration") shouldBe false
                context.containsBean("dbTesterDataSourceRegistry") shouldBe false
                context.containsBean("dataSourceRegistrar") shouldBe false
            }
    }

    @Test
    fun `should register beans when explicitly enabled`() {
        contextRunner
            .withPropertyValues("db-tester.enabled=true")
            .run { context ->
                context.containsBean("dbTesterConfiguration") shouldBe true
            }
    }

    @Test
    fun `should register beans when property is not set`() {
        contextRunner.run { context ->
            context.containsBean("dbTesterConfiguration") shouldBe true
        }
    }

    @Test
    fun `should use custom Configuration when provided`() {
        val customConfig = Configuration.defaults()
        contextRunner
            .withBean("dbTesterConfiguration", Configuration::class.java, { customConfig })
            .run { context ->
                context.containsBean("dbTesterConfiguration") shouldBe true
                (context.getBean("dbTesterConfiguration") === customConfig) shouldBe true
            }
    }

    @Test
    fun `should use custom DataSourceRegistry when provided`() {
        val customRegistry = DataSourceRegistry()
        contextRunner
            .withBean("dbTesterDataSourceRegistry", DataSourceRegistry::class.java, { customRegistry })
            .run { context ->
                (context.getBean("dbTesterDataSourceRegistry") === customRegistry) shouldBe true
            }
    }

    @Test
    fun `should bind convention properties`() {
        contextRunner
            .withPropertyValues(
                "db-tester.convention.expectation-suffix=/verify",
                "db-tester.convention.scenario-marker=[Test]",
            ).run { context ->
                val config = context.getBean("dbTesterConfiguration", Configuration::class.java)
                config.conventions().expectationSuffix() shouldBe "/verify"
                config.conventions().scenarioMarker() shouldBe "[Test]"
            }
    }

    @Test
    fun `should bind auto-register-data-sources property`() {
        contextRunner
            .withPropertyValues("db-tester.auto-register-data-sources=false")
            .run { context ->
                context.getBean(DbTesterProperties::class.java).isAutoRegisterDataSources shouldBe false
            }
    }
}
