package example

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Spring Boot application for demonstrating db-tester-spock-spring-boot-starter.
 *
 * <p>This application provides a minimal Spring Boot setup with H2 database for testing the
 * integration between Spring Boot, Spock, and the database testing framework.
 */
@SpringBootApplication
class ExampleApplication {

	/**
	 * Application entry point.
	 *
	 * @param args command line arguments
	 */
	static void main(String[] args) {
		SpringApplication.run(ExampleApplication, args)
	}
}
