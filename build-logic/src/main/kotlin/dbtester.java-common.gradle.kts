import com.diffplug.gradle.spotless.SpotlessTask

plugins {
    jacoco
}

group = rootProject.group
version = rootProject.version

pluginManager.withPlugin("java") {
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<Jar>().configureEach {
        val moduleName = project.findProperty("automaticModuleName") as? String
        if (moduleName != null) {
            manifest {
                attributes("Automatic-Module-Name" to moduleName)
            }
        }
    }
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.withType<JacocoReport>().configureEach {
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal("0.70")
            }
        }
    }
    if (project.path.startsWith(":examples:")) {
        isEnabled = false
    }
}

// Framework entry points that drive real database I/O through test-framework internals
// (Spock IMethodInvocation, Kotest TestCase) are verified by integration tests in examples/,
// not unit coverage. Exclude only the classes that resist isolated unit testing per module.
val jacocoExclusionsByModule =
    mapOf(
        "db-tester-spock" to listOf("**/DatabaseTestExtension*.class"),
        "db-tester-spock-spring-boot-starter" to
            listOf(
                "**/SpringBootDatabaseTestExtension*.class",
                "**/SpringBootDatabaseTestInterceptor*.class",
            ),
        "db-tester-kotest-spring-boot-starter" to listOf("**/SpringBootDatabaseTestExtension*.class"),
    )

jacocoExclusionsByModule[project.name]?.let { exclusions ->
    afterEvaluate {
        tasks.withType<JacocoCoverageVerification>().configureEach {
            classDirectories.setFrom(
                classDirectories.files.map {
                    fileTree(it) { exclude(exclusions) }
                },
            )
        }
    }
}

pluginManager.withPlugin("java") {
    tasks.named<Test>("test") {
        finalizedBy(tasks.named("jacocoTestReport"))
    }
    tasks.named("jacocoTestReport") {
        finalizedBy(tasks.named("jacocoTestCoverageVerification"))
    }
}

pluginManager.withPlugin("com.diffplug.spotless") {
    tasks.withType<SpotlessTask>().configureEach {
        notCompatibleWithConfigurationCache("Spotless tasks are not compatible with configuration cache")
    }
}
