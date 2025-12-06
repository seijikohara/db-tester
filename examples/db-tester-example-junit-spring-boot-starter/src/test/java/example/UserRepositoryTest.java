package example;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure.SpringBootDatabaseTestExtension;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for UserRepository demonstrating db-tester-junit-spring-boot-starter.
 *
 * <p>This test class demonstrates how the database testing framework integrates with Spring Boot
 * using {@link SpringBootDatabaseTestExtension} for automatic DataSource registration:
 *
 * <ul>
 *   <li>Automatic DataSource registration via {@link SpringBootDatabaseTestExtension} - no manual
 *       {@code @BeforeAll} setup required
 *   <li>Convention-based CSV file resolution
 *   <li>{@code @Preparation} and {@code @Expectation} annotations for database state management
 *   <li>Spring Data JPA integration with test framework
 * </ul>
 *
 * <p>CSV files are located at:
 *
 * <ul>
 *   <li>{@code src/test/resources/example/UserRepositoryTest/USERS.csv}
 *   <li>{@code src/test/resources/example/UserRepositoryTest/expected/USERS.csv}
 * </ul>
 */
@SpringBootTest(classes = ExampleApplication.class)
@ExtendWith(SpringBootDatabaseTestExtension.class)
public class UserRepositoryTest {

  /** Logger instance for test execution logging. */
  private static final Logger logger = LoggerFactory.getLogger(UserRepositoryTest.class);

  /** User repository under test. */
  private final UserRepository userRepository;

  /**
   * Creates a new test instance with the required dependencies.
   *
   * @param userRepository the user repository to test
   */
  @Autowired
  public UserRepositoryTest(final UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Verifies that findAllByOrderByIdAsc returns all users from the prepared database.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads initial users from {@code USERS.csv}
   *   <li>Execution: Calls findAllByOrderByIdAsc() to retrieve all users
   *   <li>Verification: Asserts the correct number and content of users
   * </ul>
   */
  @Test
  @Preparation
  void shouldFindAllUsers() {
    logger.info("Testing findAllByOrderByIdAsc() operation");

    final List<User> users = userRepository.findAllByOrderByIdAsc();

    assertAll(
        "findAllByOrderByIdAsc results",
        () -> assertEquals(2, users.size()),
        () -> assertEquals("Alice", users.get(0).getName()),
        () -> assertEquals("Bob", users.get(1).getName()));

    logger.info("Found {} users", users.size());
  }

  /**
   * Verifies that findById returns the correct user.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads initial users from {@code USERS.csv}
   *   <li>Execution: Calls findById() to retrieve a specific user
   *   <li>Verification: Asserts the correct user is returned
   * </ul>
   */
  @Test
  @Preparation
  void shouldFindUserById() {
    logger.info("Testing findById() operation");

    final var userOptional = userRepository.findById(1L);

    assertTrue(userOptional.isPresent(), "User should be present");
    final var user = userOptional.orElseThrow();

    assertAll(
        "findById results",
        () -> assertEquals("Alice", user.getName()),
        () -> assertEquals("alice@example.com", user.getEmail()));

    logger.info("Found user: {}", user);
  }

  /**
   * Verifies that save inserts a new user and the database state matches expectations.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads initial users from {@code USERS.csv}
   *   <li>Execution: Saves a new user
   *   <li>Expectation: Verifies final database state from {@code expected/USERS.csv}
   * </ul>
   */
  @Test
  @Preparation
  @Expectation
  void shouldSaveNewUser() {
    logger.info("Testing save() operation");

    final var newUser = new User(3L, "Charlie", "charlie@example.com");
    userRepository.save(newUser);

    logger.info("Saved new user: {}", newUser);
  }

  /**
   * Verifies that deleteById removes a user correctly.
   *
   * <p>Test flow:
   *
   * <ul>
   *   <li>Preparation: Loads initial users from {@code USERS.csv}
   *   <li>Execution: Deletes user with ID 2
   *   <li>Verification: Asserts the user was deleted and remaining users are correct
   * </ul>
   */
  @Test
  @Preparation
  void shouldDeleteUser() {
    logger.info("Testing deleteById() operation");

    userRepository.deleteById(2L);

    // Verify the user was deleted
    final List<User> remainingUsers = userRepository.findAllByOrderByIdAsc();

    assertAll(
        "deleteById results",
        () -> assertEquals(1, remainingUsers.size()),
        () -> assertEquals("Alice", remainingUsers.get(0).getName()));

    logger.info("Deleted user with ID 2, {} users remaining", remainingUsers.size());
  }
}
