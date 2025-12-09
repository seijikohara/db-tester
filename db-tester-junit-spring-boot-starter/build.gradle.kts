plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester JUnit Spring Boot Starter - Spring Boot AutoConfiguration for JUnit 5 database testing"

dependencies {
    // Public API dependency
    api(project(":db-tester-junit"))

    // Core implementation (provides SPI implementations)
    implementation(project(":db-tester-core"))

    // Internal implementation
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Optional dependencies (provided at runtime by Spring Boot)
    compileOnly(libs.spring.boot.starter.jdbc)
    compileOnly(libs.spring.boot.starter.test)
}

tasks.named<JavaCompile>("compileJava") {
    // Spring Boot configuration processor requires resources to be processed first
    inputs.files(tasks.named("processResources"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.spring.test)
                implementation(libs.spring.boot.test)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester JUnit Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with JUnit 5 auto-configuration"
    }
}
