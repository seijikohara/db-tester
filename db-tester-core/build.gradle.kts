plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Core - Core implementation library"

dependencies {
    // API module (exposed to consumers)
    api(project(":db-tester-api"))

    // Logging (compile-time only - users provide their own SLF4J binding)
    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)

    // CSV/TSV parsing and YAML output (internal implementation only)
    implementation(platform(libs.jackson.bom))
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
