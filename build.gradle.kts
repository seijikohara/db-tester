import com.diffplug.gradle.spotless.SpotlessTask

plugins {
    alias(libs.plugins.axion.release)
    alias(libs.plugins.spotless)
    alias(libs.plugins.version.catalog.update)
}

group = "io.github.seijikohara"

// Configure version management with axion-release-plugin
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
tasks.withType<SpotlessTask>().configureEach {
    notCompatibleWithConfigurationCache("Spotless tasks are not compatible with configuration cache")
}
