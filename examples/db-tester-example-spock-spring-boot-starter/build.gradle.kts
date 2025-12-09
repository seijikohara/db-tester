plugins {
    groovy
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - Spock Spring Boot Starter Integration"

// Example projects are not published to Maven Central

dependencies {
    // Groovy
    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)

    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-spock-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spock.spring)
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
