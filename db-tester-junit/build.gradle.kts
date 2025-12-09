plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester JUnit - JUnit Jupiter Extension for database testing"

dependencies {
    // Public API dependencies (only db-tester-api)
    api(project(":db-tester-api"))
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    // JUnit Jupiter implementation
    implementation(libs.junit.jupiter)

    // Compile-time dependency for logging
    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                runtimeOnly(project(":db-tester-core"))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester JUnit"
        description = "JUnit Jupiter extension for DB Tester framework"
    }
}
