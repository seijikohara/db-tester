plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Core - Core implementation library"

dependencies {
    // API module (exposed to consumers)
    api(project(":db-tester-api"))

    // Logging
    api(platform(libs.slf4j.bom))
    api(libs.slf4j.api)

    // CSV/TSV parsing and YAML output
    api(platform(libs.jackson.bom))
    implementation(libs.jackson.dataformat.csv)
    implementation(libs.jackson.dataformat.yaml)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
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
