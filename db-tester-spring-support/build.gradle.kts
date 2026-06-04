plugins {
    id("dbtester.java-library")
    id("dbtester.publishing")
}

extra["automaticModuleName"] = "io.github.seijikohara.dbtester.spring.support"

description = "DB Tester Spring Support - Common Spring utilities for database testing"

dependencies {
    api(project(":db-tester-api"))

    implementation(libs.spring.context)
    implementation(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configuration.processor)

    compileOnly(platform(libs.slf4j.bom))
    compileOnly(libs.slf4j.api)
}

tasks.named<JavaCompile>("compileJava") {
    inputs.files(tasks.named("processResources"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(platform(libs.mockito.bom))
                implementation(libs.mockito.core)
                implementation(libs.mockito.junit.jupiter)
                implementation(libs.spring.test)
                implementation(platform(libs.slf4j.bom))
                implementation(libs.slf4j.api)
                runtimeOnly(libs.slf4j.simple)
                // Configuration.build() resolves a DataSetLoaderProvider via ServiceLoader at
                // runtime, which db-tester-core supplies. Required only for factory tests.
                runtimeOnly(project(":db-tester-core"))
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
