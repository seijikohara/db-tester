plugins {
    groovy
}

description = "DB Tester Example - Spock Framework examples and integration tests"

// Example projects are not published to Maven Central

dependencies {
    // DB Tester dependencies
    testImplementation(project(":db-tester-spock"))
    testImplementation(platform(libs.groovy.bom))
    testImplementation(libs.groovy)
    testImplementation(libs.groovy.sql)

    // Database drivers for examples
    testImplementation(libs.bundles.database.drivers.test)

    // Testcontainers for database integration tests
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers.databases.spock)
    testImplementation(libs.bundles.database.drivers.integration)

    // Logging
    testImplementation(platform(libs.slf4j.bom))
    testImplementation(libs.slf4j.api)
    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.junit.platform.launcher)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            targets.configureEach {
                testTask.configure {
                    testLogging {
                        events("passed", "skipped", "failed")
                    }
                }
            }
        }
    }
}
