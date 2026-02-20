import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("dbtester.java-common")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

pluginManager.withPlugin("java") {
    extensions.configure<TestingExtension> {
        suites {
            withType(JvmTestSuite::class.java).configureEach {
                useJUnitJupiter(catalog.findVersion("junit").get().requiredVersion)
            }
        }
    }

    tasks.withType<Test>().configureEach {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
