package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.ObjectProvider
import java.util.stream.Stream
import javax.sql.DataSource

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
    fun `should create Configuration bean with default properties`(): Unit =
        autoConfiguration.dbTesterConfiguration(properties).let { configuration ->
            configuration shouldNotBe null
            configuration.conventions() shouldNotBe null
            configuration.operations() shouldNotBe null
            configuration.loader() shouldNotBe null
        }

    @Test
    fun `should create Configuration bean with custom conventions`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.convention.baseDirectory = "custom/path"
                customProperties.convention.expectationSuffix = "-expected"
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration shouldNotBe null
                    configuration.conventions().baseDirectory() shouldBe "custom/path"
                    configuration.conventions().expectationSuffix() shouldBe "-expected"
                }
            }

    @Test
    fun `should create DataSourceRegistry bean`(): Unit =
        mockk<ObjectProvider<DataSource>>()
            .also { dataSourceProvider ->
                every { dataSourceProvider.stream() } returns Stream.empty()
            }.let { dataSourceProvider ->
                autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider).let { registry ->
                    registry shouldNotBe null
                    registry.hasDefault() shouldBe false
                }
            }

    @Test
    fun `should create DataSourceRegistry bean with DataSource`(): Unit =
        mockk<DataSource>().let { dataSource ->
            mockk<ObjectProvider<DataSource>>()
                .also { dataSourceProvider ->
                    every { dataSourceProvider.stream() } returns Stream.of(dataSource)
                }.let { dataSourceProvider ->
                    autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider).let { registry ->
                        registry shouldNotBe null
                        registry.hasDefault() shouldBe true
                        registry.getDefault() shouldBe dataSource
                    }
                }
        }

    @Test
    fun `should create DataSourceRegistrar bean`(): Unit =
        autoConfiguration.dataSourceRegistrar(properties).let { registrar ->
            registrar shouldNotBe null
        }

    @Test
    fun `should create all beans end-to-end`(): Unit =
        DbTesterProperties().let { props ->
            mockk<ObjectProvider<DataSource>>()
                .also { dataSourceProvider ->
                    every { dataSourceProvider.stream() } returns Stream.empty()
                }.let { dataSourceProvider ->
                    autoConfiguration.dbTesterConfiguration(props).let { configuration ->
                        autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider).let { registry ->
                            autoConfiguration.dataSourceRegistrar(props).let { registrar ->
                                configuration.shouldNotBe(null)
                                registry.shouldNotBe(null)
                                registrar.shouldNotBe(null)
                            }
                        }
                    }
                }
        }

    @Test
    fun `should create Configuration with correct loader`(): Unit =
        autoConfiguration.dbTesterConfiguration(properties).let { configuration ->
            configuration.loader() shouldNotBe null
        }

    @Test
    fun `should create new registry instance each time`(): Unit =
        mockk<ObjectProvider<DataSource>>()
            .also { dataSourceProvider ->
                every { dataSourceProvider.stream() } returns Stream.empty()
            }.let { dataSourceProvider ->
                autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider).let { registry1 ->
                    every { dataSourceProvider.stream() } returns Stream.empty()
                    autoConfiguration.dbTesterDataSourceRegistry(dataSourceProvider).let { registry2 ->
                        (registry1 === registry2) shouldBe false
                    }
                }
            }
}
