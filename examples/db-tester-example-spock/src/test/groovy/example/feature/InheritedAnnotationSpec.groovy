package example.feature

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.DataSetSource
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet

/**
 * Demonstrates annotation inheritance from a base specification with Spock.
 *
 * <p>This specification inherits:
 * <ul>
 *   <li>Class-level {@code @DataSet} annotation from {@link InheritanceSpecBase}
 *   <li>Database setup and utility methods
 * </ul>
 *
 * <p>Each feature method automatically uses the base specification's {@code @DataSet} unless overridden
 * at the method level.
 *
 * <p>Directory structure:
 * <pre>
 * example/feature/InheritanceSpecBase/
 *   TABLE1.csv          (base setup data)
 *   expected/
 *     TABLE1.csv
 * example/feature/InheritedAnnotationSpec/
 *   expected/
 *     TABLE1.csv        (child class specific expectations)
 * </pre>
 */
class InheritedAnnotationSpec extends InheritanceSpecBase {

	/**
	 * Tests using inherited class-level @DataSet annotation.
	 *
	 * <p>This test uses the {@code @DataSet} from {@link InheritanceSpecBase} automatically.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Uses inherited @DataSet (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
	 *   <li>Execution: Inserts ID=3 (Monitor, 20, Warehouse B)
	 *   <li>Expectation: Verifies all three products exist (Laptop, Keyboard, Monitor)
	 * </ul>
	 */
	@ExpectedDataSet
	def 'should use inherited preparation'() {
		when: 'inserting a new product using inherited preparation'
		sql.execute '''
			INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2, COLUMN3)
			VALUES (3, 'Monitor', 20, 'Warehouse B')
		'''

		then: 'record count is correct'
		def count = getRecordCount('TABLE1')
		count == 3

		and: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Tests overriding inherited @DataSet with method-level annotation.
	 *
	 * <p>The method-level {@code @DataSet} takes precedence over the inherited class-level
	 * annotation.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Uses method-level @DataSet (overrideSetup) - TABLE1(ID=1 Laptop,
	 *       COLUMN2=30)
	 *   <li>Execution: Updates ID=1 COLUMN2 from 30 to 50
	 *   <li>Expectation: Verifies ID=1 has COLUMN2=50
	 * </ul>
	 */
	@DataSet(dataSets = @DataSetSource(scenarioNames = 'overrideSetup'))
	@ExpectedDataSet(dataSets = @DataSetSource(scenarioNames = 'overrideSetup'))
	def 'should override inherited preparation'() {
		when: 'updating a record with overridden preparation'
		sql.executeUpdate 'UPDATE TABLE1 SET COLUMN2 = 50 WHERE ID = 1'

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}

	/**
	 * Tests combining inherited and method-level expectations.
	 *
	 * <p>Uses inherited {@code @DataSet} but adds method-level {@code @ExpectedDataSet}.
	 *
	 * <p>Test flow:
	 * <ul>
	 *   <li>Preparation: Uses inherited @DataSet (baseSetup) - TABLE1(ID=1 Laptop, ID=2 Keyboard)
	 *   <li>Execution: Updates Laptop's COLUMN3 from 'Warehouse A' to 'Warehouse C'
	 *   <li>Expectation: Verifies ID=1 has COLUMN3='Warehouse C', ID=2 unchanged
	 * </ul>
	 */
	@ExpectedDataSet(dataSets = @DataSetSource(scenarioNames = 'combinedTest'))
	def 'should combine inherited and method level annotations'() {
		when: 'updating a record with combined annotations'
		sql.execute '''
			UPDATE TABLE1 SET COLUMN3 = 'Warehouse C' WHERE COLUMN1 = 'Laptop'
		'''

		then: 'expectation phase verifies database state'
		noExceptionThrown()
	}
}
