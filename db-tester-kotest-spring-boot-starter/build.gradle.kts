plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Kotest Spring Boot Starter - Spring Boot AutoConfiguration for Kotest database testing"

dependencies {
    // Public API dependency
    api(project(":db-tester-kotest"))

    // Core implementation (provides SPI implementations)
    implementation(project(":db-tester-core"))

    // Spring support (common DataSource registration logic)
    implementation(project(":db-tester-spring-support"))

    // Kotlin
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    // Internal implementation
    implementation(libs.spring.boot.autoconfigure)

    // Optional dependencies (provided at runtime by Spring Boot)
    compileOnly(libs.spring.boot.starter.jdbc)
    compileOnly(libs.spring.boot.starter.test)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.kotest.bom))
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)
                implementation(libs.mockk)
                implementation(libs.spring.test)
                implementation(libs.spring.boot.test)
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

mavenPublishing {
    pom {
        name = "DB Tester Kotest Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with Kotest auto-configuration"
    }
}
