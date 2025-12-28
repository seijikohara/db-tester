package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.operation.Operation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Unit tests for [DbTesterProperties].
 *
 * This specification verifies the configuration properties for
 * DB Tester Spring Boot integration.
 */
class DbTesterPropertiesSpec : AnnotationSpec() {
    /** The properties under test. */
    private lateinit var properties: DbTesterProperties

    @BeforeEach
    fun setup(): Unit = run { properties = DbTesterProperties() }

    @Test
    fun `should create instance`(): Unit =
        DbTesterProperties().let { instance ->
            instance shouldNotBe null
        }

    @Test
    fun `should have default values`(): Unit =
        properties.let { props ->
            props.isEnabled shouldBe true
            props.isAutoRegisterDataSources shouldBe true
        }

    @Test
    fun `should allow changing enabled property`(): Unit =
        properties.let { props ->
            props.isEnabled = false
            props.isEnabled shouldBe false
        }

    @Test
    fun `should allow changing autoRegisterDataSources property`(): Unit =
        properties.let { props ->
            props.isAutoRegisterDataSources = false
            props.isAutoRegisterDataSources shouldBe false
        }

    @Test
    fun `should have independent property instances`(): Unit =
        DbTesterProperties().let { props1 ->
            DbTesterProperties().let { props2 ->
                props1.isEnabled = false
                props2.isEnabled shouldBe true
            }
        }

    @Test
    fun `should preserve multiple property changes`(): Unit =
        properties.let { props ->
            props.isEnabled = false
            props.isAutoRegisterDataSources = false
            props.isEnabled shouldBe false
            props.isAutoRegisterDataSources shouldBe false
        }

    @Test
    fun `should have convention property with defaults`(): Unit =
        properties.convention.let { convention ->
            convention shouldNotBe null
            convention.baseDirectory shouldBe null
            convention.expectationSuffix shouldBe ConventionSettings.DEFAULT_EXPECTATION_SUFFIX
            convention.scenarioMarker shouldBe ConventionSettings.DEFAULT_SCENARIO_MARKER
            convention.dataFormat shouldBe DataFormat.CSV
            convention.tableMergeStrategy shouldBe TableMergeStrategy.UNION_ALL
            convention.loadOrderFileName shouldBe ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME
        }

    @Test
    fun `should allow changing convention properties`(): Unit =
        properties.convention.let { convention ->
            convention.baseDirectory = "custom/path"
            convention.expectationSuffix = "-expected"
            convention.scenarioMarker = "TEST_SCENARIO"
            convention.dataFormat = DataFormat.CSV
            convention.tableMergeStrategy = TableMergeStrategy.FIRST

            convention.baseDirectory shouldBe "custom/path"
            convention.expectationSuffix shouldBe "-expected"
            convention.scenarioMarker shouldBe "TEST_SCENARIO"
            convention.dataFormat shouldBe DataFormat.CSV
            convention.tableMergeStrategy shouldBe TableMergeStrategy.FIRST
        }

    @Test
    fun `should allow replacing convention object`(): Unit =
        DbTesterProperties
            .ConventionProperties()
            .also { newConvention ->
                newConvention.baseDirectory = "replaced/path"
            }.let { newConvention ->
                properties.convention = newConvention
                properties.convention.baseDirectory shouldBe "replaced/path"
            }

    @Test
    fun `should have operation property with defaults`(): Unit =
        properties.operation.let { operation ->
            operation shouldNotBe null
            operation.preparation shouldBe Operation.CLEAN_INSERT
            operation.expectation shouldBe Operation.NONE
        }

    @Test
    fun `should allow changing operation properties`(): Unit =
        properties.operation.let { operation ->
            operation.preparation = Operation.INSERT
            operation.expectation = Operation.DELETE_ALL

            operation.preparation shouldBe Operation.INSERT
            operation.expectation shouldBe Operation.DELETE_ALL
        }

    @Test
    fun `should allow replacing operation object`(): Unit =
        DbTesterProperties
            .OperationProperties()
            .also { newOperation ->
                newOperation.preparation = Operation.DELETE_ALL
                newOperation.expectation = Operation.INSERT
            }.let { newOperation ->
                properties.operation = newOperation
                properties.operation.preparation shouldBe Operation.DELETE_ALL
                properties.operation.expectation shouldBe Operation.INSERT
            }
}
