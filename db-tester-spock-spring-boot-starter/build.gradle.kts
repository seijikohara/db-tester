plugins {
    `java-library`
    groovy
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Spock Spring Boot Starter - Spring Boot AutoConfiguration for Spock database testing"

dependencies {
    // Groovy BOM for version management
    implementation(platform(libs.groovy.bom))

    // Public API dependency
    api(project(":db-tester-spock"))

    // Internal implementation
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.test)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Groovy for extension implementation
    implementation(libs.groovy)

    // Optional dependencies (provided at runtime by Spring Boot)
    compileOnly(libs.spring.boot.starter.jdbc)
    compileOnly(libs.spring.boot.starter.test)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.spring.test)
                implementation(libs.spring.boot.test)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Spock Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with Spock auto-configuration"
    }
}
