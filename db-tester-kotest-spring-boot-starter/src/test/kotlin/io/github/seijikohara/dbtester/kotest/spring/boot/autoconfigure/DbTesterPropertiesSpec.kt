package io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure

import io.github.seijikohara.dbtester.api.config.ConventionSettings
import io.github.seijikohara.dbtester.api.config.DataFormat
import io.github.seijikohara.dbtester.api.config.RowOrdering
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy
import io.github.seijikohara.dbtester.api.config.TransactionMode
import io.github.seijikohara.dbtester.api.operation.Operation
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration

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
            props.enabled shouldBe true
            props.autoRegisterDataSources shouldBe true
        }

    @Test
    fun `should allow changing enabled property`(): Unit =
        properties.let { props ->
            props.enabled = false
            props.enabled shouldBe false
        }

    @Test
    fun `should allow changing autoRegisterDataSources property`(): Unit =
        properties.let { props ->
            props.autoRegisterDataSources = false
            props.autoRegisterDataSources shouldBe false
        }

    @Test
    fun `should have independent property instances`(): Unit =
        DbTesterProperties().let { props1 ->
            DbTesterProperties().let { props2 ->
                props1.enabled = false
                props2.enabled shouldBe true
            }
        }

    @Test
    fun `should preserve multiple property changes`(): Unit =
        properties.let { props ->
            props.enabled = false
            props.autoRegisterDataSources = false
            props.enabled shouldBe false
            props.autoRegisterDataSources shouldBe false
        }

    @Test
    fun `should have convention property with defaults`(): Unit =
        properties.convention.let { convention ->
            convention shouldNotBe null
            convention.baseDirectory shouldBe null
            convention.expectationSuffix shouldBe ConventionSettings.DEFAULT_EXPECTATION_SUFFIX
            convention.scenarioMarker shouldBe ConventionSettings.DEFAULT_SCENARIO_MARKER
            convention.dataFormat shouldBe DataFormat.AUTO
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

    /** Verifies that the verification property has correct default values. */
    @Test
    fun `should have verification property with defaults`(): Unit =
        properties.verification.let { verification ->
            verification shouldNotBe null
            verification.globalExcludeColumns shouldBe emptySet()
            verification.rowOrdering shouldBe RowOrdering.ORDERED
            verification.retryCount shouldBe 0
            verification.retryDelay shouldBe Duration.ofMillis(100)
        }

    /** Verifies that verification properties can be changed individually. */
    @Test
    fun `should allow changing verification properties`(): Unit =
        properties.verification.let { verification ->
            verification.globalExcludeColumns = setOf("created_at", "updated_at")
            verification.rowOrdering = RowOrdering.UNORDERED
            verification.retryCount = 3
            verification.retryDelay = Duration.ofSeconds(1)

            verification.globalExcludeColumns shouldBe setOf("created_at", "updated_at")
            verification.rowOrdering shouldBe RowOrdering.UNORDERED
            verification.retryCount shouldBe 3
            verification.retryDelay shouldBe Duration.ofSeconds(1)
        }

    /** Verifies that the verification object can be replaced entirely. */
    @Test
    fun `should allow replacing verification object`(): Unit =
        DbTesterProperties
            .VerificationProperties()
            .also { newVerification ->
                newVerification.rowOrdering = RowOrdering.UNORDERED
                newVerification.retryCount = 5
            }.let { newVerification ->
                properties.verification = newVerification
                properties.verification.rowOrdering shouldBe RowOrdering.UNORDERED
                properties.verification.retryCount shouldBe 5
            }

    /** Verifies that the execution property has correct default values. */
    @Test
    fun `should have execution property with defaults`(): Unit =
        properties.execution.let { execution ->
            execution shouldNotBe null
            execution.queryTimeout shouldBe null
            execution.transactionMode shouldBe TransactionMode.SINGLE_TRANSACTION
        }

    /** Verifies that execution properties can be changed individually. */
    @Test
    fun `should allow changing execution properties`(): Unit =
        properties.execution.let { execution ->
            execution.queryTimeout = Duration.ofSeconds(30)
            execution.transactionMode = TransactionMode.AUTO_COMMIT

            execution.queryTimeout shouldBe Duration.ofSeconds(30)
            execution.transactionMode shouldBe TransactionMode.AUTO_COMMIT
        }

    /** Verifies that the execution object can be replaced entirely. */
    @Test
    fun `should allow replacing execution object`(): Unit =
        DbTesterProperties
            .ExecutionProperties()
            .also { newExecution ->
                newExecution.queryTimeout = Duration.ofSeconds(30)
                newExecution.transactionMode = TransactionMode.AUTO_COMMIT
            }.let { newExecution ->
                properties.execution = newExecution
                properties.execution.queryTimeout shouldBe Duration.ofSeconds(30)
                properties.execution.transactionMode shouldBe TransactionMode.AUTO_COMMIT
            }
}
