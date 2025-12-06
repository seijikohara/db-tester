plugins {
    groovy
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - Spock Spring Boot Starter Integration"

// Example projects are not published to Maven Central

dependencies {
    // Groovy
    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)

    // Spring Boot dependencies
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)

    // Test dependencies
    testImplementation(project(":db-tester-spock-spring-boot-starter"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spock.spring)
}

