package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.Configuration
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.ObjectProvider
import java.time.Duration
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

    /** Verifies that the Configuration bean includes verification settings. */
    @Test
    fun `should create Configuration with verification settings`(): Unit =
        autoConfiguration.dbTesterConfiguration(properties).let { configuration ->
            configuration.verification() shouldNotBe null
        }

    /** Verifies that the Configuration bean includes execution settings. */
    @Test
    fun `should create Configuration with execution settings`(): Unit =
        autoConfiguration.dbTesterConfiguration(properties).let { configuration ->
            configuration.execution() shouldNotBe null
        }

    /** Verifies that custom convention properties are correctly mapped to Configuration. */
    @Test
    fun `should map custom convention properties to Configuration`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.convention.baseDirectory = "custom/path"
                customProperties.convention.expectationSuffix = "-verify"
                customProperties.convention.scenarioMarker = "[Test]"
                customProperties.convention.dataFormat = DataFormat.TSV
                customProperties.convention.tableMergeStrategy = TableMergeStrategy.FIRST
                customProperties.convention.loadOrderFileName = "custom-order.txt"
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration.conventions().baseDirectory() shouldBe "custom/path"
                    configuration.conventions().expectationSuffix() shouldBe "-verify"
                    configuration.conventions().scenarioMarker() shouldBe "[Test]"
                    configuration.conventions().dataFormat() shouldBe DataFormat.TSV
                    configuration.conventions().tableMergeStrategy() shouldBe TableMergeStrategy.FIRST
                    configuration.conventions().loadOrderFileName() shouldBe "custom-order.txt"
                }
            }

    /** Verifies that custom verification properties are correctly mapped to Configuration. */
    @Test
    fun `should map custom verification properties to Configuration`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.verification.globalExcludeColumns = setOf("created_at", "updated_at")
                customProperties.verification.rowOrdering = RowOrdering.UNORDERED
                customProperties.verification.retryCount = 3
                customProperties.verification.retryDelay = Duration.ofSeconds(2)
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration.verification().globalExcludeColumns() shouldBe setOf("created_at", "updated_at")
                    configuration.verification().rowOrdering() shouldBe RowOrdering.UNORDERED
                    configuration.verification().retryCount() shouldBe 3
                    configuration.verification().retryDelay() shouldBe Duration.ofSeconds(2)
                }
            }

    /** Verifies that custom execution properties are correctly mapped to Configuration. */
    @Test
    fun `should map custom execution properties to Configuration`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.execution.queryTimeout = Duration.ofSeconds(30)
                customProperties.execution.transactionMode = TransactionMode.AUTO_COMMIT
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration.execution().queryTimeout() shouldBe Duration.ofSeconds(30)
                    configuration.execution().transactionMode() shouldBe TransactionMode.AUTO_COMMIT
                }
            }

    /** Verifies that custom operation properties are correctly mapped to Configuration. */
    @Test
    fun `should map custom operation properties to Configuration`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                customProperties.operation.preparation = Operation.INSERT
                customProperties.operation.expectation = Operation.DELETE_ALL
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration.operations().preparation() shouldBe Operation.INSERT
                    configuration.operations().expectation() shouldBe Operation.DELETE_ALL
                }
            }

    /** Verifies that column strategies properties are correctly mapped to Configuration. */
    @Test
    fun `should map column strategies properties to Configuration`(): Unit =
        DbTesterProperties()
            .also { customProperties ->
                val timestampStrategy =
                    DbTesterProperties.ColumnStrategyProperty().apply {
                        columnName = "CREATED_AT"
                        strategy = ComparisonStrategy.Type.TIMESTAMP_FLEXIBLE
                    }
                val ignoreStrategy =
                    DbTesterProperties.ColumnStrategyProperty().apply {
                        columnName = "updated_at"
                        strategy = ComparisonStrategy.Type.IGNORE
                    }
                customProperties.verification.columnStrategies = mutableListOf(timestampStrategy, ignoreStrategy)
            }.let { customProperties ->
                autoConfiguration.dbTesterConfiguration(customProperties).let { configuration ->
                    configuration.verification().globalColumnStrategies().size shouldBe 2
                    configuration
                        .verification()
                        .globalColumnStrategies()["CREATED_AT"]!!
                        .strategy() shouldBe ComparisonStrategy.TIMESTAMP_FLEXIBLE
                    configuration
                        .verification()
                        .globalColumnStrategies()["UPDATED_AT"]!!
                        .strategy() shouldBe ComparisonStrategy.IGNORE
                }
            }

    /** Verifies that empty column strategies produces empty map. */
    @Test
    fun `should produce empty column strategies when none configured`(): Unit =
        autoConfiguration.dbTesterConfiguration(DbTesterProperties()).let { configuration ->
            configuration.verification().globalColumnStrategies() shouldBe emptyMap()
        }

    /** Verifies that default properties produce correct default values for all Configuration sections. */
    @Test
    fun `should produce default values for all Configuration sections`(): Unit =
        autoConfiguration.dbTesterConfiguration(DbTesterProperties()).let { configuration ->
            configuration.conventions() shouldNotBe null
            configuration.conventions().baseDirectory() shouldBe null
            configuration.conventions().dataFormat() shouldBe DataFormat.AUTO
            configuration.verification() shouldNotBe null
            configuration.verification().globalExcludeColumns() shouldBe emptySet()
            configuration.verification().rowOrdering() shouldBe RowOrdering.ORDERED
            configuration.verification().retryCount() shouldBe 0
            configuration.verification().retryDelay() shouldBe Duration.ofMillis(100)
            configuration.execution() shouldNotBe null
            configuration.execution().queryTimeout() shouldBe null
            configuration.execution().transactionMode() shouldBe TransactionMode.SINGLE_TRANSACTION
            configuration.operations() shouldNotBe null
            configuration.operations().preparation() shouldBe Operation.CLEAN_INSERT
            configuration.operations().expectation() shouldBe Operation.NONE
        }
}
