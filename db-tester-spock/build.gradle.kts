plugins {
    id("dbtester.groovy-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.spock"

description = "DB Tester Spock - Spock Extension for database testing"

dependencies {
    api(project(":db-tester-api"))
    api(platform(libs.spock.bom))
    api(libs.spock.core)

    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)

    runtimeOnly(project(":db-tester-core"))

    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
                runtimeOnly(libs.junit.platform.launcher)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Spock"
        description = "Spock extension for DB Tester framework"
    }
}
