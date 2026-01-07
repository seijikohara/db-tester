// Gradle 9+ settings
// Configuration Cache can be enabled via --configuration-cache flag or gradle.properties
// Note: Some plugins (e.g., Spotless) may not fully support Configuration Cache yet

// Plugin version management
// https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_management
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Toolchain resolver for automatic JDK provisioning
    // https://docs.gradle.org/current/userguide/toolchains.html
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "db-tester"

// Centralized dependency management (Gradle 9 best practice)
// https://docs.gradle.org/current/userguide/best_practices_dependencies.html
dependencyResolutionManagement {
    // Enforce all repositories are declared in settings, not in build scripts
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

include(
    // Core modules (published to Maven Central)
    "db-tester-bom",
    "db-tester-api",
    "db-tester-core",
    "db-tester-spring-support",
    "db-tester-junit",
    "db-tester-spock",
    "db-tester-kotest",
    "db-tester-junit-spring-boot-starter",
    "db-tester-spock-spring-boot-starter",
    "db-tester-kotest-spring-boot-starter",
    // Example modules (not published)
    "examples:db-tester-example-junit",
    "examples:db-tester-example-spock",
    "examples:db-tester-example-kotest",
    "examples:db-tester-example-junit-spring-boot-starter",
    "examples:db-tester-example-spock-spring-boot-starter",
    "examples:db-tester-example-kotest-spring-boot-starter",
)
