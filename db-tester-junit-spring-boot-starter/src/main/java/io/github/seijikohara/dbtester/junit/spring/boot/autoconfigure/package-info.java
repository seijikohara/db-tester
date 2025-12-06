/**
 * Spring Boot auto-configuration for DB Tester JUnit 5 integration.
 *
 * <p>This package provides automatic configuration for integrating the DB Tester framework with
 * Spring Boot applications using JUnit 5 (Jupiter) for testing. The auto-configuration
 * automatically registers Spring-managed {@link javax.sql.DataSource} beans with the database
 * testing framework.
 *
 * <h2>Usage</h2>
 *
 * <p>Add the dependency to your build file:
 *
 * <pre>{@code
 * testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
 * }</pre>
 *
 * <p>Use in your tests:
 *
 * <pre>{@code
 * @SpringBootTest
 * @ExtendWith(SpringBootDatabaseTestExtension.class)
 * class MyRepositoryTest {
 *
 *     @Preparation
 *     @Expectation
 *     @Test
 *     void testCreate() {
 *         // Test logic
 *     }
 * }
 * }</pre>
 *
 * @see
 *     io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure.DbTesterJUnitAutoConfiguration
 * @see
 *     io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure.SpringBootDatabaseTestExtension
 */
@NullMarked
package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import org.jspecify.annotations.NullMarked;
