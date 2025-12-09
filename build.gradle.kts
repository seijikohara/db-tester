import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.plugins.quality.CodeNarcExtension
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    // Plugins with `apply false` are available for subprojects but not applied to root
    // This is required because plugins {} block cannot be used within subprojects/allprojects blocks
    alias(libs.plugins.axion.release)
    alias(libs.plugins.errorprone) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.version.catalog.update)
}

group = "io.github.seijikohara"

// Configure version management with axion-release-plugin
// Version is derived from Git tags (e.g., v1.2.0 -> 1.2.0)
scmVersion {
    useHighestVersion = true
    tag {
        prefix = "v"
        versionSeparator = ""
    }
    versionCreator("simple")
    repository {
        pushTagsOnly = true
    }
    checks {
        uncommittedChanges = false
        aheadOfRemote = false
    }
}

version = scmVersion.version

versionCatalogUpdate {
    sortByKey = true
}

spotless {
    kotlinGradle {
        ktlint()
    }
}

// Mark Spotless tasks as not compatible with configuration cache
// This prevents caching of Spotless tasks which have serialization issues
// See: https://github.com/diffplug/spotless/issues/987
tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
    notCompatibleWithConfigurationCache("Spotless tasks are not compatible with configuration cache")
}

allprojects {
    group = rootProject.group
    version = rootProject.version
}

// Module classification
val javaModules =
    setOf(
        "db-tester-api",
        "db-tester-core",
        "db-tester-junit",
        "db-tester-junit-spring-boot-starter",
        "db-tester-example-junit",
        "db-tester-example-junit-spring-boot-starter",
    )

val groovyModules =
    setOf(
        "db-tester-spock",
        "db-tester-spock-spring-boot-starter",
        "db-tester-example-spock",
        "db-tester-example-spock-spring-boot-starter",
    )

// JPMS Automatic-Module-Name mapping
// Only for modules without module-info.java (Spring Boot Starters and Groovy modules)
val automaticModuleNames =
    mapOf(
        "db-tester-spock" to "io.github.seijikohara.dbtester.spock",
        "db-tester-junit-spring-boot-starter" to "io.github.seijikohara.dbtester.junit.spring.autoconfigure",
        "db-tester-spock-spring-boot-starter" to "io.github.seijikohara.dbtester.spock.spring.autoconfigure",
    )

// Common configuration for all subprojects (excluding java-platform projects)
subprojects {
    // Skip configuration for java-platform projects (e.g., BOM)
    // to avoid unsafe configuration resolution in Gradle 9+
    if (name == "db-tester-bom") return@subprojects

    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral()
            signAllPublications()
            pom {
                url = "https://github.com/seijikohara/db-tester"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "seijikohara"
                        name = "Seiji Kohara"
                        email = "seiji.kohara@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/seijikohara/db-tester.git"
                    developerConnection = "scm:git:ssh://github.com/seijikohara/db-tester.git"
                    url = "https://github.com/seijikohara/db-tester"
                }
            }
        }
    }

    pluginManager.withPlugin("jacoco") {
        extensions.configure<JacocoPluginExtension> {
            toolVersion = "0.8.14"
        }
        tasks.withType<JacocoReport>().configureEach {
            reports {
                xml.required = true
                html.required = true
            }
        }
        tasks.named<Test>("test") {
            finalizedBy(tasks.named("jacocoTestReport"))
        }
    }

    pluginManager.withPlugin("java") {
        val projectName = this@subprojects.name

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion = JavaLanguageVersion.of(21)
            }
        }

        tasks.withType<Jar>().configureEach {
            automaticModuleNames[projectName]?.let { moduleName ->
                manifest {
                    attributes("Automatic-Module-Name" to moduleName)
                }
            }
        }
    }
}

