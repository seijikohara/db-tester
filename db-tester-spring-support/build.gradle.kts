plugins {
    `java-library`
    alias(libs.plugins.maven.publish)
}

description = "DB Tester Spring Support - Common Spring utilities for database testing"

dependencies {
    // API module dependency
    api(project(":db-tester-api"))

    // Spring Context (for ApplicationContext utilities)
    implementation(libs.spring.context)

    // Logging (compile-time only - users provide their own SLF4J binding)
    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.spring.test)
                implementation(platform(libs.slf4j.bom))
                implementation(libs.slf4j.api)
                runtimeOnly(libs.slf4j.simple)
            }
        }
    }
}

mavenPublishing {
    pom {
        name = "DB Tester Spring Support"
        description = "Common Spring utilities for DB Tester framework"
    }
}
