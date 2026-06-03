plugins {
    id("dbtester.groovy-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.spock.spring.autoconfigure"

description = "DB Tester Spock Spring Boot Starter - Spring Boot AutoConfiguration for Spock database testing"

dependencies {
    implementation(platform(libs.groovy.bom))
    implementation(platform(libs.spock.bom))

    api(project(":db-tester-spock"))

    implementation(project(":db-tester-core"))
    implementation(project(":db-tester-spring-support"))
    implementation(libs.spock.spring)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.groovy)

    compileOnly(libs.spring.boot.starter.jdbc)
    compileOnly(libs.spring.boot.starter.test)
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
        name = "DB Tester Spock Spring Boot Starter"
        description = "Spring Boot Starter for DB Tester framework with Spock auto-configuration"
    }
}
