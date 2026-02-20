plugins {
    id("dbtester.example")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
}

description = "DB Tester Example - Kotest Spring Boot Starter Integration"

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-kotest-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
                implementation(platform(libs.kotest.bom))
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}
