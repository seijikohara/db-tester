import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.quality.CodeNarc
import org.gradle.api.plugins.quality.CodeNarcExtension

plugins {
    id("dbtester.java-common")
    `java-library`
    groovy
    codenarc
    id("com.diffplug.spotless")
}

val catalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

spotless {
    groovy {
        importOrder()
        greclipse()
    }
}

extensions.configure<CodeNarcExtension> {
    configFile = rootProject.file("config/codenarc/codenarc.xml")
    reportFormat = "html"
    isIgnoreFailures = false
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

testing {
    suites {
        withType<JvmTestSuite>().configureEach {
            useSpock(catalog.findVersion("spock").get().requiredVersion)
        }
    }
}

tasks.withType<GroovyCompile>().configureEach {
    options.encoding = "UTF-8"
}

abstract class VerifyGroovydocTask : DefaultTask() {
    @get:InputFiles
    abstract val sourceDirs: ConfigurableFileCollection

    @TaskAction
    fun verify() {
        val classPattern = Regex("""^\s*(class|interface|trait|enum)\s+\w+""", RegexOption.MULTILINE)
        val javadocPattern = Regex("""/\*\*[\s\S]*?\*/\s*(\n\s*@\w+[\s\S]*?)*\n\s*(class|interface|trait|enum)\s+\w+""")

        sourceDirs
            .files
            .filter { it.exists() }
            .asSequence()
            .flatMap { srcDir: File ->
                srcDir
                    .walkTopDown()
                    .filter { file: File ->
                        file.isFile &&
                            file.extension == "groovy" &&
                            file.name != "package-info.groovy"
                    }.map { file: File -> file to file.relativeTo(srcDir).path }
            }.mapNotNull { (file: File, relativePath: String) ->
                val content = file.readText()
                when {
                    !classPattern.containsMatchIn(content) -> null
                    !javadocPattern.containsMatchIn(content) -> "Missing class-level Javadoc: $relativePath"
                    else -> null
                }
            }.sorted()
            .toList()
            .takeIf { list: List<String> -> list.isNotEmpty() }
            ?.joinToString(prefix = "Groovydoc violations found:\n", separator = "\n") { msg: String -> "  - $msg" }
            ?.let { throw GradleException(it) }
    }
}

tasks.register<VerifyGroovydocTask>("verifyGroovydoc") {
    group = "verification"
    description = "Verifies all Groovy classes have Javadoc comments"
    sourceDirs.from(
        project.extensions
            .getByType<JavaPluginExtension>()
            .sourceSets
            .flatMap { sourceSet ->
                sourceSet.extensions.getByType<org.gradle.api.tasks.GroovySourceDirectorySet>().srcDirs
            },
    )
}

tasks.named("check") {
    dependsOn("verifyGroovydoc")
}

pluginManager.withPlugin("com.vanniktech.maven.publish") {
    extensions.configure<MavenPublishBaseExtension> {
        configure(
            JavaLibrary(
                javadocJar = JavadocJar.Dokka("groovydoc"),
                sourcesJar = SourcesJar.Sources(),
            ),
        )
    }
}