// Java subprojects configuration
subprojects.filter { it.name in javaModules }.forEach { subproject ->
    subproject.run {
        apply(plugin = "java")
        apply(plugin = "jvm-test-suite")
        apply(plugin = "jacoco")
        apply(plugin = "checkstyle")
        apply(plugin = "com.diffplug.spotless")
        apply(plugin = "net.ltgt.errorprone")

        extensions.configure<SpotlessExtension> {
            java {
                googleJavaFormat()
            }
        }

        tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
            notCompatibleWithConfigurationCache("Spotless tasks are not compatible with configuration cache")
        }

        extensions.configure<CheckstyleExtension> {
            configFile = rootProject.file("config/checkstyle/checkstyle.xml")
            configDirectory = rootProject.file("config/checkstyle")
            isIgnoreFailures = false
            isShowViolations = true
            // Treat warnings as errors - zero tolerance for any violations
            maxWarnings = 0
            maxErrors = 0
        }

        tasks.withType<Checkstyle>().configureEach {
            // Checkstyle reports
            reports {
                xml.required = true
                html.required = true
            }
        }

        extensions.configure<TestingExtension> {
            suites {
                withType<JvmTestSuite>().configureEach {
                    useJUnitJupiter(rootProject.libs.versions.junit)
                }
            }
        }

        dependencies {
            "compileOnly"(rootProject.libs.checker.qual)
            "testCompileOnly"(rootProject.libs.checker.qual)
            "errorprone"(rootProject.libs.errorprone.annotations)
            "errorprone"(rootProject.libs.errorprone.core)
            "errorprone"(rootProject.libs.errorprone.refaster)
            "errorprone"(rootProject.libs.nullaway)
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(
                listOf(
                    "-Xlint:all,-processing",
                    "-Werror",
                    "-Xdoclint:all",
                    "-XDaddTypeAnnotationsToSymbol=true",
                ),
            )
            options.errorprone {
                allErrorsAsWarnings = false
                disableWarningsInGeneratedCode = false

                // NullAway configuration
                check("NullAway", CheckSeverity.ERROR)
                option("NullAway:AnnotatedPackages", "io.github.seijikohara.dbtester,example")
                option("NullAway:JSpecifyMode", "true")
                option("NullAway:TreatGeneratedAsUnannotated", "true")
                option("NullAway:CheckOptionalEmptiness", "true")
                option("NullAway:CheckContracts", "true")
                option("NullAway:HandleTestAssertionLibraries", "true")

                // Optional usage checks
                // Ref: docs/code-styles/JAVA.md - Optional Patterns
                check("OptionalNotPresent", CheckSeverity.ERROR)
                check("OptionalOfRedundantMethod", CheckSeverity.ERROR)

                // Stream API checks
                // Ref: docs/code-styles/JAVA.md - Stream API
                check("StreamResourceLeak", CheckSeverity.ERROR)
                check("StreamToIterable", CheckSeverity.ERROR)
                check("UnnecessaryMethodReference", CheckSeverity.ERROR)

                // Additional code quality checks
                check("ImmutableEnumChecker", CheckSeverity.ERROR)
                check("UnnecessaryLambda", CheckSeverity.ERROR)
            }
        }

        tasks.withType<Javadoc>().configureEach {
            (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                charSet = "UTF-8"
                addStringOption("Xdoclint:all", "-quiet")
            }
        }

        val sourceDirs =
            provider {
                extensions
                    .getByType<JavaPluginExtension>()
                    .sourceSets
                    .flatMap { sourceSet -> sourceSet.java.srcDirs }
                    .filter { it.exists() }
                    .toSet()
            }

        tasks.register("verifyNullMarkedPackages") {
            group = "verification"
            description = "Verifies all Java packages have package-info.java with @NullMarked annotation"
            inputs.files(sourceDirs)

            doLast {
                val hasNullMarked = Regex("""@NullMarked\b|@org\.jspecify\.annotations\.NullMarked\b""")::containsMatchIn

                sourceDirs
                    .get()
                    .asSequence()
                    .flatMap { srcDir ->
                        srcDir
                            .walkTopDown()
                            .filter { file ->
                                file.isFile &&
                                    file.extension == "java" &&
                                    file.name != "package-info.java" &&
                                    file.name != "module-info.java"
                            }.map { file -> file.parentFile }
                            .distinct()
                            .map { packageDir -> packageDir to packageDir.relativeTo(srcDir).path }
                    }.mapNotNull { (packageDir, relativePath) ->
                        val packageInfoFile = File(packageDir, "package-info.java")
                        when {
                            !packageInfoFile.exists() -> "Missing package-info.java: $relativePath"
                            !hasNullMarked(packageInfoFile.readText()) -> "Missing @NullMarked annotation: $relativePath/package-info.java"
                            else -> null
                        }
                    }.sorted()
                    .toList()
                    .takeIf(List<String>::isNotEmpty)
                    ?.joinToString(prefix = "Null safety violations found:\n", separator = "\n") { "  - $it" }
                    ?.let { throw GradleException(it) }
            }
        }

        tasks.named("check") {
            dependsOn("verifyNullMarkedPackages")
        }
    }
}

// Groovy subprojects configuration
subprojects.filter { it.name in groovyModules }.forEach { subproject ->
    subproject.run {
        apply(plugin = "groovy")
        apply(plugin = "jvm-test-suite")
        apply(plugin = "jacoco")
        apply(plugin = "codenarc")
        apply(plugin = "com.diffplug.spotless")

        extensions.configure<SpotlessExtension> {
            groovy {
                importOrder()
                greclipse()
            }
        }

        tasks.withType<com.diffplug.gradle.spotless.SpotlessTask>().configureEach {
            notCompatibleWithConfigurationCache("Spotless tasks are not compatible with configuration cache")
        }

        extensions.configure<CodeNarcExtension> {
            configFile = rootProject.file("config/codenarc/codenarc.xml")
            reportFormat = "html"
            isIgnoreFailures = false
            // Treat all priority levels as errors - zero tolerance for any violations
            maxPriority1Violations = 0
            maxPriority2Violations = 0
            maxPriority3Violations = 0
        }

        tasks.withType<CodeNarc>().configureEach {
            reports {
                xml.required = true
                html.required = true
            }
        }

        extensions.configure<TestingExtension> {
            suites {
                withType<JvmTestSuite>().configureEach {
                    useSpock(rootProject.libs.versions.spock)
                }
            }
        }

        tasks.withType<GroovyCompile>().configureEach {
            options.encoding = "UTF-8"
        }

        // Configure maven-publish to use groovydoc instead of javadoc
        pluginManager.withPlugin("com.vanniktech.maven.publish") {
            extensions.configure<MavenPublishBaseExtension> {
                configure(
                    com.vanniktech.maven.publish.JavaLibrary(
                        javadocJar = JavadocJar.Dokka("groovydoc"),
                        sourcesJar = true,
                    ),
                )
            }
        }
    }
}
