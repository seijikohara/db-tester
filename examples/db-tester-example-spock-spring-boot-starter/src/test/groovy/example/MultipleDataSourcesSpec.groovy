package example

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure.SpringBootDatabaseTest
import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.simple.JdbcClient
import spock.lang.Specification

/**
 * Integration test demonstrating multiple DataSource support with Spock.
 *
 * <p>This test verifies that:
 *
 * <ul>
 *   <li>Multiple DataSources can be defined and injected via Spring
 *   <li>The {@code @Primary} DataSource is used for default operations
 *   <li>Named DataSources are accessible via their bean names
 *   <li>The {@code @DataSet} annotation works with the primary DataSource
 * </ul>
 *
 * <p>CSV files are located at:
 * <ul>
 *   <li>{@code src/test/resources/example/MultipleDataSourcesSpec/USERS.csv}
 * </ul>
 */
@SpringBootTest(classes = [ExampleApplication, MultipleDataSourcesSpec.MultiDataSourceConfig])
@SpringBootDatabaseTest
class MultipleDataSourcesSpec extends Specification {

	private static final Logger logger = LoggerFactory.getLogger(MultipleDataSourcesSpec)

	@Autowired
	@Qualifier("mainDb")
	DataSource mainDataSource

	@Autowired
	@Qualifier("archiveDb")
	DataSource archiveDataSource

	def "should have multiple DataSources injected"() {
		expect: 'both DataSources are injected'
		mainDataSource != null
		archiveDataSource != null
		mainDataSource != archiveDataSource

		logger.info("Successfully verified multiple DataSource injection")
	}

	@DataSet
	def "should prepare default DataSource"() {
		when: 'querying the primary DataSource after preparation'
		logger.info("Testing preparation with default (primary) DataSource")
		def jdbcClient = JdbcClient.create(mainDataSource)
		def count = jdbcClient.sql("SELECT COUNT(*) FROM USERS").query(Long).single()

		then: 'primary DataSource has prepared data from CSV'
		count == 2L
		logger.info("Primary DataSource has {} users", count)
	}

	def "should access named DataSource directly"() {
		when: 'inserting data into the archive DataSource'
		logger.info("Testing direct access to named DataSource (archiveDb)")
		def jdbcClient = JdbcClient.create(archiveDataSource)

		jdbcClient.sql(
				"INSERT INTO ARCHIVED_USERS (ID, NAME, EMAIL, ARCHIVED_AT) VALUES (1, 'Test', 'test@example.com', CURRENT_TIMESTAMP)")
				.update()

		def count = jdbcClient.sql("SELECT COUNT(*) FROM ARCHIVED_USERS").query(Long).single()

		then: 'archiveDb has the inserted data'
		count == 1L
		logger.info("archiveDb has {} archived users", count)
	}

	/**
	 * Test configuration that defines multiple DataSources.
	 */
	@TestConfiguration
	static class MultiDataSourceConfig {

		@Bean
		@Primary
		DataSource mainDb() {
			def dataSource = DataSourceBuilder.create()
					.url("jdbc:h2:mem:spock_maindb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
					.driverClassName("org.h2.Driver")
					.username("sa")
					.password("")
					.build()

			// Initialize schema
			JdbcClient.create(dataSource).sql("""
				CREATE TABLE IF NOT EXISTS USERS (
					ID BIGINT PRIMARY KEY,
					NAME VARCHAR(255) NOT NULL,
					EMAIL VARCHAR(255) NOT NULL
				)
			""").update()

			return dataSource
		}

		@Bean
		DataSource archiveDb() {
			def dataSource = DataSourceBuilder.create()
					.url("jdbc:h2:mem:spock_archivedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;SCHEMA=PUBLIC")
					.driverClassName("org.h2.Driver")
					.username("sa")
					.password("")
					.build()

			// Initialize schema
			JdbcClient.create(dataSource).sql("""
				CREATE TABLE IF NOT EXISTS ARCHIVED_USERS (
					ID BIGINT PRIMARY KEY,
					NAME VARCHAR(255) NOT NULL,
					EMAIL VARCHAR(255) NOT NULL,
					ARCHIVED_AT TIMESTAMP NOT NULL
				)
			""").update()

			return dataSource
		}
	}
}
