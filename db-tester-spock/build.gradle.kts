plugins {
    `java-library`
    groovy
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Spock - Spock Framework Extension for database testing"

dependencies {
    // Public API dependencies
    api(project(":db-tester-core"))
    api(platform(libs.groovy.bom))
    api(libs.groovy)
    api(platform(libs.spock.bom))
    api(libs.spock.core)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
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
