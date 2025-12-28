package example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot application for demonstrating db-tester-kotest-spring-boot-starter.
 *
 * This application provides a minimal Spring Boot setup with H2 database for testing the
 * integration between Spring Boot, Kotest, and the database testing framework.
 */
@SpringBootApplication
class ExampleApplication

/**
 * Application entry point.
 *
 * @param args command line arguments
 */
fun main(args: Array<String>): Unit = runApplication<ExampleApplication>(*args).let { }
