plugins {
    java
}

description = "DB Tester Example - JUnit Jupiter examples and integration tests"

// Example projects are not published to Maven Central

dependencies {
    // DB Tester dependencies
    testImplementation(project(":db-tester-junit"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)

    // Database drivers for examples
    testImplementation(libs.bundles.database.drivers.test)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.bundles.testcontainers.databases)
    testImplementation(libs.bundles.database.drivers.integration)

    // Logging
    testImplementation(platform(libs.slf4j.bom))
    testImplementation(libs.slf4j.api)
    testRuntimeOnly(libs.slf4j.simple)
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
