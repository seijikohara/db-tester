plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester API - Public API for database testing"

dependencies {
    // Null safety annotations
    api(libs.jspecify)
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
        name = "DB Tester API"
        description = "Public API for DB Tester database testing framework"
    }
}
