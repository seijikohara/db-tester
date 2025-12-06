plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Core - Core library with public API and DbUnit integration"

dependencies {
    // Public API dependencies (exposed to consumers)
    api(libs.jspecify)
    api(platform(libs.slf4j.bom))
    api(libs.slf4j.api)

    // Internal implementation dependencies (hidden from consumers)
    implementation(libs.dbunit)
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
        description = "Core implementation of DB Tester framework with DbUnit integration"
    }
}
