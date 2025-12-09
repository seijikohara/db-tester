plugins {
    `java-library`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - JUnit 5 Spring Boot Starter Integration"

// Example projects are not published to Maven Central

dependencies {
    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-junit-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
            }
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
