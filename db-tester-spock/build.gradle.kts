plugins {
    `java-library`
    groovy
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Spock - Spock Extension for database testing"

dependencies {
    // Public API dependencies (only db-tester-api and Spock API)
    api(project(":db-tester-api"))
    api(libs.spock.core)

    // Groovy and Spock BOM for version management (internal only)
    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)
    implementation(platform(libs.spock.bom))

    // Runtime dependency for SPI implementation (ServiceLoader)
    runtimeOnly(project(":db-tester-core"))

    // Compile-time dependency for logging
    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Spock"
        description = "Spock extension for DB Tester framework"
    }
}
