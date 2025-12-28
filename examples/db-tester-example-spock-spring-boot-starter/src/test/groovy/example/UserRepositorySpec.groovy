package example

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.spock.spring.boot.autoconfigure.SpringBootDatabaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

/**
 * Integration tests for UserRepository demonstrating db-tester-spock-spring-boot-starter.
 *
 * <p>This test class demonstrates how the database testing framework integrates with Spring Boot
 * using Spock for automatic DataSource registration:
 *
 * <ul>
 *   <li>Automatic DataSource registration via Spring auto-configuration
 *   <li>Convention-based CSV file resolution
 *   <li>{@code @Preparation} and {@code @Expectation} annotations for database state management
 *   <li>Spring Data JPA integration with test framework
 * </ul>
 *
 * <p>CSV files are located at:
 *
 * <ul>
 *   <li>{@code src/test/resources/example/UserRepositorySpec/USERS.csv}
 *   <li>{@code src/test/resources/example/UserRepositorySpec/expected/USERS.csv}
 * </ul>
 *
 * <p><strong>Note:</strong> No manual {@code DataSourceRegistry} configuration is required.
 * The {@code db-tester-spock-spring-boot-starter} automatically discovers and registers
 * Spring-managed DataSources.
 */
@SpringBootTest(classes = ExampleApplication)
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

	@Autowired
	UserRepository userRepository

	@Preparation
	def "should find all users"() {
		when:
		def users = userRepository.findAllByOrderByIdAsc()

		then:
		users.size() == 2
		users[0].name == 'Alice'
		users[1].name == 'Bob'
	}

	@Preparation
	def "should find user by id"() {
		when:
		def userOptional = userRepository.findById(1L)

		then:
		userOptional.present
		userOptional.get().name == 'Alice'
		userOptional.get().email == 'alice@example.com'
	}

	@Preparation
	@Expectation
	def "should save new user"() {
		when:
		userRepository.save(new User(3L, 'Charlie', 'charlie@example.com'))

		then:
		noExceptionThrown()
	}

	@Preparation
	def "should delete user"() {
		when:
		userRepository.deleteById(2L)
		def remainingUsers = userRepository.findAllByOrderByIdAsc()

		then:
		remainingUsers.size() == 1
		remainingUsers[0].name == 'Alice'
	}
}
