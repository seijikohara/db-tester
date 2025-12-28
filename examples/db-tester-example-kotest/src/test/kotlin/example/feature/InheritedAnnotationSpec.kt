package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory

/**
 * Demonstrates annotation inheritance from a base specification with Kotest.
 *
 * This specification inherits:
 * - Class-level [Preparation] annotation from [InheritanceSpecBase]
 * - Database setup and utility methods
 *
 * Each test method automatically uses the base specification's [Preparation] unless overridden
 * at the method level.
 *
 * Directory structure:
 * ```
 * example/feature/InheritanceSpecBase/
 *   TABLE1.csv          (base setup data)
 *   expected/
 *     TABLE1.csv
 * example/feature/InheritedAnnotationSpec/
 *   expected/
 *     TABLE1.csv        (child class specific expectations)
 * ```
 */
class InheritedAnnotationSpec : InheritanceSpecBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(InheritedAnnotationSpec::class.java)
    }

    /**
     * Tests using inherited class-level @Preparation annotation.
     *
     * This test uses the [Preparation] from [InheritanceSpecBase] automatically.
     *
     * Test flow:
     * - Preparation: Uses inherited @Preparation (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
     * - Execution: Inserts ID=3 (Monitor, 20, Warehouse B)
     * - Expectation: Verifies all three products exist (Laptop, Keyboard, Monitor)
     */
    @Test
    @Expectation
    fun `should use inherited preparation`(): Unit =
        logger.info("Running inherited preparation test").also {
            executeSql(
                """
                INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
                VALUES (3, 'Monitor', 20, 'Warehouse B')
                """.trimIndent(),
            )
            val count = getRecordCount("TABLE1")
            count shouldBe 3
            logger.info("Inherited preparation test completed")
        }

    /**
     * Tests overriding inherited @Preparation with method-level annotation.
     *
     * The method-level [Preparation] takes precedence over the inherited class-level
     * annotation.
     *
     * Test flow:
     * - Preparation: Uses method-level @Preparation (overrideSetup) - TABLE1(ID=1 Laptop, COLUMN2=30)
     * - Execution: Updates ID=1 COLUMN2 from 30 to 50
     * - Expectation: Verifies ID=1 has COLUMN2=50
     */
    @Test
    @Preparation(dataSets = [DataSet(scenarioNames = ["overrideSetup"])])
    @Expectation(dataSets = [DataSet(scenarioNames = ["overrideSetup"])])
    fun `should override inherited preparation`(): Unit =
        logger.info("Running override inherited preparation test").also {
            executeSql("UPDATE TABLE1 SET COLUMN2 = 50 WHERE ID = 1")
            logger.info("Override inherited preparation test completed")
        }

    /**
     * Tests combining inherited and method-level expectations.
     *
     * Uses inherited [Preparation] but adds method-level [Expectation].
     *
     * Test flow:
     * - Preparation: Uses inherited @Preparation (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
     * - Execution: Updates Laptop's COLUMN3 from 'Warehouse A' to 'Warehouse C'
     * - Expectation: Verifies ID=1 has COLUMN3='Warehouse C', ID=2 unchanged
     */
    @Test
    @Expectation(dataSets = [DataSet(scenarioNames = ["combinedTest"])])
    fun `should combine inherited and method level annotations`(): Unit =
        logger.info("Running combined annotations test").also {
            executeSql(
                """
                UPDATE TABLE1 SET COLUMN3 = 'Warehouse C' WHERE COLUMN1 = 'Laptop'
                """.trimIndent(),
            )
            logger.info("Combined annotations test completed")
        }
}
