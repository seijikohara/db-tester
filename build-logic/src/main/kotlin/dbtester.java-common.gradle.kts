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

val jacocoExcludedModules =
    setOf(
        "db-tester-spock",
        "db-tester-spock-spring-boot-starter",
        "db-tester-kotest",
        "db-tester-kotest-spring-boot-starter",
    )

if (project.name in jacocoExcludedModules) {
    afterEvaluate {
        tasks.withType<JacocoCoverageVerification>().configureEach {
            classDirectories.setFrom(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude(
                            "**/DatabaseTestExtension*.class",
                            "**/DatabaseTestInterceptor*.class",
                            "**/SpringBootDatabaseTestExtension*.class",
                            "**/SpringBootDatabaseTestInterceptor*.class",
                        )
                    }
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
