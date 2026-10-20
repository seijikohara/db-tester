plugins {
    id("dbtester.example")
    groovy
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

description = "DB Tester Example - Spock Spring Boot Starter Integration"

// The Spring Boot BOM manages Groovy 5, but Spock's -groovy-6.0 variant refuses to compile specs
// with any Groovy version other than 6.x. Override the managed version, as consumers must.
extra["groovy.version"] = libs.versions.groovy.get()

dependencies {
    implementation(platform(libs.groovy.bom))
    implementation(libs.groovy)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.h2)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(project(":db-tester-spock-spring-boot-starter"))
                implementation(libs.spring.boot.starter.test)
                implementation(libs.spock.spring)
            }
        }
    }
}
