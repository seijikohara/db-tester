package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application for demonstrating db-tester-junit-spring-boot-starter.
 *
 * <p>This application provides a minimal Spring Boot setup with H2 database for testing the
 * integration between Spring Boot, JUnit 5, and the database testing framework.
 */
@SpringBootApplication
public class ExampleApplication {

  /** Creates a new application instance. */
  public ExampleApplication() {
    // Default constructor
  }

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }
}
