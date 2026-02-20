plugins {
    id("dbtester.java-library")
    id("dbtester.publishing")
}

description = "DB Tester JUnit - JUnit Jupiter Extension for database testing"

dependencies {
    api(project(":db-tester-api"))
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    runtimeOnly(project(":db-tester-core"))

    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester JUnit"
        description = "JUnit Jupiter extension for DB Tester framework"
    }
}
