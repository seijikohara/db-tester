plugins {
    id("dbtester.example")
    groovy
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - Spock Spring Boot Starter Integration"

dependencies {
    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-spock-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spock.spring)
            }
        }
    }
}
