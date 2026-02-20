plugins {
    id("dbtester.kotlin-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.kotest.spring.autoconfigure"

description = "DB Tester Kotest Spring Boot Starter - Spring Boot AutoConfiguration for Kotest database testing"

dependencies {
    api(project(":db-tester-kotest"))

    implementation(project(":db-tester-core"))
    implementation(project(":db-tester-spring-support"))
    implementation(platform(libs.kotlin.bom))
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.autoconfigure)

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

mavenPublishing {
    pom {
        name = "DB Tester Kotest Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with Kotest auto-configuration"
    }
}
