plugins {
    id("dbtester.kotlin-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.kotest"

description = "DB Tester Kotest - Kotest Framework Extension for database testing"

dependencies {
    api(platform(libs.kotest.bom))
    implementation(platform(libs.kotlin.bom))
    compileOnly(platform(libs.slf4j.bom))

    api(project(":db-tester-api"))
    api(libs.kotest.framework.engine)

    implementation(libs.kotlin.reflect)

    runtimeOnly(project(":db-tester-core"))

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
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Kotest"
        description = "Kotest Framework extension for DB Tester framework"
    }
}
