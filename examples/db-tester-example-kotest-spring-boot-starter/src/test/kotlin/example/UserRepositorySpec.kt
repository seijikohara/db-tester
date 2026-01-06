package example

import io.github.seijikohara.dbtester.api.annotation.DataSet
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
import io.github.seijikohara.dbtester.kotest.spring.boot.autoconfigure.SpringBootDatabaseTestExtension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * Integration tests for UserRepository demonstrating db-tester-kotest-spring-boot-starter.
 *
 * This test class demonstrates how the database testing framework integrates with Spring Boot
 * using [SpringBootDatabaseTestExtension] for automatic DataSource registration:
 *
 * - Automatic DataSource registration via [SpringBootDatabaseTestExtension] - no manual setup required
 * - Convention-based CSV file resolution
 * - `@DataSet` and `@ExpectedDataSet` annotations for database state management
 * - Spring Data JPA integration with test framework
 *
 * CSV files are located at:
 * - `src/test/resources/example/UserRepositorySpec/USERS.csv`
 * - `src/test/resources/example/UserRepositorySpec/expected/USERS.csv`
 */
@SpringBootTest(classes = [ExampleApplication::class])
class UserRepositorySpec : AnnotationSpec() {
    companion object {
        private val logger = LoggerFactory.getLogger(UserRepositorySpec::class.java)
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    /**
     * Verifies that findAllByOrderByIdAsc returns all users from the prepared database.
     *
     * Test flow:
     * - Preparation: Loads initial users from `USERS.csv`
     * - Execution: Calls findAllByOrderByIdAsc() to retrieve all users
     * - Verification: Asserts the correct number and content of users
     */
    @Test
    @DataSet
    fun `should find all users`(): Unit =
        logger
            .info("Testing findAllByOrderByIdAsc() operation")
            .let {
                userRepository.findAllByOrderByIdAsc().also { users ->
                    users shouldHaveSize 2
                    users[0].name shouldBe "Alice"
                    users[1].name shouldBe "Bob"
                    logger.info("Found {} users", users.size)
                }
            }.let { }

    /**
     * Verifies that findById returns the correct user.
     *
     * Test flow:
     * - Preparation: Loads initial users from `USERS.csv`
     * - Execution: Calls findById() to retrieve a specific user
     * - Verification: Asserts the correct user is returned
     */
    @Test
    @DataSet
    fun `should find user by id`(): Unit =
        logger
            .info("Testing findById() operation")
            .let {
                userRepository.findById(1L).orElse(null).also { user ->
                    user shouldNotBe null
                    user.name shouldBe "Alice"
                    user.email shouldBe "alice@example.com"
                    logger.info("Found user: {}", user)
                }
            }.let { }

    /**
     * Verifies that save inserts a new user and the database state matches expectations.
     *
     * Test flow:
     * - Preparation: Loads initial users from `USERS.csv`
     * - Execution: Saves a new user
     * - Expectation: Verifies final database state from `expected/USERS.csv`
     */
    @Test
    @DataSet
    @ExpectedDataSet
    fun `should save new user`(): Unit =
        logger
            .info("Testing save() operation")
            .let {
                User(id = 3L, name = "Charlie", email = "charlie@example.com").also { newUser ->
                    userRepository.save(newUser)
                    logger.info("Saved new user: {}", newUser)
                }
            }.let { }

    /**
     * Verifies that deleteById removes a user correctly.
     *
     * Test flow:
     * - Preparation: Loads initial users from `USERS.csv`
     * - Execution: Deletes user with ID 2
     * - Verification: Asserts the user was deleted and remaining users are correct
     */
    @Test
    @DataSet
    fun `should delete user`(): Unit =
        logger
            .info("Testing deleteById() operation")
            .let {
                userRepository.deleteById(2L)
                userRepository.findAllByOrderByIdAsc().also { remainingUsers ->
                    remainingUsers shouldHaveSize 1
                    remainingUsers[0].name shouldBe "Alice"
                    logger.info("Deleted user with ID 2, {} users remaining", remainingUsers.size)
                }
            }.let { }
}
