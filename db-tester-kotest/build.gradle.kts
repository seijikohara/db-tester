plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Kotest - Kotest Framework Extension for database testing"

dependencies {
    // BOM for version management (must be first)
    api(platform(libs.kotest.bom))
    implementation(platform(libs.kotlin.bom))
    compileOnly(platform(libs.slf4j.bom))

    // Public API dependencies (only db-tester-api and Kotest API)
    api(project(":db-tester-api"))
    api(libs.kotest.framework.engine)

    // Kotlin runtime dependencies
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    // Compile-time dependency for logging
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.kotest.bom))
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)
                implementation(libs.mockk)
                runtimeOnly(project(":db-tester-core"))
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
        name = "DB Tester Kotest"
        description = "Kotest Framework extension for DB Tester framework"
    }
}
