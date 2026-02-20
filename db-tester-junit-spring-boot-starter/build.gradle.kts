plugins {
    id("dbtester.java-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.junit.spring.autoconfigure"

description = "DB Tester JUnit Spring Boot Starter - Spring Boot AutoConfiguration for JUnit 6 database testing"

dependencies {
    api(project(":db-tester-junit"))

    implementation(project(":db-tester-core"))
    implementation(project(":db-tester-spring-support"))
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(libs.spring.boot.starter.jdbc)
    compileOnly(libs.spring.boot.starter.test)
}

tasks.named<JavaCompile>("compileJava") {
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
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester JUnit Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with JUnit 6 auto-configuration"
    }
}
