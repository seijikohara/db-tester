plugins {
    `java-library`
    groovy
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Spock - Spock Framework Extension for database testing"

dependencies {
    // Public API dependencies (only db-tester-api)
    api(project(":db-tester-api"))
    api(platform(libs.groovy.bom))
    api(libs.groovy)
    api(platform(libs.spock.bom))
    api(libs.spock.core)

    // Compile-time dependency for logging
    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly(project(":db-tester-core"))
                runtimeOnly(libs.slf4j.simple)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Spock"
        description = "Spock Framework extension for DB Tester framework"
    }
}
