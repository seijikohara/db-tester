plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester JUnit - JUnit Jupiter Extension for database testing"

dependencies {
    // Public API dependencies
    api(project(":db-tester-core"))
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    // Internal implementation dependencies
    implementation(libs.junit.jupiter)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
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
