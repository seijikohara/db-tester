import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
plugins {
    id("dbtester.java-common")
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktlint()
    }
}

extensions.configure<DokkaExtension> {
    dokkaSourceSets.configureEach {
        reportUndocumented.set(true)
        documentedVisibilities.set(
            setOf(
                VisibilityModifier.Public,
                VisibilityModifier.Protected,
            ),
        )
    }
    dokkaPublications.configureEach {
        failOnWarning.set(true)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("dokkaGenerateHtml"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

pluginManager.withPlugin("com.vanniktech.maven.publish") {
    extensions.configure<MavenPublishBaseExtension> {
        configure(
            JavaLibrary(
                javadocJar = JavadocJar.Dokka("dokkaGenerateHtml"),
                sourcesJar = true,
            ),
        )
    }
}
