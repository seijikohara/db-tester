package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.operation.Operation
import io.github.seijikohara.dbtester.spring.support.DataSourceRegistrar
import io.github.seijikohara.dbtester.spring.support.DbTesterProperties
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Unit tests for [DbTesterKotestAutoConfiguration].
 *
 * This specification verifies the auto-configuration of DB Tester
 * components for Spring Boot integration with Kotest.
 */
class DbTesterKotestAutoConfigurationSpec : AnnotationSpec() {
    /** The configuration under test. */
    private lateinit var autoConfiguration: DbTesterKotestAutoConfiguration

    /** Test properties. */
    private lateinit var properties: DbTesterProperties

    @BeforeEach
    fun setup(): Unit =
        run {
            autoConfiguration = DbTesterKotestAutoConfiguration()
            properties = DbTesterProperties()
        }

    @Test
    fun `should create instance`(): Unit =
        DbTesterKotestAutoConfiguration().let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should build Configuration from properties`(): Unit =
        DbTesterProperties()
            .also { it.operation.preparation = Operation.INSERT }
            .let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration shouldNotBe null
                    configuration.operations().preparation() shouldBe Operation.INSERT
                }
            }

    @Test
    fun `should create DataSourceRegistrar bean`(): Unit =
        autoConfiguration.dataSourceRegistrar(properties).let { registrar ->
            registrar.shouldBeInstanceOf<DataSourceRegistrar>()
        }

    @Test
    fun `should create new DataSourceRegistrar instance each time`(): Unit =
        autoConfiguration.dataSourceRegistrar(properties).let { registrar1 ->
            autoConfiguration.dataSourceRegistrar(properties).let { registrar2 ->
                (registrar1 === registrar2) shouldBe false
            }
        }
}
