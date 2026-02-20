import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("dbtester.java-common")
    `java-library`
    checkstyle
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
}

val catalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

spotless {
    java {
        googleJavaFormat()
    }
}

checkstyle {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    configDirectory = rootProject.file("config/checkstyle")
    isIgnoreFailures = false
    isShowViolations = true
    maxWarnings = 0
    maxErrors = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required = true
        html.required = true
    }
}

testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useJUnitJupiter(catalog.findVersion("junit").get().requiredVersion)
        }
    }
}

dependencies {
    "compileOnly"(catalog.findLibrary("checker-qual").get())
    "testCompileOnly"(catalog.findLibrary("checker-qual").get())
    "errorprone"(catalog.findLibrary("errorprone-annotations").get())
    "errorprone"(catalog.findLibrary("errorprone-core").get())
    "errorprone"(catalog.findLibrary("errorprone-refaster").get())
    "errorprone"(catalog.findLibrary("nullaway").get())
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

        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "io.github.seijikohara.dbtester,example")
        option("NullAway:JSpecifyMode", "true")
        option("NullAway:TreatGeneratedAsUnannotated", "true")
        option("NullAway:CheckOptionalEmptiness", "true")
        option("NullAway:CheckContracts", "true")
        option("NullAway:HandleTestAssertionLibraries", "true")

        check("OptionalNotPresent", CheckSeverity.ERROR)
        check("OptionalOfRedundantMethod", CheckSeverity.ERROR)
        check("StreamResourceLeak", CheckSeverity.ERROR)
        check("StreamToIterable", CheckSeverity.ERROR)
        check("UnnecessaryMethodReference", CheckSeverity.ERROR)
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

abstract class VerifyNullMarkedPackagesTask : DefaultTask() {
    @get:InputFiles
    abstract val sourceDirs: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val hasNullMarked = Regex("""@NullMarked\b|@org\.jspecify\.annotations\.NullMarked\b""")::containsMatchIn

        sourceDirs
            .files
            .filter { it.exists() }
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

tasks.register<VerifyNullMarkedPackagesTask>("verifyNullMarkedPackages") {
    group = "verification"
    description = "Verifies all Java packages have package-info.java with @NullMarked annotation"
    sourceDirs.from(
        project.extensions
            .getByType<JavaPluginExtension>()
            .sourceSets
            .flatMap { sourceSet -> sourceSet.java.srcDirs },
    )
}

tasks.named("check") {
    dependsOn("verifyNullMarkedPackages")
}
