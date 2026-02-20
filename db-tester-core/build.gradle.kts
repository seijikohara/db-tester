plugins {
    id("dbtester.java-library")
    id("dbtester.publishing")
}

description = "DB Tester Core - Core implementation library"

dependencies {
    api(project(":db-tester-api"))

    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)

    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.dataformat.yaml)

    compileOnly(libs.datafaker)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.datafaker)
                runtimeOnly(platform(libs.slf4j.bom))
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Core"
        description = "Core implementation of DB Tester framework"
    }
}
