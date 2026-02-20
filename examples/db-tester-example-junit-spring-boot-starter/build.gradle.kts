plugins {
    id("dbtester.example")
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - JUnit 6 Spring Boot Starter Integration"

dependencies {
    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-junit-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
            }
        }
    }
}
